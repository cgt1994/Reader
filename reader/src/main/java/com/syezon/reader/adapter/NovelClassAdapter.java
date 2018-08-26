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
import com.syezon.reader.model.NovelClassBean;

import java.util.List;

/**
 * 分类的适配器
 * Created by jin on 2016/9/26.
 */
public class NovelClassAdapter extends RecyclerView.Adapter<NovelClassAdapter.ViewHolder> {

    private Context mContext;
    private List<NovelClassBean> mData;


    public interface ItemClickListener {
        void onItemClick(int position);
    }

    private ItemClickListener mListener;

    public void setOnItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public NovelClassAdapter(Context context, List<NovelClassBean> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Picasso.with(mContext).load(mData.get(position).getImg()).placeholder(mContext.getResources().getDrawable(R.mipmap.error_pic)).into(holder.iv_class);
        holder.tv_class.setText(mData.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv_class;
        private TextView tv_class;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_class = (ImageView) itemView.findViewById(R.id.iv_class);
            tv_class = (TextView) itemView.findViewById(R.id.tv_class);
        }
    }
}
