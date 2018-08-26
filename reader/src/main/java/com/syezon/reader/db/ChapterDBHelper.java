package com.syezon.reader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.utils.FileUtils;
import com.syezon.reader.utils.SPHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 书本章节数据库的帮助类
 * Created by jin on 2016/8/25.
 */
public class ChapterDBHelper {

    private ChapterDB mBookDB;
    private SQLiteDatabase mDataBase;
    private Context mContext;

    public ChapterDBHelper(Context context, String bookName) {
        mContext = context;
        mBookDB = new ChapterDB(context, "book_" + bookName);
        openDB();
    }

    public void openDB() {
        try {//当一本书被清理之后，如果未再进行缓存这这本书的数据库会不存在
            if (mDataBase == null) {
                mDataBase = mBookDB.getWritableDatabase();
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

    //向数据库中写入数据
    public boolean writeBook(ChapterBean book) {
        ChapterBean bean = readBookOneChapter(book.getChapterId());
        //如果存在则修改，否则就插入
        if (bean != null) {
            bean.setChapterName(bean.getChapterName());
            bean.setPage(book.getPage());
            bean.setChapterStart(book.getChapterStart());
            bean.setChapterEnd(book.getChapterEnd());
            bean.setIsLastChapter(book.getIsLastChapter());
            bean.setChapterPosition(book.getChapterPosition());
            return updateBookChapter(bean);
        } else {
            ContentValues values = new ContentValues();
            values.put(mBookDB.ID, book.getChapterId());
            values.put(mBookDB.BOOK_CHAPTER, book.getChapterName());
            values.put(mBookDB.BOOK_POSITION, book.getChapterPosition());
            values.put(mBookDB.BOOK_START, book.getChapterStart());
            values.put(mBookDB.BOOK_END, book.getChapterEnd());
            values.put(mBookDB.BOOK_PAGE, book.page);
            values.put(mBookDB.BOOK_READ, book.getIsRead());
            values.put(mBookDB.BOOK_LAST, book.getIsLastChapter());
            long id = mDataBase.insert(mBookDB.TABLE_NAME, mBookDB.ID +
                    mBookDB.BOOK_CHAPTER + mBookDB.BOOK_POSITION + mBookDB.BOOK_START + mBookDB.BOOK_END + mBookDB.BOOK_READ + mBookDB.BOOK_PAGE + mBookDB.BOOK_LAST, values);
            return id > 0;
        }
    }

    //读取一章内容
    public ChapterBean readBookOneChapter(int chapterId) {
        if (mDataBase != null) {
            Cursor cursor = mDataBase.query(mBookDB.TABLE_NAME, null, "_id = ?", new String[]{chapterId + ""}, null, null, null, null);
            if (cursor.moveToFirst()) {
                ChapterBean book = new ChapterBean();
                book.setBookName(mBookDB.TABLE_NAME);
                book.setChapterName(cursor.getString(cursor.getColumnIndex(mBookDB.BOOK_CHAPTER)));
                book.setChapterId(cursor.getInt(cursor.getColumnIndex(mBookDB.ID)));
                book.setChapterPosition(cursor.getString(cursor.getColumnIndex(mBookDB.BOOK_POSITION)));
                book.setChapterStart(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_START)));
                book.setChapterEnd(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_END)));
                book.page = cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_PAGE));
                book.setIsRead(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_READ)));
                book.setIsLastChapter(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_LAST)));
                cursor.close();
                return book;
            } else {
                cursor.close();
                return null;
            }
        } else {
            return null;
        }
    }

    //读取章节列表
    public List<ChapterBean> readBookChapterList() {
        Cursor cursor = mDataBase.query(mBookDB.TABLE_NAME, new String[]{mBookDB.ID, mBookDB.BOOK_CHAPTER, mBookDB.BOOK_START, mBookDB.BOOK_PAGE, mBookDB.BOOK_END, mBookDB.BOOK_LAST, mBookDB.BOOK_POSITION},
                null, null, null, null, mBookDB.ID + " asc", null);
        if (cursor.moveToFirst()) {
            List<ChapterBean> list = new ArrayList<>();
            do {
                ChapterBean book = new ChapterBean();
                book.setChapterId(cursor.getInt(cursor.getColumnIndex(mBookDB.ID)));
                book.setChapterName(cursor.getString(cursor.getColumnIndex(mBookDB.BOOK_CHAPTER)));
                book.setChapterStart(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_START)));
                book.setChapterEnd(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_END)));
                book.page = cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_PAGE));
                book.setIsLastChapter(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_LAST)));
                book.setChapterPosition(cursor.getString(cursor.getColumnIndex(mBookDB.BOOK_POSITION)));
                list.add(book);
            } while (cursor.moveToNext());
            cursor.close();
            return list;
        } else {
            cursor.close();
            return null;
        }
    }

    //读取一部分章节列表
    public List<ChapterBean> readBookChapterList(int start, int size) {
        Cursor cursor = mDataBase.query(mBookDB.TABLE_NAME,
                new String[]{mBookDB.ID, mBookDB.BOOK_CHAPTER, mBookDB.BOOK_START, mBookDB.BOOK_END, mBookDB.BOOK_PAGE, mBookDB.BOOK_LAST},
                null, null, null, null, mBookDB.ID + " asc limit " + size + " offset " + start);
        if (cursor.moveToFirst()) {
            List<ChapterBean> list = new ArrayList<>();
            do {
                ChapterBean book = new ChapterBean();
                book.setChapterId(cursor.getInt(cursor.getColumnIndex(mBookDB.ID)));
                book.setChapterName(cursor.getString(cursor.getColumnIndex(mBookDB.BOOK_CHAPTER)));
                book.setChapterStart(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_START)));
                book.setChapterEnd(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_END)));
                book.page = cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_PAGE));
                book.setIsLastChapter(cursor.getInt(cursor.getColumnIndex(mBookDB.BOOK_LAST)));
                list.add(book);
            } while (cursor.moveToNext());
            cursor.close();
            return list;
        } else {
            cursor.close();
            return null;
        }
    }

    //返回最后一章章节的id
    public int getLastChapterNO() {
        Cursor cursor = mDataBase.query(mBookDB.TABLE_NAME, new String[]{mBookDB.ID},
                null, null, null, null, mBookDB.ID + " desc", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                //最后一章id加1
                int chapterNO = cursor.getInt(cursor.getColumnIndex(mBookDB.ID)) + 1;
                cursor.close();
                return chapterNO;
            }
            return 1;
        }
        return 1;
    }

    //更新章节信息
    public boolean updateBookChapter(ChapterBean bean) {
        ContentValues values = new ContentValues();
        values.put(mBookDB.BOOK_PAGE, bean.getPage());
        values.put(mBookDB.BOOK_START, bean.getChapterStart());
        values.put(mBookDB.BOOK_END, bean.getChapterEnd());
        values.put(mBookDB.BOOK_LAST, bean.getIsLastChapter());
        long id = mDataBase.update(mBookDB.TABLE_NAME, values, mBookDB.BOOK_CHAPTER + " = ?", new String[]{bean.getChapterName()});
        return id > 0;
    }

    //删除书本
    public void delete() {
        Log.e("TAG", "delete chapter db:" + mBookDB.TABLE_NAME);
        SPHelper.clearSP(mContext, mBookDB.TABLE_NAME.substring(mBookDB.TABLE_NAME.indexOf("_") + 1));
        FileUtils.deleteFile(mBookDB.TABLE_NAME);
        mDataBase.delete(mBookDB.TABLE_NAME, null, null);
    }
}
