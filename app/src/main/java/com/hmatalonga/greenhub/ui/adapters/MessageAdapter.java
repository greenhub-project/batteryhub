package com.hmatalonga.greenhub.ui.adapters;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.OpenMessageEvent;
import com.hmatalonga.greenhub.models.data.Message;
import com.hmatalonga.greenhub.util.StringHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.DashboardViewHolder> {

    static class DashboardViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView body;
        public TextView date;

        DashboardViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.message_title);
            body = itemView.findViewById(R.id.message_body);
            date = itemView.findViewById(R.id.message_date);
        }
    }

    private ArrayList<Message> mMessages;

    public MessageAdapter(ArrayList<Message> items){
        this.mMessages = items;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public MessageAdapter.DashboardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.message_item_view,
                viewGroup,
                false
        );
        return new MessageAdapter.DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.DashboardViewHolder viewHolder, int i) {
        viewHolder.title.setText(StringHelper.truncate(mMessages.get(i).title, 25));
        viewHolder.body.setText(StringHelper.truncate(mMessages.get(i).body, 30));
        viewHolder.date.setText(mMessages.get(i).date.substring(0, 10));

        if (!mMessages.get(i).read) {
            viewHolder.title.setTypeface(null, Typeface.BOLD);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new OpenMessageEvent(viewHolder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void swap(ArrayList<Message> list){
        if (mMessages != null) {
            mMessages.clear();
            mMessages.addAll(list);
        }
        else {
            mMessages = list;
        }
        notifyDataSetChanged();
    }
}