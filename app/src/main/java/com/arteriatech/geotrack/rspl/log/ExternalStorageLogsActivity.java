package com.arteriatech.geotrack.rspl.log;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arteriatech.mutils.adapter.AdapterViewInterface;
import com.arteriatech.mutils.adapter.SimpleRecyclerViewTypeAdapter;
import com.arteriatech.geotrack.rspl.Constants;
import com.arteriatech.geotrack.rspl.ConstantsUtils;
import com.arteriatech.geotrack.rspl.R;

import java.io.File;
import java.util.ArrayList;

public class ExternalStorageLogsActivity extends AppCompatActivity implements AdapterViewInterface {
    private RecyclerView recyclerView;
    private TextView noRecordFound;
    private SimpleRecyclerViewTypeAdapter<ExternalLogViewBean> recyclerViewAdapter;
    ArrayList<ExternalLogViewBean> listofFiles = new ArrayList<>();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_storage_logs);
        noRecordFound = (TextView) findViewById(R.id.no_record_found);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ConstantsUtils.initActionBarView(this, toolbar, true, "Logs", 0);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ExternalStorageLogsActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new SimpleRecyclerViewTypeAdapter<>(ExternalStorageLogsActivity.this, R.layout.ext_log_itemview, this, recyclerView, noRecordFound);
        recyclerView.setAdapter(recyclerViewAdapter);
        getLogFilesFromStorage();
    }

    private void getLogFilesFromStorage() {
        listofFiles.addAll(Constants.getLogFilesFromStorage());
        recyclerViewAdapter.refreshAdapter(listofFiles);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Object o, View view, int i) {

    }

    @Override
    public int getItemViewType(int i, ArrayList arrayList) {
        return 0;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i, View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View listItem= layoutInflater.inflate(R.layout.ext_log_itemview, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;     }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position, Object o, ArrayList arrayList) {
        final ExternalLogViewBean countBean = (ExternalLogViewBean) arrayList.get(position);
        ((ViewHolder) viewHolder).tvFileName.setText(countBean.getFileName());

        /*((ViewHolder) viewHolder).ll_mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri textUri = null;
                *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", countBean);
                } else {*//*
                File newFile = new File(countBean.getFilePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textUri = FileProvider.getUriForFile(ExternalStorageLogsActivity.this, getPackageName() + ".provider", newFile);
                    grantUriPermission(" com.arteriatech.ss.msecsales.rspl.visualaid", textUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    textUri = Uri.fromFile(newFile);
                }

                //  Uri uri = Uri.fromFile(textUri);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // Text file
                intent.setDataAndType(textUri, "text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Open File"));

            }
        });*/

        ((ViewHolder) viewHolder).ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri pdfUri = null;
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", countBean);
                } else {*/
                File newFile = new File(countBean.getFilePath());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pdfUri = FileProvider.getUriForFile(ExternalStorageLogsActivity.this, getPackageName() + ".provider", newFile);
                } else {
                    pdfUri = Uri.fromFile(newFile);
                }
               // }
                /*Intent share = new Intent();
                share.setAction(Intent.ACTION_SEND);
                share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM, pdfUri);
                startActivity(Intent.createChooser(share, "Share"));*/

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Log Files");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));




            }
        });
       /* if(countBean.getCount()>0){
            holder.tvPendingStatus.setBackgroundColor(context.getResources().getColor(R.color.RejectedColor));
        }else {
            holder.tvPendingStatus.setBackgroundColor(context.getResources().getColor(R.color.ApprovedColor));
        }
        holder.tvEntityName.setText(countBean.getCollection());
        holder.tvPendingCount.setText(""+countBean.getCount());
        holder.tvSyncTime.setText(""+countBean.getSyncTime());
//        holder.ivUploadDownload.setText("UPLOAD");
        holder.ivUploadDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (collectionSyncInterface!=null){
                    syncType = Constants.DownLoad;
                    collectionSyncInterface.onUploadDownload(false,countBean,syncType);
                }
            }
        });*/
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFileName;
        public ImageView ivShare;
        public LinearLayout ll_mainLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            tvFileName = (TextView)itemView.findViewById(R.id.tvEntityName);
            ivShare = (ImageView) itemView.findViewById(R.id.ivShare);
            ll_mainLayout = (LinearLayout) itemView.findViewById(R.id.ll_mainLayout);
        }
    }
}
