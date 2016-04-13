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
 * Created by hugo on 05-04-2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.DashboardViewHolder> {

    public static class DashboardViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView title;
        TextView value;

        public DashboardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            title = (TextView) itemView.findViewById(R.id.dashboard_title);
            value = (TextView) itemView.findViewById(R.id.dashboard_value);
        }
    }

    List<DeviceResourceCard> deviceResourceCards;

    public RVAdapter(List<DeviceResourceCard> deviceResourceCards){
        this.deviceResourceCards = deviceResourceCards;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dashboard_item, viewGroup, false);
        return new DashboardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DashboardViewHolder personViewHolder, int i) {
        personViewHolder.title.setText(deviceResourceCards.get(i).title);
        personViewHolder.value.setText(deviceResourceCards.get(i).value);
    }

    @Override
    public int getItemCount() {
        return deviceResourceCards.size();
    }
}
