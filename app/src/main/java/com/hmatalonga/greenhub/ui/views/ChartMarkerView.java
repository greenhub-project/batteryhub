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

package com.hmatalonga.greenhub.ui.views;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.ui.adapters.ChartRVAdapter;
import com.hmatalonga.greenhub.util.StringHelper;

/**
 * ChartMarkerView.
 */
public class ChartMarkerView extends MarkerView {

    private static final String TAG = "ChartMarkerView";

    private TextView mContent;
    private MPPointF mOffset;
    private int mType;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        mContent = findViewById(R.id.tvContent);
    }

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context, int layoutResource, int type) {
        super(context, layoutResource);

        mType = type;
        mContent = findViewById(R.id.tvContent);
    }


    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String value = "";

        switch (mType) {
            case ChartRVAdapter.BATTERY_LEVEL:
                value = StringHelper.formatPercentageNumber(e.getY());
                break;
            case ChartRVAdapter.BATTERY_TEMPERATURE:
                value = StringHelper.formatNumber(e.getY()) + " ºC";
                break;
            case ChartRVAdapter.BATTERY_VOLTAGE:
                value = StringHelper.formatNumber(e.getY()) + " V";
                break;
        }

        mContent.setText(value);

        // this will perform necessary layouting
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }
}
