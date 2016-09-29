package hmatalonga.greenhub;

import android.content.Context;
import android.content.Intent;
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

import hmatalonga.greenhub.adapters.PagerAdapter;
import hmatalonga.greenhub.fragments.HomeFragment;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.tasks.RegisterDeviceTask;
import hmatalonga.greenhub.utils.FontManager;

public class MainActivity extends AppCompatActivity {
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
        sApp = new GreenHub(getApplicationContext());
        sApp.initModules();

        // TODO: Create default xml preferences file
        // TODO: Create a chart menu with temp, voltage and battery level
        // PreferenceManager.setDefaultValues();

        // Initialize fragments content
        HomeFragment.setApp(sApp);

        // Run tasks --
        HomeFragment.setStatus("Registering Device...");
        // Register device on the web server
        new RegisterDeviceTask().execute(sApp);

        setupTabs();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_summary) {
            Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        sApp.startReceivers();
        // update status
        // refresh UI
        // Toast.makeText(getApplicationContext(), "App resumed", Toast.LENGTH_LONG).show();
        super.onResume();
    }

    @Override
    protected void onPause() {
        sApp.stopReceivers();
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
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

    /**
     *
     * @param view
     */
    private void setupFont(View view) {
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(view, iconFont);
    }
}
