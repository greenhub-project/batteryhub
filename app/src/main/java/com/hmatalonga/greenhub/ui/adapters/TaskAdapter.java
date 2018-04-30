package com.hmatalonga.greenhub.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.managers.TaskController;
import com.hmatalonga.greenhub.events.OpenTaskDetailsEvent;
import com.hmatalonga.greenhub.events.TaskRemovedEvent;
import com.hmatalonga.greenhub.models.ui.Task;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hugo on 27-05-2017.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    public static final String TAG = "TaskAdapter";

    private List<Task> mItems;

    private List<Task> mItemsPendingRemoval;

    // is undo on, you can turn it on from the toolbar menu
    private boolean mUndoOn;

    // hanlder for running delayed runnables
    private Handler mHandler;

    // map of mItems to pending runnables, so we can cancel a removal
    private HashMap<Task, Runnable> mPendingRunnables = new HashMap<>();

    private TaskController mTaskController;

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView name;
        TextView memory;
        RelativeLayout details;
        TextView autoStart;
        TextView backgroundService;
        TextView appPackage;
        TextView appVersion;
        ImageView icon;
        ImageView more;
        Button undoButton;
        CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.taskName);
            memory = view.findViewById(R.id.taskMemory);
            details = view.findViewById(R.id.taskDetailsContainer);
            autoStart = view.findViewById(R.id.taskAutoStart);
            backgroundService = view.findViewById(R.id.taskBackgroundService);
            appPackage = view.findViewById(R.id.taskPackage);
            appVersion = view.findViewById(R.id.taskAppVersion);
            icon = view.findViewById(R.id.taskIcon);
            more = view.findViewById(R.id.taskShowDetails);
            undoButton = view.findViewById(R.id.undo_button);
            checkBox = view.findViewById(R.id.checkBox);
        }
    }

    public TaskAdapter(@NonNull final Context context, List<Task> tasks) {
        mItems = tasks;
        mTaskController = new TaskController(context);
        mItemsPendingRemoval = new ArrayList<>();
        mUndoOn = true;
        mHandler = new Handler();
    }

    @Override
    public TaskAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_task,
                parent,
                false
        );
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Task item = mItems.get(position);
        String text;

        if (mItemsPendingRemoval.contains(item)) {
            /** Undo state of item */
            holder.itemView.setBackgroundColor(Color.DKGRAY);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);

            holder.name.setVisibility(View.GONE);
            holder.memory.setVisibility(View.GONE);
            holder.details.setVisibility(View.GONE);
            holder.autoStart.setVisibility(View.GONE);
            holder.backgroundService.setVisibility(View.GONE);
            holder.appPackage.setVisibility(View.GONE);
            holder.appVersion.setVisibility(View.GONE);
            holder.icon.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);

            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setOnCheckedChangeListener(null);

            holder.undoButton.setVisibility(View.VISIBLE);
            holder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = mPendingRunnables.get(item);
                    mPendingRunnables.remove(item);
                    if (pendingRemovalRunnable != null) {
                        mHandler.removeCallbacks(pendingRemovalRunnable);
                    }
                    mItemsPendingRemoval.remove(item);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(mItems.indexOf(item));
                }
            });
        } else {
            /** Visible state of item */
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.details.getVisibility() == View.GONE) {
                        holder.more.setImageResource(R.drawable.ic_chevron_up_grey600_18dp);
                        holder.details.setVisibility(View.VISIBLE);
                    } else if (holder.details.getVisibility() == View.VISIBLE) {
                        holder.more.setImageResource(R.drawable.ic_chevron_down_grey600_18dp);
                        holder.details.setVisibility(View.GONE);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    EventBus.getDefault().post(new OpenTaskDetailsEvent(item));
                    return true;
                }
            });

            holder.name.setText(item.getLabel());
            if (item.getMemory() > 0) {
                text = item.getMemory() + " MB";
            } else {
                text = "N/A";
            }
            holder.memory.setText(text);
            holder.icon.setImageDrawable(
                    mTaskController.iconForApp(item.getPackageInfo())
            );
            if (item.isAutoStart()) {
                holder.autoStart.setVisibility(View.VISIBLE);
            }
            if (item.hasBackgroundService()) {
                holder.backgroundService.setVisibility(View.VISIBLE);
            }
            text = "Package: " + item.getPackageInfo().packageName;
            holder.appPackage.setText(text);
            String version = (item.getPackageInfo().versionName == null) ?
                    "Not available" : item.getPackageInfo().versionName;
            text = "Version: " + version;
            holder.appVersion.setText(text);

            holder.name.setVisibility(View.VISIBLE);
            holder.memory.setVisibility(View.VISIBLE);
            holder.appPackage.setVisibility(View.VISIBLE);
            holder.appVersion.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(item.isChecked());

            holder.checkBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            item.setIsChecked(b);
                        }
                    }
            );

            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setUndoOn(boolean undoOn) {
        this.mUndoOn = undoOn;
    }

    public boolean isUndoOn() {
        return mUndoOn;
    }

    public void pendingRemoval(int position) {
        final Task item = mItems.get(position);
        if (!mItemsPendingRemoval.contains(item)) {
            mItemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(mItems.indexOf(item));
                }
            };
            mHandler.postDelayed(pendingRemovalRunnable, Config.PENDING_REMOVAL_TIMEOUT);
            mPendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        Task item = mItems.get(position);
        if (mItemsPendingRemoval.contains(item)) {
            mItemsPendingRemoval.remove(item);
        }
        if (mItems.contains(item)) {
            mItems.remove(position);
            notifyItemRemoved(position);
            /** Kill process here */
            mTaskController.killApp(item);
            EventBus.getDefault().post(new TaskRemovedEvent(position, item));
        }
    }

    public boolean isPendingRemoval(int position) {
        Task item;
        try {
            item = mItems.get(position);
        } catch (ArrayIndexOutOfBoundsException e) {
            item = null;
        }
        return mItemsPendingRemoval.contains(item);
    }

    public void swap(List<Task> list) {
        clear();
        if (mItems != null) {
            mItems.addAll(list);
        } else {
            mItems = list;
        }
        notifyDataSetChanged();
    }

    private void clear() {
        if (mItems != null) mItems.clear();
        if (mItemsPendingRemoval != null) mItemsPendingRemoval.clear();
        mPendingRunnables.clear();
    }
}
