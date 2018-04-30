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

package com.hmatalonga.greenhub.ui.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.ui.ChartCard;
import com.hmatalonga.greenhub.ui.views.ChartMarkerView;
import com.hmatalonga.greenhub.util.DateUtils;
import com.hmatalonga.greenhub.util.StringHelper;

/**
 * ChartRVAdapter.
 */
public class ChartRVAdapter extends RecyclerView.Adapter<ChartRVAdapter.DashboardViewHolder> {

    public static final int BATTERY_LEVEL = 1;
    public static final int BATTERY_TEMPERATURE = 2;
    public static final int BATTERY_VOLTAGE = 3;

    private ArrayList<ChartCard> mChartCards;

    private int mInterval;

    private Context mContext;

    static class DashboardViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView label;
        TextView interval;
        LineChart chart;
        RelativeLayout extras;
        TextView min;
        TextView avg;
        TextView max;

        DashboardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            label = itemView.findViewById(R.id.label);
            interval = itemView.findViewById(R.id.interval);
            chart = itemView.findViewById(R.id.chart);
            extras = itemView.findViewById(R.id.extra_details);
            min = itemView.findViewById(R.id.minValue);
            avg = itemView.findViewById(R.id.avgValue);
            max = itemView.findViewById(R.id.maxValue);
        }
    }

    public ChartRVAdapter(ArrayList<ChartCard> chartCards, int interval, Context context) {
        this.mChartCards = chartCards;
        this.mInterval = interval;
        this.mContext = context;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ChartRVAdapter.DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.chart_card_view,
                parent,
                false
        );
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder holder, int position) {
        ChartCard card = mChartCards.get(position);
        setup(holder, card);
        holder.chart.setData(loadData(card));
        holder.chart.invalidate();
        holder.label.setText(card.label);

        if (card.extras != null) {
            String value;
            if (card.type == BATTERY_TEMPERATURE) {
                value = "Min: " + StringHelper.formatNumber(card.extras[0]) + " ºC";
                holder.min.setText(value);
                value = mContext.getString(R.string.chart_average_title) +
                        ": " + StringHelper.formatNumber(card.extras[1]) + " ºC";
                holder.avg.setText(value);
                value = "Max: " + StringHelper.formatNumber(card.extras[2]) + " ºC";
                holder.max.setText(value);
            } else if (card.type == BATTERY_VOLTAGE) {
                value = "Min: " + StringHelper.formatNumber(card.extras[0]) + " V";
                holder.min.setText(value);
                value = mContext.getString(R.string.chart_average_title) +
                        ": " + StringHelper.formatNumber(card.extras[1]) + " V";
                holder.avg.setText(value);
                value = "Max: " + StringHelper.formatNumber(card.extras[2]) + " V";
                holder.max.setText(value);
            }
            holder.extras.setVisibility(View.VISIBLE);
        }

        if (mInterval == DateUtils.INTERVAL_24H) {
            holder.interval.setText("Last 24h");
        } else if (mInterval == DateUtils.INTERVAL_3DAYS) {
            holder.interval.setText("Last 3 days");
        } else if (mInterval == DateUtils.INTERVAL_5DAYS) {
            holder.interval.setText("Last 5 days");
        }
    }

    @Override
    public int getItemCount() {
        return mChartCards.size();
    }

    public void swap(ArrayList<ChartCard> list) {
        if (mChartCards != null) {
            mChartCards.clear();
            mChartCards.addAll(list);
        } else {
            mChartCards = list;
        }
        notifyDataSetChanged();
    }

    private LineData loadData(ChartCard card) {
        // add entries to dataset
        LineDataSet lineDataSet = new LineDataSet(card.entries, null);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setColor(card.color);
        lineDataSet.setCircleColor(card.color);
        lineDataSet.setLineWidth(1.8f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(card.color);

        return new LineData(lineDataSet);
    }

    private void setup(DashboardViewHolder holder, final ChartCard card) {
        IAxisValueFormatter formatterX = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateUtils.convertMilliSecondsToFormattedDate((long) value);
            }
        };

        IAxisValueFormatter formatterY = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                switch (card.type) {
                    case BATTERY_LEVEL:
                        return StringHelper.formatPercentageNumber(value);
                    case BATTERY_TEMPERATURE:
                        return StringHelper.formatNumber(value) + " ºC";
                    case BATTERY_VOLTAGE:
                        return StringHelper.formatNumber(value) + " V";
                    default:
                        return String.valueOf(value);
                }
            }
        };

        holder.chart.getXAxis().setValueFormatter(formatterX);

        if (card.type == BATTERY_LEVEL) {
            holder.chart.getAxisLeft().setAxisMaximum(1f);
        }

        holder.chart.setExtraBottomOffset(5f);
        holder.chart.getAxisLeft().setDrawGridLines(false);
        holder.chart.getAxisLeft().setValueFormatter(formatterY);
        holder.chart.getAxisRight().setDrawGridLines(false);
        holder.chart.getAxisRight().setDrawLabels(false);
        holder.chart.getXAxis().setDrawGridLines(false);
        holder.chart.getXAxis().setLabelCount(3);
        holder.chart.getXAxis().setGranularity(1f);

        holder.chart.getLegend().setEnabled(false);
        holder.chart.getDescription().setEnabled(false);
        holder.chart.setNoDataText(mContext.getString(R.string.chart_loading_data));

        IMarker marker = new ChartMarkerView(
                holder.itemView.getContext(), R.layout.item_marker, card.type
        );

        holder.chart.setMarker(marker);

        holder.chart.animateY(600, Easing.EasingOption.Linear);
    }
}
