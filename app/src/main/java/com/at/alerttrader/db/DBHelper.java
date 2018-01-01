package com.at.alerttrader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lenovo on 22-12-2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "TRADE_ALERT : DBHelper";
    public static final String TRADE_ALERT_DB_NAME = "trade_alert";
    public static final int DB_VERSION = 4;
    public static final String CREATE_QUERY = "create table "+MessageContract.MessagesDS.MESSAGES_TABLE_NAME+
            " ( "+ MessageContract.MessagesDS.MESSAGE+" TEXT,"+ MessageContract.MessagesDS.MESSAGE_TIME+ " INTEGER, "+
            MessageContract.MessagesDS.MESSAGE_STATUS + " TEXT, "+ MessageContract.MessagesDS.MESSAGE_USER+ " Text);";
    public static final String DROP_QUERY = " DROP TABLE IF EXISTS "+MessageContract.MessagesDS.MESSAGES_TABLE_NAME;
    public DBHelper(Context context){
        super(context,TRADE_ALERT_DB_NAME,null,DB_VERSION);
        Log.d(TAG, "DBHelper : Constructor : Database created..");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate query : "+CREATE_QUERY);
        sqLiteDatabase.execSQL(CREATE_QUERY);
        Log.d(TAG, "onCreate: table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_QUERY);
        Log.d(TAG, "onUpgrade: database deleted");
    }

    public long putMessages(String message, long message_time, String message_status, String message_user, SQLiteDatabase sqLiteDatabase){
        ContentValues contentValue =  new ContentValues();
        contentValue.put(MessageContract.MessagesDS.MESSAGE,message);
        contentValue.put(MessageContract.MessagesDS.MESSAGE_TIME,message_time);
        contentValue.put(MessageContract.MessagesDS.MESSAGE_STATUS,message_status);
        contentValue.put(MessageContract.MessagesDS.MESSAGE_USER,message_user);
        long l = sqLiteDatabase.insert(MessageContract.MessagesDS.MESSAGES_TABLE_NAME,null,contentValue);
        Log.d(TAG, "putMessages: One row inserted"+l);
        return l;
    }

    public Cursor getMessages(SQLiteDatabase sqLiteDatabase){
        String[] projection = {MessageContract.MessagesDS.MESSAGE, MessageContract.MessagesDS.MESSAGE_TIME, MessageContract.MessagesDS.MESSAGE_STATUS, MessageContract.MessagesDS.MESSAGE_USER};
        Cursor cursor = sqLiteDatabase.query(MessageContract.MessagesDS.MESSAGES_TABLE_NAME,projection,null,null,null,null, MessageContract.MessagesDS.MESSAGE_TIME,null);
        return cursor;
    }

    public Integer deleteMessage(SQLiteDatabase sqLiteDatabase,long time){
        return sqLiteDatabase.delete(MessageContract.MessagesDS.MESSAGES_TABLE_NAME, MessageContract.MessagesDS.MESSAGE_TIME+" < ?",
                new String[]{String.valueOf(time)});

    }
}














