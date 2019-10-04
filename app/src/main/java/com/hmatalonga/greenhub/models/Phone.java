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

package com.hmatalonga.greenhub.models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.hmatalonga.greenhub.util.PermissionsUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Phone.
 */
public class Phone {

    // Call state constants
    public static String CALL_STATE_IDLE = "idle";
    public static String CALL_STATE_OFFHOOK = "offhook";
    public static String CALL_STATE_RINGING = "ringing";

    // Phone type constants
    public static String PHONE_TYPE_CDMA = "cdma";
    public static String PHONE_TYPE_GSM = "gsm";
    public static String PHONE_TYPE_SIP = "sip";
    public static String PHONE_TYPE_NONE = "none";

    /* Get call status */
    public static String getCallState(Context context) {
        TelephonyManager telManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int callState = telManager.getCallState();
        switch (callState) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return CALL_STATE_OFFHOOK;
            case TelephonyManager.CALL_STATE_RINGING:
                return CALL_STATE_RINGING;
            default:
                return CALL_STATE_IDLE;
        }
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(final Context context) {
        if (PermissionsUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager manager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            return (manager == null) ? null : manager.getDeviceId();
        }
        return null;
    }

    /* Get Phone Type */
    public static String getType(Context context) {
        TelephonyManager telManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int phoneType = telManager.getPhoneType();
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_CDMA:
                return PHONE_TYPE_CDMA;
            case TelephonyManager.PHONE_TYPE_GSM:
                return PHONE_TYPE_GSM;
            default:
                return PHONE_TYPE_NONE;
        }
    }

    /**
     * Return a long[3] with incoming call time, outgoing call time, and
     * non-call time in seconds since boot.
     *
     * @param context from onReceive or Activity
     * @return a long[3] with incoming call time, outgoing call time, and
     * non-call time in seconds since boot.
     */
    /*
    public static long[] getCalltimesSinceBoot(Context context) {

        long[] result = new long[3];

        long callInSeconds = 0;
        long callOutSeconds = 0;
        int type;
        long dur;

        // ms since boot
        long uptime = SystemClock.elapsedRealtime();
        long now = System.currentTimeMillis();
        long bootTime = now - uptime;

        String[] queries = new String[]{
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION
        };

        try {
            // It requires permission READ_CALL_LOG
            Cursor cursor = context.getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI, queries,
                    android.provider.CallLog.Calls.DATE + " > " + bootTime, null,
                    android.provider.CallLog.Calls.DATE + " ASC"
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        type = cursor.getInt(0);
                        dur = cursor.getLong(2);
                        switch (type) {
                            case android.provider.CallLog.Calls.INCOMING_TYPE:
                                callInSeconds += dur;
                                break;
                            case android.provider.CallLog.Calls.OUTGOING_TYPE:
                                callOutSeconds += dur;
                                break;
                            default:
                        }
                        cursor.moveToNext();
                    }
                } else {
                    logW("CallDurFromBoot", "No calls listed");
                }
                cursor.close();
            } else {
                logW("CallDurFromBoot", "Cursor is null");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // upTime is ms, so it needs to be divided by 1000
        long nonCallTime = uptime / 1000 - callInSeconds - callOutSeconds;
        result[0] = callInSeconds;
        result[1] = callOutSeconds;
        result[2] = nonCallTime;
        return result;
    }

    // Get a monthly call duration record
    // TODO: Refactor!!
    public static Map<String, CallMonth> getMonthCallDur(Context context) {

        Map<String, CallMonth> callMonth = new HashMap<>();
        Map<String, String> callInDur = new HashMap<>();
        Map<String, String> callOutDur = new HashMap<>();

        int callType;
        long callDur;
        Date callDate;
        String tmpTime = null;
        String time;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM", Locale.UK);
        CallMonth curMonth = null;

        String[] queryFields = new String[]{
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION
        };

        try {
            // It requires permission READ_CALL_LOG
            Cursor cursor = context.getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI, queryFields,
                    null, null, android.provider.CallLog.Calls.DATE + " DESC"
            );

            if (cursor == null) {
                return callMonth;
            }

            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    cursor.moveToPosition(i);
                    callType = cursor.getInt(0);
                    callDate = new Date(cursor.getLong(1));
                    callDur = cursor.getLong(2);

                    time = dateformat.format(callDate);
                    if (tmpTime != null && !time.equals(tmpTime)) {
                        callMonth.put(tmpTime, curMonth);
                        callInDur.clear();
                        callOutDur.clear();
                        curMonth = new CallMonth();
                    }
                    tmpTime = time;

                    if (callType == 1) {
                        curMonth.totalCallInNum++;
                        curMonth.totalCallInDur += callDur;
                        callInDur.put("totalCallInNum", String.valueOf(curMonth.totalCallInNum));
                        callInDur.put("totalCallInDur", String.valueOf(curMonth.totalCallInDur));
                    }
                    if (callType == 2) {
                        curMonth.totalCallOutNum++;
                        curMonth.totalCallOutDur += callDur;
                        callOutDur.put("totalCallOutNum", String.valueOf(curMonth.totalCallOutNum));
                        callOutDur.put("totalCallOutDur", String.valueOf(curMonth.totalCallOutDur));
                    }
                    if (callType == 3) {
                        curMonth.totalMissedCallNum++;
                        callInDur.put("totalMissedCallNum",
                            String.valueOf(curMonth.totalMissedCallNum));
                    }
                }
            }
            cursor.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return callMonth;
    }

    public static CallMonth getCallMonthinfo(Context context, String time) {
        return getMonthCallDur(context).get(time);
    }
    */

    /**
     * Returns numeric mobile country code.
     *
     * @param context Application context
     * @return 3-digit country code
     */
    public static String getMcc(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getNetworkOperator();
        if (networkOperator != null && networkOperator.length() >= 5) {
            return networkOperator.substring(0, 3);
        }
        String operatorProperty = "gsm.operator.numeric";
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            operatorProperty = "ro.cdma.home.operator.numeric"; // CDMA
        }
        networkOperator = Specifications.getStringFromSystemProperty(context, operatorProperty);
        if (networkOperator != null && networkOperator.length() >= 5) {
            return networkOperator.substring(0, 3);
        }
        return "Unknown";
    }

    /**
     * Returns numeric mobile network code.
     *
     * @param context Application context
     * @return 2-3 digit network code
     */
    public static String getMnc(final Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getNetworkOperator();
        if (networkOperator != null && networkOperator.length() >= 5) {
            return networkOperator.substring(3);
        }
        String operatorProperty = "gsm.operator.numeric";
        if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            operatorProperty = "ro.cdma.home.operator.numeric"; // CDMA
        }
        networkOperator = Specifications.getStringFromSystemProperty(context, operatorProperty);
        if (networkOperator != null && networkOperator.length() >= 5) {
            return networkOperator.substring(3);
        }
        return "Unknown";
    }

    /**
     * Network operator is responsible for the network infrastructure which
     * might be used by many virtual network operators. Network operator
     * is not necessarily bound to the device and might change at any time.
     *
     * @param context Application context
     * @return Network operator name, aka. carrier
     */
    public static String getNetworkOperator(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator;

        operator = getNetworkOperators(context);
        if (operator != null && operator.length() != 0) return operator;
        operator = telephonyManager.getNetworkOperatorName();
        if (operator != null && operator.length() != 0) return operator;
        // CDMA support
        operator = Specifications.getStringFromSystemProperty(
                context,
                "ro.cdma.home.operator.alpha"
        );
        if (operator != null && operator.length() != 0) return operator;

        return "unknown";
    }

    /**
     * Retrieves network operator names from subscription manager.
     * NOTE: Requires SDK level 22 or above
     *
     * @param context
     * @return
     */
    private static String getNetworkOperators(Context context) {
        String operator = "";

        if (!PermissionsUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            return operator;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            if (subscriptionManager != null) {
                List<SubscriptionInfo> subscriptions =
                        subscriptionManager.getActiveSubscriptionInfoList();
                if (subscriptions != null) {
                    for (SubscriptionInfo info : subscriptions) {
                        CharSequence carrierName = info.getCarrierName();
                        if (carrierName != null && carrierName.length() > 0) {
                            operator += carrierName + ";";
                        }
                    }
                    // Remove last delimiter
                    if (operator.length() >= 1) {
                        operator = operator.substring(0, operator.length() - 1);
                    }
                }
            }
        }
        return operator;
    }

    /**
     * Undocumented call to look up a two-letter country code with an mcc.
     * WARNING: Uses reflection, data might not always be available.
     *
     * @param context Application context
     * @param mcc     Numeric country code
     * @return Country code
     * @throws Exception
     */
    static String getCountryCodeForMcc(Context context, int mcc) throws Exception {
        Class<?> mccTable = Class.forName("com.android.internal.telephony.MccTable");
        Method countryCodeForMcc = mccTable.getMethod("countryCodeForMcc", int.class);
        countryCodeForMcc.setAccessible(true);
        return ((String) countryCodeForMcc.invoke(context, mcc));
    }
}
