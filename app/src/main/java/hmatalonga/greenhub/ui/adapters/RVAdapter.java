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

package hmatalonga.greenhub.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import hmatalonga.greenhub.R;
import hmatalonga.greenhub.models.DeviceResourceCard;

/**
 * RecyclerView Adapter Class
 *
 * Created by hugo on 05-04-2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DashboardViewHolder> {

    static class DashboardViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        public TextView title;
        public TextView value;

        DashboardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            title = (TextView) itemView.findViewById(R.id.dashboard_title);
            value = (TextView) itemView.findViewById(R.id.dashboard_value);
        }
    }

    private ArrayList<DeviceResourceCard> mDeviceResourceCards;

    public RVAdapter(ArrayList<DeviceResourceCard> deviceResourceCards){
        mDeviceResourceCards = deviceResourceCards;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dashboard_item,
                viewGroup, false);
        return new DashboardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder personViewHolder, int i) {
        personViewHolder.title.setText(mDeviceResourceCards.get(i).title);
        personViewHolder.value.setText(mDeviceResourceCards.get(i).value);
    }

    @Override
    public int getItemCount() {
        return mDeviceResourceCards.size();
    }
}
