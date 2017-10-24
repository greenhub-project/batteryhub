/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmatalonga.greenhub.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.controllers.TaskController;
import com.hmatalonga.greenhub.events.OpenTaskDetailsEvent;
import com.hmatalonga.greenhub.events.TaskRemovedEvent;
import com.hmatalonga.greenhub.models.ui.Task;
import com.hmatalonga.greenhub.ui.adapters.TaskAdapter;
import com.hmatalonga.greenhub.util.SettingsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hmatalonga.greenhub.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TaskListActivity extends BaseActivity {

    private ArrayList<Task> mTaskList;

    private RecyclerView mRecyclerView;

    private TaskAdapter mAdapter;

    /**
     * The {@link android.support.v4.widget.SwipeRefreshLayout} that detects swipe gestures and
     * triggers callbacks in the app.
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ProgressBar mLoader;

    private Task mLastKilledApp;

    private long mLastKilledTimestamp;

    private boolean mIsUpdating;

    private int mSortOrderName;

    private int mSortOrderMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SettingsUtils.isTosAccepted(getApplicationContext())) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_task_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        loadComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_sort_memory) {
            sortTasksBy(Config.SORT_BY_MEMORY, mSortOrderMemory);
            mSortOrderMemory = -mSortOrderMemory;
            return true;
        } else if (id == R.id.action_sort_name) {
            sortTasksBy(Config.SORT_BY_NAME, mSortOrderName);
            mSortOrderName = -mSortOrderName;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsUpdating) initiateRefresh();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTaskRemovedEvent(TaskRemovedEvent event) {
        updateHeaderInfo();
        mLastKilledApp = event.task;
        mLastKilledTimestamp = System.currentTimeMillis();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOpenTaskDetailsEvent(OpenTaskDetailsEvent event) {
        startActivity(new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + event.task.getPackageInfo().packageName)
        ));
    }

    private void loadComponents() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        mLoader = (ProgressBar) findViewById(R.id.loader);
        mLastKilledApp = null;
        mSortOrderName = 1;
        mSortOrderMemory = 1;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskList.isEmpty()) {
                    Snackbar.make(
                            view,
                            getString(R.string.task_no_apps_running),
                            Snackbar.LENGTH_LONG
                    ).show();
                    return;
                }

                int apps = 0;
                double memory = 0;
                String message;

                TaskController controller = new TaskController(getApplicationContext());

                for (Task task : mTaskList) {
                    if (!task.isChecked()) continue;
                    controller.killApp(task);
                    memory += task.getMemory();
                    apps++;
                }
                memory = Math.round(memory * 100.0) / 100.0;

                mRecyclerView.setVisibility(View.GONE);
                mLoader.setVisibility(View.VISIBLE);

                initiateRefresh();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    message = (apps > 0) ?
                            makeMessage(apps) :
                            getString(R.string.task_no_apps_killed);
                } else {
                    message = (apps > 0) ?
                            makeMessage(apps, memory) :
                            getString(R.string.task_no_apps_killed);
                }

                Snackbar.make(
                        view,
                        message,
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                !hasSpecialPermission(getApplicationContext())) {
            showPermissionInfoDialog();
        }

        mTaskList = new ArrayList<>();
        mIsUpdating = false;

        setupRefreshLayout();

        setupRecyclerView();
    }

    private void sortTasksBy(final int filter, final int order) {
        if (filter == Config.SORT_BY_MEMORY) {
            // Sort by memory
            Collections.sort(mTaskList, new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    int result;
                    if (t1.getMemory() < t2.getMemory()) {
                        result = -1;
                    } else if (t1.getMemory() == t2.getMemory()) {
                        result = 0;
                    } else {
                        result = 1;
                    }
                    return order * result;
                }
            });
        } else if (filter == Config.SORT_BY_NAME) {
            // Sort by name
            Collections.sort(mTaskList, new Comparator<Task>() {
                @Override
                public int compare(Task t1, Task t2) {
                    return order * t1.getLabel().compareTo(t2.getLabel());
                }
            });
        }
        mAdapter.notifyDataSetChanged();
    }

    private String makeMessage(int apps) {
        return getString(R.string.task_killed) + " " + apps + " apps!";
    }

    private String makeMessage(int apps, double memory) {
        return getString(R.string.task_killed) + " " + apps + " apps! " +
                getString(R.string.task_cleared) + " " + memory + " MB";
    }

    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TaskAdapter(getApplicationContext(), mTaskList);
        mRecyclerView.setAdapter(mAdapter);

        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    private void setupRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        //noinspection ResourceAsColor
        if (Build.VERSION.SDK_INT >= 23) {
            mSwipeRefreshLayout.setColorSchemeColors(
                    getColor(R.color.color_accent),
                    getColor(R.color.color_primary_dark)
            );
        } else {
            final Context context = getApplicationContext();
            mSwipeRefreshLayout.setColorSchemeColors(
                    ContextCompat.getColor(context, R.color.color_accent),
                    ContextCompat.getColor(context, R.color.color_primary_dark)
            );
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mIsUpdating) initiateRefresh();
            }
        });
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.DKGRAY);
                xMark = ContextCompat.getDrawable(
                        TaskListActivity.this, R.drawable.ic_delete_white_24dp
                );
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) TaskListActivity.this.getResources()
                        .getDimension(R.dimen.fab_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                TaskAdapter testAdapter = (TaskAdapter)recyclerView.getAdapter();
                if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                TaskAdapter adapter = (TaskAdapter)mRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder
                    viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(canvas);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(canvas);

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.DKGRAY);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(canvas);

                }
                super.onDraw(canvas, parent, state);
            }

        });
    }

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {
        mIsUpdating = true;
        setHeaderToRefresh();
        /**
         * Execute the background task, which uses {@link android.os.AsyncTask} to load the data.
         */
        new LoadRunningProcessesTask().execute(getApplicationContext());
    }

    /**
     * When the AsyncTask finishes, it calls onRefreshComplete(), which updates the data in the
     * ListAdapter and turns off the progress bar.
     */
    private void onRefreshComplete(List<Task> result) {
        if (mLoader.getVisibility() == View.VISIBLE) {
            mLoader.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        // Remove all items from the ListAdapter, and then replace them with the new items
        mAdapter.swap(result);
        mIsUpdating = false;
        updateHeaderInfo();
        // Stop the refreshing indicator
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void updateHeaderInfo() {
        String text;
        TextView textView = (TextView) findViewById(R.id.count);
        text = "Apps " + mTaskList.size();
        textView.setText(text);
        textView = (TextView) findViewById(R.id.usage);
        double memory = getAvailableMemory();
        if (memory > 1000) {
            text = getString(R.string.task_free_ram) + " " +
                    (Math.round(memory / 1024.0 * 100.0) / 100.0) + " GB";
        } else {
            text = getString(R.string.task_free_ram) + " " + + memory + " MB";
        }
        textView.setText(text);
    }

    private void setHeaderToRefresh() {
        TextView textView = (TextView) findViewById(R.id.count);
        textView.setText(getString(R.string.header_status_loading));
        textView = (TextView) findViewById(R.id.usage);
        textView.setText("");
    }

    private double getAvailableMemory() {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)
                getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(info);

        return Math.round(info.availMem / 1048576.0 * 100.0) / 100.0;
    }

    private double getTotalUsage(List<Task> list) {
        double usage = 0;
        for (Task task : list) {
            usage += task.getMemory();
        }
        return Math.round(usage * 100.0) / 100.0;
    }

    private boolean isKilledAppAlive(final String label) {
        long now = System.currentTimeMillis();
        if (mLastKilledTimestamp < (now - Config.KILL_APP_TIMEOUT)) {
            mLastKilledApp = null;
            return false;
        }
        for (Task task : mTaskList) {
            if (task.getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    private void checkIfLastAppIsKilled() {
        if (mLastKilledApp != null && isKilledAppAlive(mLastKilledApp.getLabel())) {
            final String packageName = mLastKilledApp.getPackageInfo().packageName;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getString(R.string.kill_app_dialog_text))
                    .setTitle(mLastKilledApp.getLabel());

            builder.setPositiveButton(R.string.force_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    startActivity(new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + packageName)
                    ));
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                }
            });

            builder.create().show();
        }
        mLastKilledApp = null;
    }

    @TargetApi(21)
    private boolean hasSpecialPermission(final Context context) {
        AppOpsManager appOps = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @TargetApi(21)
    private void showPermissionInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.package_usage_permission_text))
                .setTitle(getString(R.string.package_usage_permission_title));

        builder.setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private class LoadRunningProcessesTask extends AsyncTask<Context, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(Context... params) {
            TaskController taskController = new TaskController(params[0]);
            return taskController.getRunningTasks();
        }

        @Override
        protected void onPostExecute(List<Task> result) {
            super.onPostExecute(result);
            onRefreshComplete(result);
            checkIfLastAppIsKilled();
        }

    }
}