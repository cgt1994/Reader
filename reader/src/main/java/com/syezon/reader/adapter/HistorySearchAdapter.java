package com.syezon.reader.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.model.HistorySearchBean;

import java.util.List;

/**
 * Created by jin on 2016/9/26.
 */
public class HistorySearchAdapter extends RecyclerView.Adapter<HistorySearchAdapter.ViewHolder> {

    private Context mContext;
    private List<HistorySearchBean> mData;

    public interface IClickListener {
        void onItemClick(int position);

        void onItemDeleteClick(int position);
    }

    private IClickListener mListener;

    public void setOnClickListenr(IClickListener listenr) {
        mListener = listenr;
    }

    public HistorySearchAdapter(Context context, List<HistorySearchBean> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_history, parent, false);
        return new ViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.tv_history.setText(mData.get(position).getSearchContent());
        holder.tv_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position);
            }
        });
        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_history;
        private TextView tv_delete;
        private TextView tv_icon_history;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "iconfont.ttf");
            tv_history = (TextView) itemView.findViewById(R.id.tv_history);
            tv_delete = (TextView) itemView.findViewById(R.id.tv_delete);
            tv_icon_history = (TextView) itemView.findViewById(R.id.tv_icon_history);
            tv_delete.setTypeface(typeface);
            tv_icon_history.setTypeface(typeface);
        }
    }
}
