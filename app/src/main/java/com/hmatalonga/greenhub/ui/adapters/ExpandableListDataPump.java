package com.hmatalonga.greenhub.ui.adapters;

import android.app.Fragment;
import android.content.Context;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.models.Sensors;
import com.hmatalonga.greenhub.models.data.SensorDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData(
            final Context context) {
        Collection<SensorDetails> list = Sensors.getSensorDetailsList(context);
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        for (SensorDetails sensor : list) {
            List<String> details = new ArrayList<String>();
            details.add(context.getString(R.string.sensors_card_type) + ": " + sensor.stringType);
            details.add(context.getString(R.string.sensors_card_power) + ": "
                    + sensor.power + " mA");
            details.add(context.getString(R.string.sensors_card_iswakeup) + ": "
                    + (sensor.isWakeUpSensor ?
                    context.getString(R.string.yes) : context.getString(R.string.no)));
            details.add(context.getString(R.string.sensors_card_inuse) + ": "
                    + (sensor.frequencyOfUse != 0 ?
                    context.getString(R.string.yes) : context.getString(R.string.no)));
            details.add(context.getString(R.string.sensors_card_usefrequency) + ": "
                    + (sensor.frequencyOfUse));
            expandableListDetail.put(
                    ((sensor.name != null) ?
                            sensor.name.toUpperCase() :
                            sensor.stringType.toUpperCase()), details);
        }
        return expandableListDetail;
    }
}