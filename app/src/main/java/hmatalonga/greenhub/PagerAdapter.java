package hmatalonga.greenhub;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import hmatalonga.greenhub.fragments.AboutFragment;
import hmatalonga.greenhub.fragments.DeviceFragment;
import hmatalonga.greenhub.fragments.HomeFragment;

/**
 * Created by hugo on 27-03-2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
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