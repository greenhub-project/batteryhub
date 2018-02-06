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

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.hmatalonga.greenhub.models.data.CellInfo;

import static com.hmatalonga.greenhub.models.Phone.PHONE_TYPE_CDMA;
import static com.hmatalonga.greenhub.models.Phone.PHONE_TYPE_GSM;

/**
 * Gsm.
 */
public class Gsm {

    private static final String TAG = "Gsm";

    /* check the GSM cell information */
    public static CellInfo getCellInfo(Context context) {
        CellInfo cellInfo = new CellInfo();

        TelephonyManager manager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        String netOperator = manager.getNetworkOperator();

        // Fix crash when not connected to network (airplane mode, underground,
        // etc)
        if (netOperator == null || netOperator.length() < 3) {
            return cellInfo;
        }

		/*
		 * FIXME: Actually check for mobile network status == connected before
		 * doing this stuff.
		 */

        if (Phone.getType(context).equals(PHONE_TYPE_CDMA)) {
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) manager.getCellLocation();

            cellInfo.cid = cdmaLocation.getBaseStationId();
            cellInfo.lac = cdmaLocation.getNetworkId();
            cellInfo.mnc = cdmaLocation.getSystemId();
            cellInfo.mcc = Integer.parseInt(netOperator.substring(0, 3));
            cellInfo.radioType = Network.getMobileNetworkType(context);
        } else if (Phone.getType(context).equals(PHONE_TYPE_GSM)) {
            GsmCellLocation gsmLocation = (GsmCellLocation) manager.getCellLocation();

            cellInfo.mcc = Integer.parseInt(netOperator.substring(0, 3));
            cellInfo.mnc = Integer.parseInt(netOperator.substring(3));
            cellInfo.lac = gsmLocation.getLac();
            cellInfo.cid = gsmLocation.getCid();
            cellInfo.radioType = Network.getMobileNetworkType(context);
        }

        return cellInfo;
    }
}
