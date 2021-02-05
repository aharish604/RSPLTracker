package com.arteriatech.geotrack.rspl.synchistoryInfo;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.R;


public class SyncHistoryInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_history2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ConstantsUtils.initActionBarView(this, toolbar, true, getString(R.string.sync_hist), 0);
        Fragment infoFragment = new SyncHistoryInfoFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.flContainer,infoFragment);
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

        }
        return true;
    }

}
