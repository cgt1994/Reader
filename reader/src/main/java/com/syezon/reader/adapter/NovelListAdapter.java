package com.syezon.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.model.NovelBean;

import java.util.List;

/**
 * 小说列表的适配器
 * Created by jin on 2016/9/26.
 */
public class NovelListAdapter extends RecyclerView.Adapter<NovelListAdapter.ViewHolder> {

    private Context mContext;
    private List<NovelBean> mData;

    public NovelListAdapter(Context context, List<NovelBean> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_novel_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Picasso.with(mContext).load(mData.get(position).getImg()).placeholder(mContext.getResources().getDrawable(R.mipmap.error_pic)).into(holder.iv_book);
        holder.tv_name.setText(mData.get(position).getName());
        holder.tv_author.setText(mData.get(position).getAuthor());
        holder.tv_chapter.setText(mData.get(position).getChapter());
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_book;
        private TextView tv_name;
        private TextView tv_author;
        private TextView tv_chapter;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_book = (ImageView) itemView.findViewById(R.id.book_img);
            tv_name = (TextView) itemView.findViewById(R.id.book_name);
            tv_author = (TextView) itemView.findViewById(R.id.book_author);
            tv_chapter = (TextView) itemView.findViewById(R.id.book_chapter);
        }
    }
}
