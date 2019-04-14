package com.hmatalonga.greenhub.ui.adapters;

import android.app.Fragment;
import android.content.Context;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.Sensors;
import com.hmatalonga.greenhub.models.data.SensorDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData(final Context context, final Fragment fragment) {
        List<SensorDetails> list = Sensors.getSensorDetailsList(context);
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        for (SensorDetails sensor : list) {
            List<String> details = new ArrayList<String>();
            details.add(fragment.getString(R.string.sensors_card_type) + ": " +sensor.stringType);
            details.add(fragment.getString(R.string.sensors_card_power) + ": " +sensor.power);
            details.add(fragment.getString(R.string.sensors_card_iswakeup) + ": " +(sensor.isWakeUpSensor ? fragment.getString(R.string.yes) : fragment.getString(R.string.no)));
            expandableListDetail.put(sensor.name != null ? sensor.name.toUpperCase() : sensor.stringType.toUpperCase(), details);
        }
        return expandableListDetail;
    }
}