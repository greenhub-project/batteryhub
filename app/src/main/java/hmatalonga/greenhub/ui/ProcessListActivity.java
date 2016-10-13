package hmatalonga.greenhub.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

import hmatalonga.greenhub.R;
import hmatalonga.greenhub.adapters.ProcessInfoAdapter;
import hmatalonga.greenhub.database.ProcessInfo;
import hmatalonga.greenhub.sampling.Inspector;

public class ProcessListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_list);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        ListView lv = (ListView) findViewById(R.id.processList);
        List<ProcessInfo> searchResults = Inspector.getRunningAppInfo(this);
        lv.setAdapter(new ProcessInfoAdapter(this, searchResults));
    }
}
