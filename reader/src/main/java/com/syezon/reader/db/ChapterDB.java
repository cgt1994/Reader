package com.syezon.reader.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 创建书本章节相对应的的数据库
 * Created by jin on 2016/8/25.
 */
public class ChapterDB extends SQLiteOpenHelper {

    public static int VERSION = 1;

    public String TABLE_NAME;//数据库名称,以书名来命名,一本书一个数据库
    public String ID = "_id";//章节号
    public String BOOK_PAGE = "book_page";//章节所对应的页数
    public String BOOK_CHAPTER = "book_chapter";//章节标题
    public String BOOK_POSITION = "book_position";//书籍所在位置
    public String BOOK_START = "book_start";//章节开始位置
    public String BOOK_END = "book_end";//章节结束位置
    public String BOOK_READ = "book_read";//是否已读,0表示未读,1表示已读
    public String BOOK_LAST = "book_last";//是否是最后一章,0不是，1是

    public ChapterDB(Context context, String name) {
        super(context, name, null, VERSION);
        TABLE_NAME = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME + "(" + ID + " integer primary key," +
                BOOK_CHAPTER + " varchar(100)," +
                BOOK_POSITION + " text," +
                BOOK_PAGE + " integer," +
                BOOK_START + " integer," +
                BOOK_END + " integer," +
                BOOK_LAST + " integer," +
                BOOK_READ + " integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
            Log.e("TAG","chapter DB onUpgrade");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
