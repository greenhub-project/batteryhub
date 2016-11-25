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

import java.util.ArrayList;

import hmatalonga.greenhub.GreenHubApp;
import hmatalonga.greenhub.util.GreenHubHelper;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.DataEstimator;
import hmatalonga.greenhub.models.ui.DeviceResourceCard;

/**
 * Home Fragment.
 */
public class HomeFragment extends Fragment {

    private Context mContext;
    private RecyclerView mRecyclerView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = view.getContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabSendSample);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        GridLayoutManager layout = new GridLayoutManager(
                mContext,
                1,
                GridLayoutManager.VERTICAL,
                false
        );
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Set all values for layout view elements
     */
    private void populateView() {
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
