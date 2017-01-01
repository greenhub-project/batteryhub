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

package hmatalonga.greenhub.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import hmatalonga.greenhub.GreenHubApp;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.ui.adapters.TabAdapter;
import hmatalonga.greenhub.ui.layouts.MainTabLayout;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        database.getDefaultInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        database.close();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {}

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_summary:
                startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY));
                return true;
        }

        return false;
    }

    private void loadComponents() {
        database = new GreenHubDb();

        mApp = (GreenHubApp) getApplication();

        loadViews();

        // Run tasks --
        // Register device on the web server
        // new RegisterDeviceTask().execute(sApp);
    }

    private void loadViews() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(TabAdapter.NUM_TABS - 1);

        final TabAdapter mTabAdapter = new TabAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabAdapter);

        MainTabLayout mTabLayout = (MainTabLayout) findViewById(R.id.tab_layout);
        mTabLayout.createTabs();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSendSample);
        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = database.allUsages().size();
                Snackbar.make(view, "Based on " + count + " usages.", Snackbar.LENGTH_SHORT).show();
            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                getActionBarToolbar().setTitle(tab.getContentDescription());
                if (tab.getPosition() == 0) {
                    fab.show();
                } else {
                    fab.hide();
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

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }

    public DataEstimator getEstimator() {
        return mApp.estimator;
    }
}
