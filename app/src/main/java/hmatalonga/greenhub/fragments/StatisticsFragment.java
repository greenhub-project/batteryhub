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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.data.BatteryUsage;
import hmatalonga.greenhub.util.DateUtils;
import hmatalonga.greenhub.util.StringHelper;
import io.realm.RealmResults;

import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * StatisticsFragment.
 */
public class StatisticsFragment extends Fragment {

    private static final String TAG = makeLogTag(StatisticsFragment.class);

    private GreenHubDb mDatabase;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        mDatabase = new GreenHubDb();

        // Battery Level
        LineChart chart = (LineChart) view.findViewById(R.id.batteryLevelChart);
        setup(chart, "Battery Level");
        chart.setData(loadData("Battery Level", ColorTemplate.rgb("#E84813")));
        chart.invalidate();

        // Battery Temperature
        chart = (LineChart) view.findViewById(R.id.batteryTemperatureChart);
        setup(chart, "Battery Temperature");
        chart.setData(loadData("Battery Temperature", ColorTemplate.rgb("#E81332")));
        chart.invalidate();

        // Battery Voltage
        chart = (LineChart) view.findViewById(R.id.batteryVoltageChart);
        setup(chart, "Battery Voltage");
        chart.setData(loadData("Battery Voltage", ColorTemplate.rgb("#FF15AC")));
        chart.invalidate();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDatabase.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDatabase.close();
    }

    private LineData loadData(String label, int color) {
        RealmResults<BatteryUsage> results = mDatabase.allUsages();
        List<Entry> entries = new ArrayList<>();

        switch (label) {
            case "Battery Level":
                for (BatteryUsage usage : results) {
                    entries.add(new Entry((float) usage.timestamp, usage.level));
                }
                break;
            case "Battery Temperature":
                for (BatteryUsage usage : results) {
                    entries.add(new Entry((float) usage.timestamp, (float) usage.details.batteryTemperature));
                }
                break;
            case "Battery Voltage":
                for (BatteryUsage usage : results) {
                    entries.add(new Entry((float) usage.timestamp, (float) usage.details.batteryVoltage));
                }
                break;
        }

        // add entries to dataset
        LineDataSet lineDataSet = new LineDataSet(entries, null);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1.8f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(color);

        return new LineData(lineDataSet);
    }

    private void setup(LineChart chart, final String label) {
        IAxisValueFormatter formatterX = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return DateUtils.ConvertMilliSecondsToFormattedDate((long) value);
            }
        };

        IAxisValueFormatter formatterY= new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                switch (label) {
                    case "Battery Level":
                        return StringHelper.formatPercentageNumber(value);
                    case "Battery Temperature":
                        return StringHelper.formatNumber(value) + " ºC";
                    case "Battery Voltage":
                        return StringHelper.formatNumber(value) + " V";
                    default:
                        return String.valueOf(value);
                }
            }
        };

        chart.getXAxis().setValueFormatter(formatterX);

        if (label.equals("Battery Level")) {
            chart.getAxisLeft().setAxisMaximum(1f);
        }

        chart.setExtraBottomOffset(5f);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setValueFormatter(formatterY);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setLabelCount(4);
        chart.getXAxis().setGranularity(1f);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);

        chart.animateY(3000, Easing.EasingOption.EaseInBack);
    }
}
