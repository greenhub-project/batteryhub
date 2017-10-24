/*
 * Copyright (C) 2016 Hugo Matalonga & João Paulo Fernandes
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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hmatalonga.greenhub.Config;
import com.hmatalonga.greenhub.GreenHubApp;
import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.RefreshChartEvent;
import com.hmatalonga.greenhub.events.StatusEvent;
import com.hmatalonga.greenhub.managers.sampling.DataEstimator;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.Specifications;
import com.hmatalonga.greenhub.models.data.Device;
import com.hmatalonga.greenhub.network.CommunicationManager;
import com.hmatalonga.greenhub.tasks.CheckNewMessagesTask;
import com.hmatalonga.greenhub.tasks.ServerStatusTask;
import com.hmatalonga.greenhub.ui.adapters.TabAdapter;
import com.hmatalonga.greenhub.ui.layouts.MainTabLayout;
import com.hmatalonga.greenhub.util.NetworkWatcher;
import com.hmatalonga.greenhub.util.SettingsUtils;

import org.greenrobot.eventbus.EventBus;

import static com.hmatalonga.greenhub.util.LogUtils.LOGI;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = makeLogTag(MainActivity.class);

    private GreenHubApp mApp;

    private ViewPager mViewPager;

    public GreenHubDb database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LOGI(TAG, "onCreate() called");

        loadComponents();

        Intent intentFromNotifier = getIntent();

        if (intentFromNotifier != null) {
            int tab = intentFromNotifier.getIntExtra("tab", -1);
            if (tab != -1) {
                mViewPager.setCurrentItem(tab);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        database.getDefaultInstance();
    }

    @Override
    protected void onStop() {
        database.close();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add the search button to the toolbar.
        Toolbar toolbar = getActionBarToolbar();
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_inbox:
                startActivity(new Intent(this, InboxActivity.class));
                return true;
            case R.id.action_summary:
                try {
                    Intent powerSummary = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                    ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerSummary, 0);
                    if (resolveInfo != null) {
                        startActivity(powerSummary);
                    }
                    // TODO: else show dialog
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_rating:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.hmatalonga.greenhub")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=com.hmatalonga.greenhub")));
                }
                return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Config.PERMISSION_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                setupPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Config.PERMISSION_ACCESS_COARSE_LOCATION);
                return;
            }
            case Config.PERMISSION_ACCESS_COARSE_LOCATION: {
                setupPermission(Manifest.permission.ACCESS_FINE_LOCATION, Config.PERMISSION_ACCESS_FINE_LOCATION);
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public DataEstimator getEstimator() {
        return mApp.estimator;
    }

    private void loadComponents() {
        final Context context = getApplicationContext();

        database = new GreenHubDb();
        mApp = (GreenHubApp) getApplication();

        // Check if Service needs to start, in case it is coming from WelcomeActivity
        if (SettingsUtils.isTosAccepted(context)) {
            mApp.startGreenHubService();

            // if there is Internet connection run tasks
            if (NetworkWatcher.hasInternet(context, NetworkWatcher.BACKGROUND_TASKS)) {
                // Fetch web server status and update them
                new ServerStatusTask().execute(context);

                if (SettingsUtils.isDeviceRegistered(context)) {
                    new CheckNewMessagesTask().execute(context);
                }
            }
        }

        // Load app views
        loadViews();
    }

    private void loadViews() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(TabAdapter.NUM_TABS - 1);

        final TabAdapter tabAdapter = new TabAdapter(getFragmentManager());
        mViewPager.setAdapter(tabAdapter);

        MainTabLayout tabLayout = (MainTabLayout) findViewById(R.id.tab_layout);
        tabLayout.createTabs();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSendSample);
        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();

                // Check Internet connectivity
                if (!NetworkWatcher.hasInternet(context, NetworkWatcher.COMMUNICATION_MANAGER)) {
                    Snackbar.make(
                            view,
                            getString(R.string.event_no_connectivity),
                            Snackbar.LENGTH_LONG
                    ).show();
                    CommunicationManager.isQueued = true;
                    return;
                }

                // Check if server url is stored in preferences and device is registered
                if (!SettingsUtils.isServerUrlPresent(context) || !SettingsUtils.isDeviceRegistered(context)) {
                    // Fetch web server status and update them
                    new ServerStatusTask().execute(context);
                    EventBus.getDefault().post(new StatusEvent(getString(R.string.event_needs_sync)));
                    refreshStatus();
                    return;
                }

                // Upload samples
                CommunicationManager manager = new CommunicationManager(context, false);

                // Check if is already uploading
                if (!CommunicationManager.isUploading) {
                    manager.sendSamples();
                } else {
                    EventBus.getDefault().post(new StatusEvent(getString(R.string.event_upload_running)));
                    refreshStatus();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                getActionBarToolbar().setTitle(tab.getContentDescription());
                if (tab.getPosition() == TabAdapter.TAB_HOME) {
                    fab.show();
                } else {
                    fab.hide();
                }

                if (tab.getPosition() == TabAdapter.TAB_CHARTS) {
                    EventBus.getDefault().post(new RefreshChartEvent());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //  nop
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //scroll the active fragment's contents to the top when user taps the current tab
            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // If device has Android version < 6.0 don't request permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        /**
         * Ask for necessary permissions on run-time
         * Permissions with a protection level above Normal need to be requested
         */
        setupPermission(Manifest.permission.READ_PHONE_STATE, Config.PERMISSION_READ_PHONE_STATE);
    }

    private void refreshStatus() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new StatusEvent(getString(R.string.event_idle)));
            }
        }, Config.REFRESH_STATUS_ERROR);
    }

    private void setupPermission(String permission, int code) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    code);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }
}
