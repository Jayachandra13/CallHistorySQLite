package com.example.contcatlist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.contcatlist.model.CallRecord;

import java.util.ArrayList;

public class CallHistorySQLiteDbHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "CallDB";
    private static final String TABLE_CALL_LIST = "CallList";

    private static final String COLUMN_KEY_ID = "id";
    private static final String COLUMN_PHONE = "PhoneNumber";
    private static final String COLUMN_DURATION = "Duration";

    @RequiresApi(api = Build.VERSION_CODES.P)
    public CallHistorySQLiteDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COUNTRY_TABLE = "CREATE TABLE " + TABLE_CALL_LIST + "("
                + COLUMN_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_PHONE + " TEXT,"
                + COLUMN_DURATION + " LONG" + ")";
        db.execSQL(CREATE_COUNTRY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_LIST);
        onCreate(db);
    }

    public void addCallRecord(CallRecord callRecord) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHONE, callRecord.getPhoneNumber());
        values.put(COLUMN_DURATION, callRecord.getDuration());

        db.insert(TABLE_CALL_LIST, null, values);
        db.close();
    }

    public ArrayList<CallRecord> getAllCallRecords() {
        ArrayList<CallRecord> callList = new ArrayList<CallRecord>();
        String selectQuery = "SELECT  * FROM " + TABLE_CALL_LIST;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                CallRecord callRecord = new CallRecord();
                callRecord.setPhoneNumber(cursor.getString(1));
                callRecord.setDuration(cursor.getLong(2));
                callList.add(callRecord);
            } while (cursor.moveToNext());
        }
        return callList;
    }

    public void deleteAllCallRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CALL_LIST, null, null);
        db.close();
    }
}
