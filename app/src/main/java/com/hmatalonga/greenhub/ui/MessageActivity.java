package com.hmatalonga.greenhub.ui;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.hmatalonga.greenhub.R;
import com.hmatalonga.greenhub.managers.storage.GreenHubDb;

public class MessageActivity extends BaseActivity {
    private int mMessageId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();

        if (extras == null) return;

        mMessageId = extras.getInt("id");

        TextView textView = (TextView) findViewById(R.id.message_title);
        textView.setText(extras.getString("title"));
        textView = (TextView) findViewById(R.id.message_body);
        textView.setText(extras.getString("body"));
        textView = (TextView) findViewById(R.id.message_date);
        try {
            textView.setText(extras.getString("date").substring(0, 16));
        } catch (NullPointerException e) {
            textView.setText("Not available");
            e.printStackTrace();
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabDeleteMessage);
        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                // Add the buttons
                builder.setMessage(R.string.dialog_message_text)
                        .setTitle(R.string.dialog_message_title);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GreenHubDb database = new GreenHubDb();
                        database.deleteMessage(mMessageId);
                        database.close();
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
