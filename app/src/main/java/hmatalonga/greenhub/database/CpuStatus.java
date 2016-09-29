package hmatalonga.greenhub.database;

import hmatalonga.greenhub.utils.StringHelper;

/**
 * Created by hugo on 09-04-2016.
 */
public class CpuStatus {
    private static final int fieldNum = 2;
    private double cpuUsage; // optional
    private double uptime; // optional

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
            try {
                setCpuUsage(Double.parseDouble(values[0]));
                setUptime(Double.parseDouble(values[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(cpuUsage) + ";" + String.valueOf(uptime);
    }
}
