/*
 * Copyright 2016 Hugo Matalonga
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

package hmatalonga.greenhub.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import hmatalonga.greenhub.GreenHub;
import hmatalonga.greenhub.ui.ProcessListActivity;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.sampling.Inspector;

/**
 * Device Fragment.
 */
public class DeviceFragment extends Fragment {

    private Context mContext;

    /** Related to the CPU usage bar */
    private long[] mLastPoint = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_device, container, false);

        /** Load Application Context to the fragment */
        mContext = GreenHub.getContext();

        loadComponents(view);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }

    // Private Helper Methods ----------------------------------------------------------------------

    /**
     * Helper method to load all UI views.
     *
     * @param view View to update
     */
    private void loadComponents(final View view) {
        Button button = (Button) view.findViewById(R.id.btProcessList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProcessListActivity.class);
                startActivity(intent);
            }
        });

        populateView(view);

        setMemoryBars(view);
    }

    /**
     * Set all values for layout view elements.
     *
     * @param view View to update
     */
    private void populateView(final View view) {
        TextView textView = (TextView) view.findViewById(R.id.IdValue);
        textView.setText(Inspector.getAndroidId(mContext));
        textView = (TextView) view.findViewById(R.id.OsVersionValue);
        textView.setText(Inspector.getOsVersion());
        textView = (TextView) view.findViewById(R.id.deviceModelValue);
        textView.setText(Inspector.getModel());
        textView = (TextView) view.findViewById(R.id.kernelValue);
        textView.setText(Inspector.getKernelVersion());
        textView = (TextView) view.findViewById(R.id.manufacturerValue);
        textView.setText(Inspector.getManufacturer());
    }

    /**
     * Set Memory bars values.
     *
     * @param view View to update
     */
    private void setMemoryBars(final View view) {
        int[] totalAndUsed = Inspector.readMeminfo();

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.memoryUsedProgressBar);
        progressBar.setMax(totalAndUsed[0] + totalAndUsed[1]);
        progressBar.setProgress(totalAndUsed[0]);
        progressBar = (ProgressBar) view.findViewById(R.id.memoryActiveProgressBar);

        if (totalAndUsed.length > 2) {
            progressBar.setMax(totalAndUsed[2] + totalAndUsed[3]);
            progressBar.setProgress(totalAndUsed[2]);
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                long[] currentPoint = Inspector.readUsagePoint();

                double cpu = 0;

                if (mLastPoint == null) {
                    mLastPoint = currentPoint;
                } else {
                    cpu = Inspector.getUsage(mLastPoint, currentPoint);
                }

                /** CPU usage */
                ProgressBar mText = (ProgressBar) view.findViewById(R.id.cpuUsageProgressBar);
                mText.setMax(100);
                mText.setProgress((int) (cpu * 100));
            }
        });
    }

    /**
     * Cleans local variables preventing memory leaks.
     */
    private void clean() {
        mContext = null;
        mLastPoint = null;
        System.gc();
    }
}
