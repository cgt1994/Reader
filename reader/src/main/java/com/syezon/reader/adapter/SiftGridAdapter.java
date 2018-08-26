package com.syezon.reader.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.model.NovelBean;

import java.util.List;

/**
 * 相似图书的girdView的adapter
 * Created by jin on 2016/9/14.
 */
public class SiftGridAdapter extends BaseAdapter {

    private Context mContext;
    private List<NovelBean> mData;
    private int mScreenWidth;
    private int mScreenHeight;

    public SiftGridAdapter(Context context, List<NovelBean> data) {
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
        return mData.size() > 6 ? 6 : mData.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_view_sift_grid, parent, false);
            holder = new ViewHolder();
            holder.tv_book = (TextView) convertView.findViewById(R.id.tv_sift);
            holder.img_book = (ImageView) convertView.findViewById(R.id.iv_sift);
           // ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(mScreenWidth / 3 - 20, Tools.dp2px(mContext, 150));
            //holder.img_book.setLayoutParams(params);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext).load(mData.get(position).getImg()).
                placeholder(mContext.getResources().getDrawable(R.mipmap.error_pic)).into(holder.img_book);
        holder.tv_book.setText(mData.get(position).getName());
        return convertView;
    }

    public static class ViewHolder {
        ImageView img_book;
        TextView tv_book;
    }
}
