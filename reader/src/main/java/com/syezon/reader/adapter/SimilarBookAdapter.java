package com.syezon.reader.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.model.NovelBean;
import com.syezon.reader.utils.Tools;

import java.util.List;

/**
 * 相似图书的girdView的adapter
 * Created by jin on 2016/9/14.
 */
public class SimilarBookAdapter extends BaseAdapter {

    private Context mContext;
    private List<NovelBean> mData;
    private int mScreenWidth;
    private int mScreenHeight;

    public SimilarBookAdapter(Context context, List<NovelBean> data) {
        mContext = context;
        mData = data;
        WindowManager m = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        m.getDefaultDisplay().getMetrics(metrics);
        mScreenHeight = metrics.heightPixels;
        mScreenWidth = metrics.widthPixels;
    }

    @Override
    public int getCount() {
        return mData.size() > 4 ? 4 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_view_similarbook, parent, false);
            holder = new ViewHolder();
            holder.img_book = (ImageView) convertView.findViewById(R.id.img_book);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(mScreenWidth / 4 - 20, Tools.dp2px(mContext, 150));
            holder.img_book.setLayoutParams(params);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext).load(mData.get(position).getImg()).
                placeholder(mContext.getResources().getDrawable(R.mipmap.error_pic)).into(holder.img_book);
        return convertView;
    }

    public static class ViewHolder {
        ImageView img_book;
    }
}
