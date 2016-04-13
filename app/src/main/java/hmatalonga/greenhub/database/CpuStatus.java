package hmatalonga.greenhub.database;

/**
 * Created by hugo on 09-04-2016.
 */
public class CpuStatus {
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
}
