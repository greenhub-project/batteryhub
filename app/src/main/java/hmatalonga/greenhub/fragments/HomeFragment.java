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

package hmatalonga.greenhub.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import hmatalonga.greenhub.GreenHubApp;
import hmatalonga.greenhub.events.BatteryLevelEvent;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.util.GreenHubHelper;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.models.ui.DeviceResourceCard;
import io.realm.Realm;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Home Fragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = makeLogTag("HomeFragment");

    private Realm mRealm;

    private Context mContext;

    private RecyclerView mRecyclerView;

    private TextView mBatteryCurrentValue;

    private ProgressBar mBatteryCircleBar;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = view.getContext();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabSendSample);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);

        GridLayoutManager layout = new GridLayoutManager(
                mContext,
                1,
                GridLayoutManager.VERTICAL,
                false
        );
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        mBatteryCurrentValue = (TextView) view.findViewById(R.id.batteryCurrentValue);
        mBatteryCircleBar = (ProgressBar) view.findViewById(R.id.batteryProgressbar);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mRealm.close();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateBatteryLevelUI(BatteryLevelEvent event) {
        String text = "" + event.level;
        mBatteryCurrentValue.setText(text);
        mBatteryCircleBar.setProgress(event.level);
    }

    /**
     * Creates an array to feed data to the recyclerView
     *
     * @param context Application context
     * @param estimator Provider of mobile status
     */
    private void loadData(final Context context, final DataEstimator estimator) {
    }

    /**
     *
     */
    private void setAdapter(){
    }
}
