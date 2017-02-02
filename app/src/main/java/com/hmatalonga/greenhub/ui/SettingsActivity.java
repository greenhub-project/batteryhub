/*
 * Copyright (c) 2017 Hugo Matalonga & João Paulo Fernandes
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

package hmatalonga.greenhub.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import hmatalonga.greenhub.BuildConfig;
import hmatalonga.greenhub.GreenHubApp;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.tasks.DeleteSessionsTask;
import hmatalonga.greenhub.tasks.DeleteUsagesTask;
import hmatalonga.greenhub.util.Notifier;
import hmatalonga.greenhub.util.SettingsUtils;

import static hmatalonga.greenhub.util.LogUtils.LOGI;
import static hmatalonga.greenhub.util.LogUtils.makeLogTag;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = makeLogTag(SettingsActivity.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public SettingsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            findPreference(SettingsUtils.PREF_APP_VERSION).setSummary(BuildConfig.VERSION_NAME);

            bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_DATA_HISTORY));
            bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_UPLOAD_RATE));
            bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_NOTIFICATIONS_PRIORITY));

            SettingsUtils.registerOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SettingsUtils.unregisterOnSharedPreferenceChangeListener(getActivity(), this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            final Context context = getActivity().getApplicationContext();

            switch (key) {
                case SettingsUtils.PREF_SAMPLING_SCREEN:
                    GreenHubApp app = (GreenHubApp) getActivity().getApplication();
                    // Restart GreenHub Service with new settings
                    LOGI(TAG, "Restarting GreenHub Service because of preference changes");
                    app.stopGreenHubService();
                    app.startGreenHubService();
                    break;
                case SettingsUtils.PREF_DATA_HISTORY:
                    bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_DATA_HISTORY));
                    // Delete old data history
                    final int interval = SettingsUtils.fetchDataHistoryInterval(context);
                    new DeleteUsagesTask().execute(interval);
                    new DeleteSessionsTask().execute(interval);
                    break;
                case SettingsUtils.PREF_UPLOAD_RATE:
                    bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_UPLOAD_RATE));
                    break;
                case SettingsUtils.PREF_POWER_INDICATOR:
                    if (SettingsUtils.isPowerIndicatorShown(context)) {
                        Notifier.startStatusBar(context);
                    } else {
                        Notifier.closeStatusBar();
                    }
                    break;
                case SettingsUtils.PREF_NOTIFICATIONS_PRIORITY:
                    bindPreferenceSummaryToValue(findPreference(SettingsUtils.PREF_NOTIFICATIONS_PRIORITY));
                    break;
            }
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            String stringValue = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), "");

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            }
        }
    }
}
