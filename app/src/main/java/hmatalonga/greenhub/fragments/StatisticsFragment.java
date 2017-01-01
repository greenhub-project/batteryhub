/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
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

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import hmatalonga.greenhub.R;
import hmatalonga.greenhub.models.data.BatteryUsage;
import hmatalonga.greenhub.models.ui.ChartCard;
import hmatalonga.greenhub.ui.MainActivity;
import hmatalonga.greenhub.ui.adapters.ChartRVAdapter;
import io.realm.RealmResults;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * StatisticsFragment.
 */
public class StatisticsFragment extends Fragment {

    private static final String TAG = makeLogTag(StatisticsFragment.class);

    private static final int INTERVAL_ALL = 1;

    private static final int INTERVAL_24H = 2;

    private static final int INTERVAL_5DAYS = 3;

    private MainActivity mActivity;

    private RecyclerView mRecyclerView;

    private ChartRVAdapter mAdapter;

    private ArrayList<ChartCard> mChartCards;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        mActivity = (MainActivity) getActivity();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);
        mAdapter = null;

        LinearLayoutManager layout = new LinearLayoutManager(view.getContext());

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        final BottomNavigationView bottomNavigationView =
                (BottomNavigationView) view.findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_24h:
                                return true;
                            case R.id.action_5days:
                                return true;
                            case R.id.action_everything:
                                return true;
                        }
                        return false;
                    }
                });

        loadData(INTERVAL_ALL);

        return view;
    }

    /**
     * Creates an array to feed data to the recyclerView
     */
    private void loadData(int interval) {
        RealmResults<BatteryUsage> results;
        ChartCard card;
        mChartCards = new ArrayList<>();

        if (interval == INTERVAL_ALL) {
            results = mActivity.database.allUsages();
        } else if (interval == INTERVAL_24H) {
            results = mActivity.database.betweenUsages(null, null);
        } else if (interval == INTERVAL_5DAYS) {
            results = mActivity.database.betweenUsages(null, null);
        } else {
            results = mActivity.database.allUsages();
        }

        // Battery Level
        card = new ChartCard(
                ChartRVAdapter.BATTERY_LEVEL,
                "Battery Level (%)",
                ColorTemplate.rgb("#E84813")
        );
        for (BatteryUsage usage : results) {
            card.entries.add(new Entry((float) usage.timestamp, usage.level));
        }
        mChartCards.add(card);

        // Battery Temperature
        card = new ChartCard(
                ChartRVAdapter.BATTERY_TEMPERATURE,
                "Battery Temperature (ºC)",
                ColorTemplate.rgb("#E81332")
        );
        for (BatteryUsage usage : results) {
            card.entries.add(new Entry((float) usage.timestamp, (float) usage.details.batteryTemperature));
        }
        mChartCards.add(card);

        // Battery Voltage
        card = new ChartCard(
                ChartRVAdapter.BATTERY_VOLTAGE,
                "Battery Voltage (V)",
                ColorTemplate.rgb("#FF15AC")
        );
        for (BatteryUsage usage : results) {
            card.entries.add(new Entry((float) usage.timestamp, (float) usage.details.batteryVoltage));
        }
        mChartCards.add(card);

        setAdapter();
    }

    private void setAdapter(){
        if (mAdapter == null) {
            mAdapter = new ChartRVAdapter(mChartCards);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.swap(mChartCards);
        }
        mRecyclerView.invalidate();
    }
}
