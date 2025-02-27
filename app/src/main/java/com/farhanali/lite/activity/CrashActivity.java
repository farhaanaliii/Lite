package com.farhanali.lite.activity;

import android.os.Bundle;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.farhanali.lite.R;
import com.farhanali.lite.utils.Utils;


public class CrashActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {
    public static final String EXTRA_CRASH_INFO = "crashInfo";
    private String crashLog;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        crashLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);

        TextView logText = findViewById(R.id.logText);
        logText.setText(crashLog != null ? crashLog : "No crash information available.");
    }

    @Override
    public void onBackPressed(){
        // Close the activity on back press to prevent crash loop
        finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == android.R.id.copy){
            Utils.Copy(crashLog, context);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, android.R.id.copy, 0, android.R.string.copy)
                .setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }
}
