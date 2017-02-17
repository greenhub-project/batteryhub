/*
 * Copyright (c) 2015 Google Inc. All rights reserved.
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

package com.hmatalonga.greenhub.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.fragments.TosFragment;
import com.hmatalonga.greenhub.fragments.WelcomeFragment;

import static com.hmatalonga.greenhub.util.LogUtils.LOGD;
import static com.hmatalonga.greenhub.util.LogUtils.makeLogTag;

/**
 * Terms of Service activity activated via
 * {@link hmatalonga.greenhub.ui.BaseActivity} functionality.
 */
public class WelcomeActivity extends AppCompatActivity implements WelcomeFragment.WelcomeFragmentContainer {

    private static final String TAG = makeLogTag(WelcomeActivity.class);
    WelcomeActivityContent mContentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        mContentFragment = getCurrentFragment(this);

        // If there's no fragment to use, we're done here.
        if (mContentFragment == null) {
            finish();
        }

        // Wire up the fragment
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.welcome_content, (Fragment) mContentFragment);
        fragmentTransaction.commit();

        LOGD(TAG, "Inside Create View.");
    }

    /**
     * Get the current fragment to display.
     *
     * This is the first fragment in the list that WelcomeActivityContent.shouldDisplay().
     *
     * @param context the application context.
     * @return the WelcomeActivityContent to display or null if there's none.
     */
    private static WelcomeActivityContent getCurrentFragment(Context context) {
        List<WelcomeActivityContent> welcomeActivityContents = getWelcomeFragments();

        for (WelcomeActivityContent fragment : welcomeActivityContents) {
            if (fragment.shouldDisplay(context)) {
                return fragment;
            }
        }

        return null;
    }

    /**
     * Whether to display the WelcomeActivity.
     *
     * Decided whether any of the fragments need to be displayed.
     *
     * @param context the application context.
     * @return true if the activity should be displayed.
     */
    public static boolean shouldDisplay(Context context) {
        WelcomeActivityContent fragment = getCurrentFragment(context);
        return fragment != null;
    }

    /**
     * Get all WelcomeFragments for the WelcomeActivity.
     *
     * @return the List of WelcomeFragments.
     */
    private static List<WelcomeActivityContent> getWelcomeFragments() {
        /**
         * Despite being just one fragment, returns an ArrayList.
         * It makes it easier add new fragments in future versions
         *
         * Use Arrays.asList() if there is more than one fragment
         */
        return new ArrayList<WelcomeActivityContent>(Collections.singletonList(
                new TosFragment()
        ));
    }

    @Override
    public Button getPositiveButton() {
        return (Button) findViewById(R.id.button_accept);
    }

    @Override
    public void setPositiveButtonEnabled(Boolean enabled) {
        try {
            getPositiveButton().setEnabled(enabled);
        } catch (NullPointerException e) {
            LOGD(TAG, "Positive welcome button doesn't exist to set enabled.");
        }
    }

    @Override
    public Button getNegativeButton() {
        return (Button) findViewById(R.id.button_decline);
    }

    @Override
    public void setNegativeButtonEnabled(Boolean enabled) {
        try {
            getNegativeButton().setEnabled(enabled);
        } catch (NullPointerException e) {
            LOGD(TAG, "Negative welcome button doesn't exist to set enabled.");
        }
    }

    /**
     * The definition of a Fragment for a use in the WelcomeActivity.
     */
    public interface WelcomeActivityContent {
        /**
         * Whether the fragment should be displayed.
         *
         * @param context the application context.
         * @return true if the WelcomeActivityContent should be displayed.
         */
        boolean shouldDisplay(Context context);
    }
}
