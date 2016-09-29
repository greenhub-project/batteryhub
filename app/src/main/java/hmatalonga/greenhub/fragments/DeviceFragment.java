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
import hmatalonga.greenhub.MainActivity;
import hmatalonga.greenhub.ProcessListActivity;
import hmatalonga.greenhub.R;
import hmatalonga.greenhub.sampling.Inspector;

/**
 * Device Fragment
 * Created by hugo on 28-03-2016.
 */
public class DeviceFragment extends Fragment {
    private Context mContext;
    private Thread mLocalThread;
    private long[] mLastPoint = null;  // related to the CPU usage bar

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        mContext = GreenHub.getContext();
        populateView(view);
        setMemoryBars(view);

        Button button = (Button) view.findViewById(R.id.btProcessList);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProcessListActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Set all values for layout view elements
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

    private void setMemoryBars(final View view) {
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.memoryUsedProgressBar);
        int[] totalAndUsed = Inspector.readMeminfo();
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
                if (mLastPoint == null)
                    mLastPoint = currentPoint;
                else
                    cpu = Inspector.getUsage(mLastPoint, currentPoint);

                /* CPU usage */
                ProgressBar mText = (ProgressBar) view.findViewById(R.id.cpuUsageProgressBar);
                mText.setMax(100);
                mText.setProgress((int) (cpu * 100));
            }
        });
    }
}
