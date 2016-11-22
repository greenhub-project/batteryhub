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

package hmatalonga.greenhub.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.GreenHubHelper;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.managers.sampling.Inspector;
import hmatalonga.greenhub.managers.storage.GreenHubDb;
import hmatalonga.greenhub.models.Network;
import hmatalonga.greenhub.models.data.Sample;

/**
 * Created by hugo on 15-04-2016.
 */
public class SampleSender {
    private static final String TAG = "sendSamples";

    private static final String TRY_AGAIN = " will try again later.";

    private static final Object sendLock = new Object();

    GreenHubHelper app = null;

    // Prevent instantiation
    private SampleSender(){}

    public static void sendSamples(GreenHubHelper app) {
        synchronized(sendLock){
            Context context = GreenHubHelper.getContext();

            String networkStatus = Network.getStatus(context);
            String networkType = Network.getType(context);

            final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean useWifiOnly = p.getBoolean(context.getString(R.string.wifi_only_key), false);

            boolean connected = (!useWifiOnly && networkStatus.equals(Network.NETWORKSTATUS_CONNECTED))
                    || networkType.equals("WIFI");

            if (connected) {
                GreenHubDb db = GreenHubDb.getInstance(context);
                int samples = db.countSamples();

                /* Click Tracking: Track sample sending. */
//                String uuId = p.getString(GreenHubHelper.getRegisteredUuid(), "UNKNOWN");
//                HashMap<String, String> options = new HashMap<String, String>();
//                options.put("count", samples+"");
//                ClickTracking.track(uuId, "sendingsamples", options, c);
                /* End Click Tracking: Track sample sending. */

                int successSum = 0;
                for (int batches = 0; batches < Config.SAMPLE_MAX_BATCHES && batches < samples
                        / Config.COMMS_MAX_UPLOAD_BATCH + 1; batches++) {
                    SortedMap<Long, Sample> map = GreenHubDb.getInstance(context).queryOldestSamples(Config.COMMS_MAX_UPLOAD_BATCH);
                    if (map.size() > 0) {

//                        int progress = (int) (successSum * 1.0 / samples * 100.0);
//                        CaratApplication.setActionProgress(progress, successSum + "/"
//                                + samples +" "+ app.getString(R.string.samplesreported), false);

                        if (app.communicationManager != null) {
                            int tries = 0;
                            while (tries < 2) {
                                try {
                                    int success = 0; //app.communicationManager.sendSamples(map.values());

                                    tries = 2;
                                    // FlurryAgent.logEvent("UploadSamples");
                                    if (Config.DEBUG)
                                        Log.d(TAG, "Uploaded " + success
                                                + " samples out of " + map.size());
//                                    if (success > 0)
//                                        GreenHubHelper.getStorage().samplesReported(success);
                                    Sample last = map.get(map.lastKey());

									/*
									 * converting (to human readable date-time format)
									 * the "timestamp" of the last sample (which is
									 * uploaded now, and should be deleted along the other
									 * uploaded samples). The "timestamp" is computed this way:
									 * CurrentTimeMillis / 1000
									 * (see getSample() in SamplingLibrary)
									 */
                                    long lastSampleTime = (long) last.getTimestamp() * 1000; // in currentTimeMillis
                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                                    Date resultdate = new Date(lastSampleTime);
                                    if (Config.DEBUG)
                                        Log.d(TAG,
                                                "Deleting " + success
                                                        + " samples older than "
                                                        + sdf.format(resultdate));
                                    /*
                                     * Log.i(TAG, "Sent samples:"); for (Sample k:
                                     * map.values()){ Log.i(TAG, k.getTimestamp() +
                                     * " " + k.getBatteryLevel()); }
                                     */
                                    SortedSet<Long> uploaded = new TreeSet<Long>();
                                    int i = 0;
                                    for (Long s : map.keySet()) {
                                        if (i < success)
                                            uploaded.add(s);
                                        i += 1;
                                    }
                                    int deleted = GreenHubDb.getInstance(context).deleteSamples(uploaded);
                                    successSum += success;
                                } catch (Throwable th) {
                                    tries++;
                                }
                            }
                        } else {
                            Log.w(TAG, "CommunicationManager is not ready yet."
                                    + TRY_AGAIN);
                        }
                    } else {
                        Log.w(TAG, "No samples to send." + TRY_AGAIN);
                    }
                }

                /* Click Tracking: Track sample sending. */
//                options.put("count", successSum+"");
//                ClickTracking.track(uuId, "sentsamples", options, c);
                /* End Click Tracking: Track sample sending. */

            }/* else if (networkStatus
                    .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                Log.w(TAG, "Network status: " + networkStatus
                        + ", trying again in 10s.");
                connecting = true;
            } else {
                Log.w(TAG, "Network status: " + networkStatus + TRY_AGAIN);
                connecting = false;
            }
            if (connecting) {
                // wait for wifi to come up
                try {
                    Thread.sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                    // ignore
                }
                connecting = false;
            } else {
                try {
                    Thread.sleep(CaratApplication.COMMS_INTERVAL);
                } catch (InterruptedException e) {
                    // wait for wifi to come up
                    try {
                        Thread.sleep(CaratApplication.COMMS_WIFI_WAIT);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            }*/
        }
    }
}
