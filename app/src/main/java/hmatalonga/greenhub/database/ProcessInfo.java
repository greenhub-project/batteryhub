package hmatalonga.greenhub.database;

import java.util.List;

/**
 * Created by hugo on 09-04-2016.
 */
public class ProcessInfo {
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
}
