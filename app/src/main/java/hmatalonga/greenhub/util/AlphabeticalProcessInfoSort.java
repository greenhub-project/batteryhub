package hmatalonga.greenhub.util;

import android.content.Context;

import java.util.Comparator;

import hmatalonga.greenhub.database.ProcessInfo;

/**
 * Created by hugo on 02-07-2016.
 */
public class AlphabeticalProcessInfoSort implements
        Comparator<ProcessInfo> {

    private Context c;

    public AlphabeticalProcessInfoSort(Context c){
        this.c = c;
    }

    @Override
    public int compare(ProcessInfo lhs, ProcessInfo rhs) {
        if (lhs.isSetApplicationLabel() && rhs.isSetApplicationLabel())
            return lhs.getApplicationLabel().compareTo(rhs.getApplicationLabel());

        String l = lhs.getpName();
        String r = rhs.getpName();
        if (l != null && r != null)
            return l.compareTo(r);
        else
            return 0;
    }
}