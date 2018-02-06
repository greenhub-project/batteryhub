package com.hmatalonga.greenhub.events;

/**
 * BatteryTimeEvent
 */

public class BatteryTimeEvent {
    public final int remainingHours;
    public final int remainingMinutes;
    public final boolean charging;

    public BatteryTimeEvent(int remainingHours, int remainingMinutes, boolean charging) {
        this.remainingHours = remainingHours;
        this.remainingMinutes = remainingMinutes;
        this.charging = charging;
    }

}
