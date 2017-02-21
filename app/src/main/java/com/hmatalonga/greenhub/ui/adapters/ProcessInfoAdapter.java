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

package com.hmatalonga.greenhub.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.ui.AppListItem;

public class ProcessInfoAdapter extends RecyclerView.Adapter<ProcessInfoAdapter.DashboardViewHolder> {

    static class DashboardViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView name;

        DashboardViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            name = (TextView) itemView.findViewById(R.id.app_name);
        }
    }

    private ArrayList<AppListItem> mAppList;

    public ProcessInfoAdapter(ArrayList<AppListItem> items){
        this.mAppList = items;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.app_list_item_view,
                viewGroup,
                false
        );
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder viewHolder, int i) {
        viewHolder.icon.setImageDrawable(mAppList.get(i).icon);
        viewHolder.name.setText(mAppList.get(i).name);
    }

    @Override
    public int getItemCount() {
        return mAppList.size();
    }

    public void swap(ArrayList<AppListItem> list){
        if (mAppList != null) {
            mAppList.clear();
            mAppList.addAll(list);
        }
        else {
            mAppList = list;
        }
        notifyDataSetChanged();
    }
}