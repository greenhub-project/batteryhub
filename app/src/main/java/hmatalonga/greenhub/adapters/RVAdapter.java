package hmatalonga.greenhub.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hmatalonga.greenhub.R;
import hmatalonga.greenhub.model.DeviceResourceCard;

/**
 * RecyclerView Adapter Class
 * Created by hugo on 05-04-2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DashboardViewHolder> {

    public static class DashboardViewHolder extends RecyclerView.ViewHolder {

        public CardView cv;
        public TextView title;
        public TextView value;

        public DashboardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            title = (TextView) itemView.findViewById(R.id.dashboard_title);
            value = (TextView) itemView.findViewById(R.id.dashboard_value);
        }
    }

    private List<DeviceResourceCard> mDeviceResourceCards;

    public RVAdapter(List<DeviceResourceCard> deviceResourceCards){
        this.mDeviceResourceCards = deviceResourceCards;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dashboard_item,
                viewGroup, false);
        return new DashboardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder personViewHolder, int i) {
        personViewHolder.title.setText(mDeviceResourceCards.get(i).title);
        personViewHolder.value.setText(mDeviceResourceCards.get(i).value);
    }

    @Override
    public int getItemCount() {
        return mDeviceResourceCards.size();
    }
}
