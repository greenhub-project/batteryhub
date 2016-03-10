package hmatalonga.greenhub.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hmatalonga.greenhub.R;

/**
 * Created by hugo on 07-03-2016.
 */
public class TabFragmentActions extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_fragment_actions, container, false);
    }
}
