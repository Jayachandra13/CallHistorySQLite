package com.example.contcatlist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contcatlist.database.SQLiteDatabaseHandler;
import com.example.contcatlist.model.CallRecord;
import com.example.contcatlist.view.CallRCVAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    TextView textView;
    Button btnCall, btnCallLogs;
    EditText etPhoneNumber;
    Activity activity;
    ArrayList countries;
    SQLiteDatabaseHandler db;
    RecyclerView rcv;
    Boolean callInitiated = false;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCall = findViewById(R.id.btnCall);
        btnCallLogs = findViewById(R.id.btnCallLogs);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        rcv = findViewById(R.id.rcvCallList);


        activity = this;
        db = new SQLiteDatabaseHandler(this);

        rcv.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        rcv.setLayoutManager(linearLayoutManager);

        requestContactPermission();

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = etPhoneNumber.getText().toString();
                if (number.length() == 10) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
                    callInitiated = true;
                } else {
                    etPhoneNumber.setError("Please enter 10 digit mobile number");
                }
            }
        });
        btnCallLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastCall();
            }
        });

    }

    @Override
    protected void onResume() {
        if (callInitiated) {
//            lastCall();
        }
        super.onResume();
    }

    public void lastCall() {

        Uri contacts = CallLog.Calls.CONTENT_URI;

        try {
            Cursor managedCursor = MainActivity.this.getContentResolver().query(contacts, null, null, null, android.provider.CallLog.Calls.DATE + " DESC limit 10;");
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);

            if (managedCursor.getCount() > 0) {

                db.deleteAllCallRecords();
                while (managedCursor.moveToNext()) {
                    String callDate = managedCursor.getString(date);
                    String callDuration = managedCursor.getString(duration);
                    String phNumber = managedCursor.getString(number);
                    Log.d("##Log", phNumber + " " + " " + callDuration);
                    CallRecord mCallRecord = new CallRecord();
                    mCallRecord.setPhoneNumber(phNumber);
                    mCallRecord.setDuration(Long.parseLong(callDuration));
                    db.addCallRecord(mCallRecord);
                }
                getCallRecordsFromDB();
            } else {
                Log.d("##Log", "No Call logs found");
            }
            managedCursor.close();
        } catch (SecurityException e) {
            Log.e("Security Exception", e.toString());

        }


    }

    private void getCallRecordsFromDB() {
        ArrayList<CallRecord> list = db.getAllCallRecords();
        rcv.setAdapter(new CallRCVAdapter(this, list, rcv));
    }

    public void requestContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
                        }
                    });
                    builder.show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG},
                            PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
//                lastCall();
            }
        } else {
//            lastCall();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    lastCall();
                } else {
                    Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        db.deleteAllCallRecords();
        super.onDestroy();
    }
}