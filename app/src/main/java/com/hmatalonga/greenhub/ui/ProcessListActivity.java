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

package com.hmatalonga.greenhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.List;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.ui.AppListItem;
import com.hmatalonga.greenhub.ui.adapters.ProcessInfoAdapter;
import com.hmatalonga.greenhub.util.GreenHubHelper;

public class ProcessListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private ProcessInfoAdapter mAdapter;

    private ArrayList<AppListItem> mAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mAdapter = null;

        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        final Context context = getApplicationContext();

        mAppList = new ArrayList<>();
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();

        for (AndroidAppProcess proc : processes) {
            mAppList.add(new AppListItem(
                    GreenHubHelper.iconForApp(context, proc.name),
                    GreenHubHelper.labelForApp(context, proc.name)
            ));
        }

        setAdapter();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new ProcessInfoAdapter(mAppList);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.swap(mAppList);
        }
        mRecyclerView.invalidate();
    }
}
