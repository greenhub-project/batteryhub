/*
 * Copyright (C) 2016 Hugo Matalonga
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
import hmatalonga.greenhub.util.AlphabeticalProcessInfoSort;

/**
 * Process List Adapter.
 */
public class ProcessInfoAdapter extends BaseAdapter {

    private static List<ProcessInfo> sSearchArrayList;

    private LayoutInflater mInflater;

    private Context mContext = null;

    public ProcessInfoAdapter(Context context, List<ProcessInfo> results) {
        mContext = context;
        sSearchArrayList = results;
        for (ProcessInfo item: sSearchArrayList)
            if (!item.isSetApplicationLabel())
                item.setApplicationLabel(GreenHub.labelForApp(mContext, item.getpName()));

        Collections.sort(sSearchArrayList, new AlphabeticalProcessInfoSort(context));
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return sSearchArrayList.size();
    }

    public Object getItem(int position) {
        return sSearchArrayList.get(position);
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
        if (sSearchArrayList == null || position < 0
                || position >= sSearchArrayList.size())
            return convertView;
        ProcessInfo x = sSearchArrayList.get(position);
        if (x == null)
            return convertView;

        String p = x.getpName();
        PackageInfo pak = Inspector.getPackageInfo(mContext, p);
        String ver = "";
        if (pak != null){
            ver = pak.versionName;
            if (ver == null)
                ver = pak.versionCode+"";
        }

        holder.appIcon.setImageDrawable(GreenHub.iconForApp(mContext, p));
        holder.pkgName.setText(trunc(p));
        if (x.isSetApplicationLabel())
            holder.txtName.setText(trunc(x.getApplicationLabel()+ " " + ver));
        else
            holder.txtName.setText(trunc(GreenHub.labelForApp(mContext, p)+ " " + ver));
        holder.txtBenefit.setText(trunc(GreenHub.translatedPriority(x.getImportance())));
        // holder.moreInfo...

        return convertView;
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView txtName;
        TextView txtBenefit;
        TextView pkgName;
    }

    public static String trunc(String text){
        if (text != null && text.length() > 30)
            return text.substring(0, 28)+" …";
        else
            return text;
    }
}