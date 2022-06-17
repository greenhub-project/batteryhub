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

package com.hmatalonga.greenhub.models.ui;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * ChartCard.
 */
public class ChartCard {

    private static final String TAG = "ChartCard";

    public int type;
    public String label;
    public int color;
    public List<Entry> entries;
    public double[] extras;

    public ChartCard(int type, String label, int color) {
        this.type = type;
        this.label = label;
        this.color = color;
        this.entries = new ArrayList<>();
        this.extras = null;
    }
}
