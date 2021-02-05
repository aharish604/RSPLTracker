package com.arteriatech.geotrack.rspl.log;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.arteriatech.mutils.log.LogListFragment;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.R;


/**
 * Created by e10742 on 29-11-2016.
 */
public class LogActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize action bar without back button(false)
        //  ActionBarView.initActionBarView(this, true,getString(R.string.log_menu));
        setContentView(R.layout.activity_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ConstantsUtils.initActionBarView(this, toolbar, true, getString(R.string.log_menu), 0);
        //Calling LogList fragment
        getFragmentManager().beginTransaction().replace(R.id.fl_log_view, new LogListFragment()).commit();
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

        }
        return true;
    }*/
}
