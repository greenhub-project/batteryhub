package hmatalonga.greenhub.database;

import java.util.Arrays;
import java.util.List;

import hmatalonga.greenhub.util.StringHelper;

/**
 * Created by hugo on 09-04-2016.
 */
public class ProcessInfo {
    private static final int fieldNum = 9;
    private int pId; // optional
    private String pName; // optional
    private String applicationLabel; // optional
    private boolean isSystemApp; // optional
    private String importance; // optional
    private String versionName; // optional
    private int versionCode; // optional
    private List<String> appSignatures; // optional
    private String installationPkg; // optional

    public ProcessInfo() {}

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getApplicationLabel() {
        return applicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;
    }

    public boolean isSetApplicationLabel() {
        return this.applicationLabel != null;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public List<String> getAppSignatures() {
        return appSignatures;
    }

    public void setAppSignatures(List<String> appSignatures) {
        this.appSignatures = appSignatures;
    }

    public String getInstallationPkg() {
        return installationPkg;
    }

    public void setInstallationPkg(String installationPkg) {
        this.installationPkg = installationPkg;
    }

    private List<String> parseAppSignatures(String s) {
        List<String> sig = null;

        if (!s.equals("null")) {
            s = s.substring(1, s.length() - 1);
            String[] split = s.split(",");
            sig = Arrays.asList(split);
        }

        return sig;
    }

    public void parseString(String s) {
        String[] values = StringHelper.trimArray(s.split(";"));
        if (values.length == fieldNum) {
            try {
                setpId(Integer.parseInt(values[0]));
                setpName(values[1]);
                setApplicationLabel(values[2]);
                setSystemApp(Boolean.parseBoolean(values[3]));
                setImportance(values[4]);
                setVersionName(values[5]);
                setVersionCode(Integer.parseInt(values[6]));
                setAppSignatures(parseAppSignatures(values[7])); // List
                setInstallationPkg(values[8]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(pId) + ";" + pName + ";" + applicationLabel + ";" +
                String.valueOf(isSystemApp) + ";" + importance + ";" + versionName + ";" +
                String.valueOf(versionCode) + ";" + appSignatures + ";" + installationPkg;
    }
}
