package com.example.contcatlist;

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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contcatlist.database.CallHistorySQLiteDbHandler;
import com.example.contcatlist.model.CallRecord;
import com.example.contcatlist.view.CallRCVAdapter;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    Button btnCall, btnCallLogs;
    EditText etPhoneNumber;
    Activity activity;
    CallHistorySQLiteDbHandler db;
    RecyclerView rcv;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Views declarations
        btnCall = findViewById(R.id.btnCall);
        btnCallLogs = findViewById(R.id.btnCallLogs);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        rcv = findViewById(R.id.rcvCallList);

//        SQLite DB initialization
        activity = this;
        db = new CallHistorySQLiteDbHandler(this);

//        RecyclerView
        rcv.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        rcv.setLayoutManager(linearLayoutManager);

//      check and request run-time permissions
        requestContactPermission();

//        Call and Call Logs OnClick listeners
        btnCall.setOnClickListener(v -> {
            String number = etPhoneNumber.getText().toString();
            if (number.length() == 10) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
            } else {
                etPhoneNumber.setError("Please enter valid 10 digit mobile number");
            }
        });
        btnCallLogs.setOnClickListener(v -> getCallHistoryRecords());
    }

    public void getCallHistoryRecords() {
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
                    Log.v("callStart:","callDate->"+getDate(Long.parseLong(callDate)));
                    String callDuration = managedCursor.getString(duration);
                    Long endTime = addTimeStamp(Long.parseLong(callDate),Integer.parseInt(callDuration));
                    Log.v("callEnd:","callDate->"+getDate(endTime));
                    String phNumber = managedCursor.getString(number);
                    Log.d("##Log", phNumber + " " + " " + callDuration);
                    CallRecord mCallRecord = new CallRecord();
                    mCallRecord.setPhoneNumber(phNumber);
                    mCallRecord.setDuration(Long.parseLong(callDuration));
                    mCallRecord.setStartTime(getDate(Long.parseLong(callDate)));
                    mCallRecord.setEndTime(getDate(endTime));
                    db.addCallRecord(mCallRecord);
                }
                getCallRecordsFromDB();
            } else {
                Log.d("##Log", "No Call logs found");
                Toast.makeText(this, "No call logs found", Toast.LENGTH_SHORT).show();
            }
            managedCursor.close();
        } catch (SecurityException e) {
            Log.e("Security Exception", e.toString());
            Toast.makeText(this, "Security Exception: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy hh:mm:ss a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    public Long addTimeStamp(Long startDate, Integer durationSec){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDate);
        cal.add(Calendar.SECOND, durationSec);
        return cal.getTimeInMillis();
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
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "You have disabled a contacts permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        db.deleteAllCallRecords();
        super.onDestroy();
    }
}
