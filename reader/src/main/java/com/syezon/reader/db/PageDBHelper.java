package com.syezon.reader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.syezon.reader.model.PageBean;

/**
 * 书本索引帮助类
 * Created by jin on 2016/9/30.
 */
public class PageDBHelper {
    private PageDB mPageDB;
    private SQLiteDatabase mDataBase;
    private Context mContext;

    public PageDBHelper(Context context, String bookName) {
        mContext = context;
        mPageDB = new PageDB(context, "page_" + bookName);
        openDB();
        //Log.e("TAG", "open db:" + bookName);
    }

    public void openDB() {
        try {
            if (mDataBase == null) {
                mDataBase = mPageDB.getWritableDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeDB() {
        if (mDataBase != null) {
            mDataBase.close();
        }
    }

    //写入页信息
    public boolean writePage(PageBean bean) {
        if (hasPageInfo(bean.getChapterId(), bean.getPage())) {
            return updatePageInfo(bean);
        } else {
            ContentValues values = new ContentValues();
            values.put(mPageDB.PAGE, bean.getPage());
            values.put(mPageDB.CHAPTER_ID, bean.getChapterId());
            values.put(mPageDB.PAGE_START_LINE, bean.getPageStartLine());
            values.put(mPageDB.PAGE_END_LINE, bean.getPageEndLine());
            values.put(mPageDB.PAGE_START_CHAR, bean.getPageStartChar());
            values.put(mPageDB.PAGE_END_CHAR, bean.getPageEndChar());
            values.put(mPageDB.IS_END, bean.getIsEnd());
            long id = mDataBase.insert(mPageDB.TABLE_NAME, mPageDB.PAGE +
                    mPageDB.CHAPTER_ID + mPageDB.PAGE_START_LINE + mPageDB.PAGE_END_LINE + mPageDB.PAGE_START_CHAR + mPageDB.PAGE_END_CHAR + mPageDB.IS_END, values);
            return id > 0;
        }
    }

    //更新页信息
    public boolean updatePageInfo(PageBean bean) {
        ContentValues values = new ContentValues();
        values.put(mPageDB.PAGE, bean.getPage());
        values.put(mPageDB.CHAPTER_ID, bean.getChapterId());
        values.put(mPageDB.PAGE_START_LINE, bean.getPageStartLine());
        values.put(mPageDB.PAGE_END_LINE, bean.getPageEndLine());
        values.put(mPageDB.PAGE_START_CHAR, bean.getPageStartChar());
        values.put(mPageDB.PAGE_END_CHAR, bean.getPageEndChar());
        values.put(mPageDB.IS_END, bean.getIsEnd());
        long id = mDataBase.update(mPageDB.TABLE_NAME, values, mPageDB.CHAPTER_ID + " = ? and " + mPageDB.PAGE + " = ?", new String[]{bean.getChapterId() + "", bean.getPage() + ""});
        return id > 0;
    }

    //删除多余页信息,字体改变之后章节的页信息发生改变,会出现多余页信息
    public void deletePage(int chapter, int page) {
        //Log.e("TAG","delete page where chapter ="+chapter+" and page >"+page);
        mDataBase.delete(mPageDB.TABLE_NAME, mPageDB.CHAPTER_ID + " = ? and " + mPageDB.PAGE + " > ?", new String[]{chapter + "", page + ""});
    }

    //是否有该章该页信息
    public boolean hasPageInfo(int chapter, int page) {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, null, mPageDB.CHAPTER_ID + " = ? and " + mPageDB.PAGE + " = ?", new String[]{chapter + "", page + ""}, null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    //查询页信息
    public PageBean getPageInfo(int chapter, int page) {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, null, mPageDB.PAGE + " = ? and " + mPageDB.CHAPTER_ID + " = ?", new String[]{page + "", chapter + ""}, null, null, null, null);
        if (cursor.moveToFirst()) {
            PageBean bean = new PageBean();
            bean.setPage(page);
            bean.setChapterId(cursor.getInt(cursor.getColumnIndex(mPageDB.CHAPTER_ID)));
            bean.setPageStartLine(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_START_LINE)));
            bean.setPageEndLine(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_END_LINE)));
            bean.setPageStartChar(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_START_CHAR)));
            bean.setPageEndChar(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_END_CHAR)));
            bean.setIsEnd(cursor.getInt(cursor.getColumnIndex(mPageDB.IS_END)));
            cursor.close();
            return bean;
        } else {
            cursor.close();
            return null;
        }
    }

    //查询页信息,内置小说和wifi传书的时候
    public PageBean getPageInfo(int page) {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, null, mPageDB.PAGE + " = ? ", new String[]{page + ""}, null, null, null, null);
        if (cursor.moveToFirst()) {
            PageBean bean = new PageBean();
            bean.setPage(page);
            bean.setChapterId(cursor.getInt(cursor.getColumnIndex(mPageDB.CHAPTER_ID)));
            bean.setPageStartLine(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_START_LINE)));
            bean.setPageEndLine(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_END_LINE)));
            bean.setPageStartChar(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_START_CHAR)));
            bean.setPageEndChar(cursor.getInt(cursor.getColumnIndex(mPageDB.PAGE_END_CHAR)));
            bean.setIsEnd(cursor.getInt(cursor.getColumnIndex(mPageDB.IS_END)));
            cursor.close();
            return bean;
        } else {
            cursor.close();
            return null;
        }
    }

    //获取最后的缓存章节信息
    public int getLastChapter() {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, new String[]{mPageDB.CHAPTER_ID}, null, null, null, null, mPageDB.PAGE + " desc", null);
        if (cursor.moveToFirst()) {
            int lastChapter = cursor.getInt(cursor.getColumnIndex(mPageDB.CHAPTER_ID));
            cursor.close();
            return lastChapter;
        } else {
            cursor.close();
            return 0;
        }
    }

    //查询这一章的总页数
    public int getTotalPagesByChapter(int chapter) {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, new String[]{mPageDB.PAGE},
                mPageDB.CHAPTER_ID + " = ?", new String[]{chapter + ""}, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //查询总页数
    public int getTotalPages() {
        Cursor cursor = mDataBase.query(mPageDB.TABLE_NAME, new String[]{mPageDB.PAGE}, null, null, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //删除页信息
    public void delete() {
        Log.e("TAG", "delete page db:" + mPageDB.TABLE_NAME);
        mDataBase.delete(mPageDB.TABLE_NAME, null, null);
    }
}
