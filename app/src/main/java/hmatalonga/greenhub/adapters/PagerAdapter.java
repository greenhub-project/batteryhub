package hmatalonga.greenhub.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.fragments.AboutFragment;
import hmatalonga.greenhub.fragments.DeviceFragment;
import hmatalonga.greenhub.fragments.HomeFragment;

/**
 * Created by hugo on 27-03-2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    private GreenHub app = null;
    private int mNumOfTabs;

    private HomeFragment homeFragment;
    private DeviceFragment deviceFragment;
    private AboutFragment aboutFragment;

    public PagerAdapter(FragmentManager fm, GreenHub app, int NumOfTabs) {
        super(fm);
        this.app = app;
        this.mNumOfTabs = NumOfTabs;

        // Load content for fragments if necessary
        // like process info and memory
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                homeFragment = new HomeFragment();
                homeFragment.setApp(app);
                return homeFragment;
            case 1:
                return new DeviceFragment();
            case 2:
                return new AboutFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}