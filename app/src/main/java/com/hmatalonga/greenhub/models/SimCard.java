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

package hmatalonga.greenhub.models;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.List;

import hmatalonga.greenhub.Config;
import hmatalonga.greenhub.util.PermissionsUtils;

import static hmatalonga.greenhub.util.LogUtils.LOGD;

/**
 * SimCard.
 */
public class SimCard {

    private static final String TAG = "SimCard";

    /**
     * SIM Operator is responsible for the product that is subscription.
     * It is directly associated with the SIM card and remains the same
     * even when changing between physical networks.
     *
     * SIM Operator might or might not own the infrastructure in use.
     * NOTE: Getting multiple operators is highly experimental.
     *
     * @param context Application context
     * @return SIM Operator name
     */
    public static String getSIMOperator(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator;

        operator = getSIMOperators(context); // Supports multiple sim cards
        if(operator != null && operator.length() > 0) return operator;
        operator = telephonyManager.getSimOperatorName();
        if(operator != null && operator.length() > 0) return operator;

        return "unknown";
    }

    /**
     * Experimental call to retrieve sim operator names by subscription ids.
     *
     * @param context Application context
     * @return SIM operator name/names with ";" as a delimiter for many.
     */
    private static String getSIMOperators(final Context context) {

        String operators = "";

        if (!PermissionsUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            return operators;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<SubscriptionInfo> subscriptions = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
            if (subscriptions != null && subscriptions.size() > 0) {
                for(SubscriptionInfo info : subscriptions){
                    int subId = info.getSubscriptionId();
                    String operator = getSimOperatorNameForSubscription(context, subId);
                    if (operator != null && operator.length() > 0) {
                        operators += operator + ";";
                    }
                }
                // Remove last delimiter
                if (operators.length() > 1) {
                    operators = operators.substring(0, operators.length()-1);
                }
            }
        }
        return operators;
    }

    /**
     * Retrieves sim operator name using an undocumented telephony manager call.
     * WARNING: Uses reflection, data might not always be available.
     *
     * @param context
     * @param subId
     * @return
     */
    private static String getSimOperatorNameForSubscription(final Context context, int subId) {
        TelephonyManager stub = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyManager = Class.forName(stub.getClass().getName());
            Method getName = telephonyManager.getMethod("getSimOperatorNameForSubscription", int.class);
            getName.setAccessible(true);
            return ((String) getName.invoke(context, subId));
        } catch (Exception e) {
            if(Config.DEBUG && e != null && e.getLocalizedMessage() != null){
                LOGD(TAG, "Failed getting sim operator with subid: " + e.getLocalizedMessage());
            }
        }
        return null;
    }
}
