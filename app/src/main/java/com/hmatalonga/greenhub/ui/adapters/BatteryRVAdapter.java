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

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.ui.BatteryCard;

/**
 * RecyclerView Adapter Class
 *
 * Created by hugo on 05-04-2016.
 */
public class BatteryRVAdapter extends RecyclerView.Adapter<BatteryRVAdapter.DashboardViewHolder> {

    static class DashboardViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        public ImageView icon;
        public TextView label;
        public TextView value;
        public View indicator;

        DashboardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            icon = itemView.findViewById(R.id.icon);
            label = itemView.findViewById(R.id.label);
            value = itemView.findViewById(R.id.value);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }

    private ArrayList<BatteryCard> mBatteryCards;

    public BatteryRVAdapter(ArrayList<BatteryCard> batteryCards){
        this.mBatteryCards = batteryCards;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.battery_item_card_view,
                viewGroup,
                false
        );
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder viewHolder, int i) {
        viewHolder.icon.setImageResource(mBatteryCards.get(i).icon);
        viewHolder.label.setText(mBatteryCards.get(i).label);
        viewHolder.value.setText(mBatteryCards.get(i).value);
        viewHolder.indicator.setBackgroundColor(mBatteryCards.get(i).indicator);
    }

    @Override
    public int getItemCount() {
        return mBatteryCards.size();
    }

    public void swap(ArrayList<BatteryCard> list){
        if (mBatteryCards != null) {
            mBatteryCards.clear();
            mBatteryCards.addAll(list);
        }
        else {
            mBatteryCards = list;
        }
        notifyDataSetChanged();
    }
}
