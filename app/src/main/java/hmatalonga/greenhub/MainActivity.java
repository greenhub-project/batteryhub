package hmatalonga.greenhub;

import android.content.Intent;
import android.content.IntentFilter;
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
import hmatalonga.greenhub.sampling.BatteryEstimator;
import hmatalonga.greenhub.tasks.RegisterDeviceTask;
import hmatalonga.greenhub.utils.FontManager;

public class MainActivity extends AppCompatActivity {
    private static GreenHub app = null;
    private static BatteryEstimator estimator = null;

    private ViewPager viewPager;
    private ActionBar actionBar;
    private int currentToolbarTitle = R.string.app_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GreenHub.setMain(this);

        // Configure View and Layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(currentToolbarTitle);

        // Initialize Application instance
        app = new GreenHub(getApplicationContext());
        app.initModules();

        // TODO: Create default xml preferences file
        // PreferenceManager.setDefaultValues();

        // Initialize fragments content

        startReceivers();

        // Run tasks
        new RegisterDeviceTask().execute(app);

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

        return super.onOptionsItemSelected(item);
    }

    public void startReceivers() {
        new Thread() {
            private IntentFilter intentFilter;

            public void run() {
                // Let sampling happen on battery change
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

                estimator = BatteryEstimator.getInstance();
                try {
                    unregisterReceiver(estimator);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                registerReceiver(estimator, intentFilter);

                // for the debugging purpose, let's comment out these actions
                // TODO: re-enable
                // intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                // registerReceiver(sampler, intentFilter);
                // intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                // registerReceiver(sampler, intentFilter);
            }
        }.start();
    }

    private void setupTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        assert tabLayout != null;

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_account_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_information_white_24dp));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), app,
                tabLayout.getTabCount());

        viewPager = (ViewPager) findViewById(R.id.pager);
        assert viewPager != null;

        try {
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            currentToolbarTitle = R.string.title_fragment_home;
                            break;
                        case 1:
                            currentToolbarTitle = R.string.title_fragment_device;
                            break;
                        case 2:
                            currentToolbarTitle = R.string.title_fragment_about;
                            break;
                        default:
                            currentToolbarTitle = R.string.app_name;
                    }
                    viewPager.setCurrentItem(tab.getPosition());
                    actionBar.setTitle(currentToolbarTitle);
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
}
