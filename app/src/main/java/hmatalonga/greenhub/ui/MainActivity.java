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
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import hmatalonga.greenhub.GreenHubApp;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.ui.adapters.TabAdapter;
import hmatalonga.greenhub.ui.layouts.MainTabLayout;
import hmatalonga.greenhub.util.FontManager;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    private static final String TAG = makeLogTag(MainActivity.class);

    private ViewPager mViewPager;
    private MainTabLayout mTabLayout;
    private TabAdapter mTabAdapter;
    private GreenHubApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadComponents();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

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

    @Override
    protected void onResume() {
        //sApp.startReceivers();
        // update status
        // refresh UI
        // Toast.makeText(getApplicationContext(), "App resumed", Toast.LENGTH_LONG).show();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // sApp.stopReceivers();
        // Inspector.resetRunningProcessInfo();
        // Toast.makeText(getApplicationContext(), "App paused", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void loadComponents() {
        mApp = (GreenHubApp) getApplication();

        loadViews();

        // Initialize Application instance
//        sApp = new GreenHubHelper(getApplicationContext());
//        sApp.initModules();

        // TODO: Create default xml preferences file
        // TODO: Create a chart menu with temp, voltage and battery level
        // PreferenceManager.setDefaultValues();

        // Initialize fragments content
//        HomeFragment.setApp(sApp);

        // Run tasks --
        // Register device on the web server
//        new RegisterDeviceTask().execute(sApp);
    }

    private void loadViews() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(TabAdapter.NUM_TABS - 1);

        mTabAdapter = new TabAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabAdapter);

        mTabLayout = (MainTabLayout) findViewById(R.id.tab_layout);
        mTabLayout.createTabs();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                getActionBarToolbar().setTitle(tab.getContentDescription());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //  nop
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //scroll the active fragment's contents to the top when user taps the current tab
                // Fragment fragment = mTabAdapter.getFragment(tab.getPosition());
                // Load here??
            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
    }

    private void setupFont(View view) {
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(view, iconFont);
    }
}
