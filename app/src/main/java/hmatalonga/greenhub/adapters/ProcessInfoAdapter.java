package hmatalonga.greenhub.adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.database.ProcessInfo;
import hmatalonga.greenhub.sampling.Inspector;
import hmatalonga.greenhub.utils.AlphabeticalProcessInfoSort;

/**
 * Created by hugo on 02-07-2016.
 */
public class ProcessInfoAdapter extends BaseAdapter {
    private static List<ProcessInfo> searchArrayList;

    private LayoutInflater mInflater;

    private Context c = null;

    public ProcessInfoAdapter(Context context, List<ProcessInfo> results) {
        this.c = context;
        searchArrayList = results;
        for (ProcessInfo item: searchArrayList)
            if (!item.isSetApplicationLabel())
                item.setApplicationLabel(GreenHub.labelForApp(c, item.getpName()));

        Collections.sort(searchArrayList, new AlphabeticalProcessInfoSort(context));
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.process, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView
                    .findViewById(R.id.app_icon);
            holder.txtName = (TextView) convertView
                    .findViewById(R.id.processName);
            holder.pkgName = (TextView) convertView
                    .findViewById(R.id.pkgName);
            holder.txtBenefit = (TextView) convertView
                    .findViewById(R.id.processPriority);
            // holder.moreInfo = (ImageView)
            // convertView.findViewById(R.id.moreinfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (searchArrayList == null || position < 0
                || position >= searchArrayList.size())
            return convertView;
        ProcessInfo x = searchArrayList.get(position);
        if (x == null)
            return convertView;

        String p = x.getpName();
        PackageInfo pak = Inspector.getPackageInfo(c, p);
        String ver = "";
        if (pak != null){
            ver = pak.versionName;
            if (ver == null)
                ver = pak.versionCode+"";
        }

        holder.appIcon.setImageDrawable(GreenHub.iconForApp(c, p));
        holder.pkgName.setText(trunc(p));
        if (x.isSetApplicationLabel())
            holder.txtName.setText(trunc(x.getApplicationLabel()+ " " + ver));
        else
            holder.txtName.setText(trunc(GreenHub.labelForApp(c, p)+ " " + ver));
        holder.txtBenefit.setText(trunc(GreenHub.translatedPriority(x.getImportance())));
        // holder.moreInfo...

        return convertView;
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView txtName;
        TextView txtBenefit;
        TextView pkgName;
        // ImageView moreInfo;
    }

    public static String trunc(String text){
        if (text != null && text.length() > 30)
            return text.substring(0, 28)+" …";
        else
            return text;
    }
}