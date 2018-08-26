package com.syezon.reader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.syezon.reader.model.BookCaseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jin on 2016/9/14.
 */
public class BookCaseDBHelper {
    public static final int BOOK_TOP = 1;//置顶
    public static final int BOOK_CACHE = 2;//缓存
    public static final int BOOK_CHECK_UPDATE = 3;//检查更新

    private BookCaseDB mBookCaseDB;
    private SQLiteDatabase mDataBase;

    public BookCaseDBHelper(Context context) {
        mBookCaseDB = new BookCaseDB(context);
        openDB();
    }

    private void openDB() {
        if (mDataBase == null) {
            mDataBase = mBookCaseDB.getWritableDatabase();
        }
    }

    public void closeDB() {
        if (mDataBase != null) {
            mDataBase.close();
        }
    }

    //添加到书架
    public boolean addToBookCase(BookCaseBean bean) {

        Log.e("add", "times");
        ContentValues values = new ContentValues();
        values.put(mBookCaseDB.BOOK_ID, bean.getBook_id());
        values.put(mBookCaseDB.BOOK_IMG, bean.getBook_img());
        values.put(mBookCaseDB.BOOK_NAME, bean.getBook_name());
        values.put(mBookCaseDB.BOOK_AUTHOR, bean.getBook_author());
        values.put(mBookCaseDB.BOOK_UPDATE_TIME, bean.getBook_update_time());
        values.put(mBookCaseDB.BOOK_LAST_CHAPTER, bean.getLast_chapter());
        values.put(mBookCaseDB.ADD_TIME, bean.getAdd_time());
        values.put(mBookCaseDB.CACHE, bean.getCache());
        values.put(mBookCaseDB.BOOK_TYPE, bean.getBook_type());
        values.put(mBookCaseDB.IS_FULL, bean.getIs_full());
        long id = mDataBase.insert(mBookCaseDB.TABLE_NAME, mBookCaseDB.BOOK_ID + mBookCaseDB.BOOK_IMG + mBookCaseDB.BOOK_NAME +
                mBookCaseDB.BOOK_AUTHOR + mBookCaseDB.BOOK_UPDATE_TIME + mBookCaseDB.BOOK_LAST_CHAPTER + mBookCaseDB.ADD_TIME + mBookCaseDB.CACHE + mBookCaseDB.BOOK_TYPE + mBookCaseDB.IS_FULL, values);
        return id > 0;
    }

    //判读书籍是否已经添加
    public BookCaseBean selectBookCaseByName(String name) {
        Cursor cursor = mDataBase.query(mBookCaseDB.TABLE_NAME, null, mBookCaseDB.BOOK_NAME + " = ?", new String[]{name}, null, null, null, null);
        if (cursor.moveToFirst()) {
            BookCaseBean bean = new BookCaseBean();
            bean.setBook_id(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.BOOK_ID)));
            bean.setBook_img(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_IMG)));
            bean.setBook_name(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_NAME)));
            bean.setBook_author(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_AUTHOR)));
            bean.setBook_update_time(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_UPDATE_TIME)));
            bean.setLast_chapter(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_LAST_CHAPTER)));
            bean.setCache(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.CACHE)));
            bean.setLast_chapter_no(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.BOOK_LAST_CHAPTER_NO)));
            bean.setBook_type(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.BOOK_TYPE)));
            bean.setIs_full(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.IS_FULL)));
            cursor.close();
            return bean;
        }
        cursor.close();
        return null;
    }

    //查询书架
    public List<BookCaseBean> queryBookCase() {
        List<BookCaseBean> list = new ArrayList<>();
        //按添加时间倒序
        Cursor cursor = mDataBase.query(mBookCaseDB.TABLE_NAME, null, null, null, null, null, mBookCaseDB.ADD_TIME + " desc ", null);
        if (cursor.moveToFirst()) {
            do {
                BookCaseBean bean = new BookCaseBean();
                bean.setBook_id(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.BOOK_ID)));
                bean.setBook_img(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_IMG)));
                bean.setBook_name(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_NAME)));
                bean.setBook_author(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_AUTHOR)));
                bean.setBook_update_time(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_UPDATE_TIME)));
                bean.setLast_chapter(cursor.getString(cursor.getColumnIndex(mBookCaseDB.BOOK_LAST_CHAPTER)));
                bean.setCache(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.CACHE)));
                bean.setBook_type(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.BOOK_TYPE)));
                bean.setIs_full(cursor.getInt(cursor.getColumnIndex(mBookCaseDB.IS_FULL)));
                list.add(bean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    //查询当前缓存章节
    public int getLastBookCache(String name) {
        Cursor cursor = mDataBase.query(mBookCaseDB.TABLE_NAME, new String[]{mBookCaseDB.CACHE}, mBookCaseDB.BOOK_NAME + " = ?", new String[]{name}, null, null, null, null);
        if (cursor.moveToFirst()) {
            int cache = cursor.getInt(cursor.getColumnIndex(mBookCaseDB.CACHE));
            cursor.close();
            return cache;
        } else {
            cursor.close();
            return 0;
        }
    }

    //修改书架内容
    public boolean updateBookCase(BookCaseBean bean, int opt) {
        if (opt == BOOK_TOP) {
            ContentValues values = new ContentValues();
            values.put(mBookCaseDB.ADD_TIME, System.currentTimeMillis());
            long id = mDataBase.update(mBookCaseDB.TABLE_NAME, values, mBookCaseDB.BOOK_NAME + " = ?", new String[]{bean.getBook_name()});
            return id > 0;
        } else if (opt == BOOK_CACHE) {
            ContentValues values = new ContentValues();
            values.put(mBookCaseDB.CACHE, bean.getCache());
            long id = mDataBase.update(mBookCaseDB.TABLE_NAME, values, mBookCaseDB.BOOK_NAME + " = ?", new String[]{bean.getBook_name()});
            return id > 0;
        } else {
            ContentValues values = new ContentValues();
            values.put(mBookCaseDB.BOOK_LAST_CHAPTER_NO, bean.getLast_chapter_no());
            values.put(mBookCaseDB.BOOK_LAST_CHAPTER, bean.getLast_chapter());
            long id = mDataBase.update(mBookCaseDB.TABLE_NAME, values, mBookCaseDB.BOOK_ID + " = ?", new String[]{bean.getBook_id() + ""});
            return id > 0;
        }
    }

    //删除书架内容
    public boolean deleteBookCase(String bookName) {
        long id = mDataBase.delete(mBookCaseDB.TABLE_NAME, mBookCaseDB.BOOK_NAME + " = ?", new String[]{bookName});
        return id > 0;
    }
}
