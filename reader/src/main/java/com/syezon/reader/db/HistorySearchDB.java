package com.syezon.reader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 搜素历史表
 * Created by jin on 2016/9/26.
 */
public class HistorySearchDB extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "history_search";
    public static final int VERSION = 1;

    public static final String ID = "id";
    public static final String SEARCH_CONTENT = "search_content";//搜索内容
    public static final String SEARCH_TIME = "search_time";//搜索时间

    public HistorySearchDB(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + "(" + ID + " integer primary key," +
                SEARCH_CONTENT + " text," +
                SEARCH_TIME + " text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
