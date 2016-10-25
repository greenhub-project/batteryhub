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
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.tasks.RegisterDeviceTask;
import hmatalonga.greenhub.ui.adapters.PagerAdapter;
import hmatalonga.greenhub.util.FontManager;

public class MainActivity extends BaseActivity {
    private static GreenHub sApp = null;
    private ViewPager mViewPager;
    private ActionBar mActionBar;

    private int mCurrentToolbarTitle = R.string.app_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the Main Activity instance on the App class
        GreenHub.setMain(this);

        // Configure View and Layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        assert mActionBar != null;
        mActionBar.setTitle(mCurrentToolbarTitle);

        // Initialize Application instance
//        sApp = new GreenHub(getApplicationContext());
//        sApp.initModules();

        // TODO: Create default xml preferences file
        // TODO: Create a chart menu with temp, voltage and battery level
        // PreferenceManager.setDefaultValues();

        // Initialize fragments content
//        HomeFragment.setApp(sApp);

        // Run tasks --
        HomeFragment.setStatus("Stopped");
        // Register device on the web server
//        new RegisterDeviceTask().execute(sApp);

//        setupTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
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
        //sApp.stopReceivers();
        Inspector.resetRunningProcessInfo();
        // Toast.makeText(getApplicationContext(), "App paused", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    private void setupTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        assert tabLayout != null;

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_account_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_information_white_24dp));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        assert mViewPager != null;

        try {
            mViewPager.setAdapter(adapter);
            mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            mCurrentToolbarTitle = R.string.title_fragment_home;
                            break;
                        case 1:
                            mCurrentToolbarTitle = R.string.title_fragment_device;
                            break;
                        case 2:
                            mCurrentToolbarTitle = R.string.title_fragment_about;
                            break;
                        default:
                            mCurrentToolbarTitle = R.string.app_name;
                    }
                    mViewPager.setCurrentItem(tab.getPosition());
                    mActionBar.setTitle(mCurrentToolbarTitle);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setupFont(View view) {
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(view, iconFont);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}
