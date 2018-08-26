package com.syezon.reader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 书架数据库
 * Created by jin on 2016/9/14.
 */
public class BookCaseDB extends SQLiteOpenHelper {

    public static int VERSION = 1;
    public static String TABLE_NAME = "bookcase";
    public static String BOOK_ID = "book_id";//书本的id
    public static String BOOK_IMG = "book_img";//书本的封面
    public static String BOOK_NAME = "book_name";//书本的名字
    public static String BOOK_AUTHOR = "book_author";//书本的作者
    public static String BOOK_UPDATE_TIME = "book_update_time";//书本的出版时间
    public static String BOOK_LAST_CHAPTER = "book_last_chapter";//最新章节
    public static String BOOK_LAST_CHAPTER_NO = "book_last_chapter_no";//最新章节的id
    public static String ADD_TIME = "add_time";//添加时间
    public static String CACHE = "is_cache";//缓存章节数，-1表示缓存到最后
    public static String BOOK_TYPE = "book_type";//书本类型,0内置,1网络,2wifi传书
    public static String IS_FULL = "is_full";//是否完本，0未完本，1完本

    public BookCaseDB(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_NAME + "(" + BOOK_ID + " integer ," +
                BOOK_IMG + " text," + BOOK_NAME + " text primary key," + BOOK_AUTHOR + " text," +
                BOOK_UPDATE_TIME + " text," + BOOK_LAST_CHAPTER + " text," + BOOK_LAST_CHAPTER_NO + " integer," + ADD_TIME + " text," +
                CACHE + " integer," + BOOK_TYPE + " integer," + IS_FULL + " integer )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if table exits " + TABLE_NAME;
        db.execSQL(sql);
    }
}
