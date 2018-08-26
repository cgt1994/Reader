package com.syezon.reader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.syezon.reader.model.HistorySearchBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jin on 2016/9/26.
 */
public class HistoryDBHelper {

    private Context mContext;
    private SQLiteDatabase mDataBase;
    private HistorySearchDB mHistoryDB;

    public HistoryDBHelper(Context context) {
        mContext = context;
        mHistoryDB = new HistorySearchDB(context);
        openDB();
    }

    private void openDB() {
        if (mDataBase == null) {
            mDataBase = mHistoryDB.getWritableDatabase();
        }
    }

    private void closeDB() {
        if (mDataBase != null) {
            mDataBase.close();
        }
    }


    //添加搜索记录
    public boolean addHistory(HistorySearchBean bean) {
        HistorySearchBean oldBean=searchByName(bean.getSearchContent());
        if ( oldBean!= null) {
            oldBean.setSearchTime(bean.getSearchTime());
            return updateHistory(oldBean);
        } else {
            ContentValues values = new ContentValues();
            values.put(mHistoryDB.SEARCH_CONTENT, bean.getSearchContent());
            values.put(mHistoryDB.SEARCH_TIME, bean.getSearchTime());
            long id = mDataBase.insert(mHistoryDB.TABLE_NAME, mHistoryDB.SEARCH_CONTENT + mHistoryDB.SEARCH_TIME, values);
            return id > 0;
        }
    }

    //根据名称来查询
    public HistorySearchBean searchByName(String name) {
        Cursor cursor = mDataBase.query(mHistoryDB.TABLE_NAME, null, mHistoryDB.SEARCH_CONTENT + " = ?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            HistorySearchBean bean = new HistorySearchBean();
            bean.setSearchContent(cursor.getString(cursor.getColumnIndex(mHistoryDB.SEARCH_CONTENT)));
            bean.setId(cursor.getInt(cursor.getColumnIndex(mHistoryDB.ID)));
            cursor.close();
            return bean;
        }
        cursor.close();
        return null;

    }


    //获取搜素记录
    public List<HistorySearchBean> getHistoryList() {
        List<HistorySearchBean> list = new ArrayList<>();
        Cursor cursor = mDataBase.query(mHistoryDB.TABLE_NAME, null, null, null, null, null, mHistoryDB.SEARCH_TIME + " desc ", null);
        if (cursor.moveToFirst()) {
            do {
                HistorySearchBean bean = new HistorySearchBean();
                bean.setSearchContent(cursor.getString(cursor.getColumnIndex(mHistoryDB.SEARCH_CONTENT)));
                bean.setId(cursor.getInt(cursor.getColumnIndex(mHistoryDB.ID)));
                list.add(bean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    //修改历史记录的排序
    public boolean updateHistory(HistorySearchBean bean) {
        ContentValues values = new ContentValues();
        values.put(mHistoryDB.SEARCH_TIME, bean.getSearchTime());
        long id = mDataBase.update(mHistoryDB.TABLE_NAME, values, mHistoryDB.ID + " = ?", new String[]{bean.getId() + ""});
        return id > 0;
    }

    //删除历史记录
    public void deleteHistory(int id) {
        mDataBase.delete(mHistoryDB.TABLE_NAME, mHistoryDB.ID + " = ?", new String[]{id + ""});
    }

}
