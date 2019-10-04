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

package com.hmatalonga.greenhub.ui.layouts;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import com.hmatalonga.greenhub.R;

/**
 * Tab Layout for Main Activity.
 */
public class MainTabLayout extends TabLayout {

    public MainTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MainTabLayout(Context context) {
        super(context);
    }

    public MainTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates the tabs for the layout.
     */
    public void createTabs() {
        addTab(R.drawable.ic_home_white_24dp, R.string.title_fragment_home);
        addTab(R.drawable.ic_cellphone_android_white_24dp, R.string.title_fragment_device);
        addTab(R.drawable.ic_chart_areaspline_white_24dp, R.string.title_fragment_stats);
        // addTab(R.drawable.ic_history_white_24dp, R.string.title_fragment_history);
        // addTab(R.drawable.ic_information_white_24dp, R.string.title_fragment_about);
    }

    /**
     * Adds a new tab to the layout provided the icon and string description resources.
     *
     * @param iconId Icon Id resource
     * @param contentDescriptionId Content Description Id resource
     */
    private void addTab(@DrawableRes int iconId, @StringRes int contentDescriptionId) {
        addTab(newTab()
                .setIcon(iconId)
                .setContentDescription(contentDescriptionId));
    }
}
