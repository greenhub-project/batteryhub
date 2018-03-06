package com.hmatalonga.greenhub.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.events.OpenMessageEvent;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;
import com.hmatalonga.greenhub.models.data.Message;
import com.hmatalonga.greenhub.ui.adapters.MessageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import io.realm.RealmResults;

public class InboxActivity extends BaseActivity {

    private RecyclerView mRecyclerView;

    private TextView mNoMessagesTextView;

    private MessageAdapter mAdapter;

    private ArrayList<Message> mMessages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mNoMessagesTextView = findViewById(R.id.no_messages_view);
        mRecyclerView = findViewById(R.id.rv);
        mAdapter = null;

        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openMessage(OpenMessageEvent event) {
        final Message message = mMessages.get(event.index);

        if (!message.read) {
            GreenHubDb database = new GreenHubDb();
            database.markMessageAsRead(message.id);
            database.close();
        }

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("id", message.id);
        intent.putExtra("title", message.title);
        intent.putExtra("body", message.body);
        intent.putExtra("date", message.date);
        startActivity(intent);
    }

    private void loadData() {
        mMessages = new ArrayList<>();
        GreenHubDb database = new GreenHubDb();

        RealmResults<Message> results = database.allMessages();

        for (Message message : results ) {
            mMessages.add(new Message(
                    message.id,
                    message.title,
                    message.body,
                    message.date,
                    message.read
            ));
        }

        database.close();

        setAdapter();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new MessageAdapter(mMessages);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.swap(mMessages);
        }
        mRecyclerView.invalidate();

        if (mMessages.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mNoMessagesTextView.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoMessagesTextView.setVisibility(View.GONE);
        }
    }
}
