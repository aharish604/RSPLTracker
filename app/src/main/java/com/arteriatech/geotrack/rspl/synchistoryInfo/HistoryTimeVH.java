package com.arteriatech.geotrack.rspl.synchistoryInfo;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.arteriatech.geotrack.rspl.R;


class HistoryTimeVH extends RecyclerView.ViewHolder {
    TextView tvEntityName, tvSyncTime;

    public HistoryTimeVH(View view) {
        super(view);
        tvEntityName = (TextView)view.findViewById(R.id.tvEntityName);
        tvSyncTime = (TextView)view.findViewById(R.id.tvSyncTime);
    }
}
