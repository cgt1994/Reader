package com.syezon.reader.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 创建书本索引
 * Created by jin on 2016/8/25.
 */
public class PageDB extends SQLiteOpenHelper {

    public static int VERSION = 1;

    public String TABLE_NAME;//数据库名称,以书名来命名,一本书一个数据库
    public String PAGE = "page";//页数
    public String CHAPTER_ID = "chapter_id";//章节号
    public String PAGE_START_LINE = "page_start_line";//页开始的行
    public String PAGE_END_LINE = "page_end_line";//页结束的行
    public String PAGE_START_CHAR = "page_start_char";//页开始位置
    public String PAGE_END_CHAR = "page_end_char";//页结束位置
    //    public String PAGE_CUT="page_cut";//该页被减去的部分
    public String IS_END = "is_end";//最后一页

    public PageDB(Context context, String name) {
        super(context, name, null, VERSION);
        TABLE_NAME = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + "( _id integer primary key AUTOINCREMENT," + PAGE + " integer," +
                CHAPTER_ID + " integer," +
                PAGE_START_LINE + " integer," +
                PAGE_END_LINE + " integer," +
                PAGE_START_CHAR + " integer," +
                PAGE_END_CHAR + " integer," +
                IS_END + " integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
            Log.e("TAG","drop table:"+TABLE_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
