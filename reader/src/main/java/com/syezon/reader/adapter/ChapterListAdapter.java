package com.syezon.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.model.ChapterBean;

import java.util.List;

/**
 * Created by jin on 2016/9/27.
 */
public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ViewHolder> {
    private Context mContext;
    private List<ChapterBean> mData;

    public interface IItemClickListener {
        void onItemClick(int position);
    }

    private IItemClickListener mListener;

    public void setOnItemClickListener(IItemClickListener listener) {
        mListener = listener;
    }

    public ChapterListAdapter(Context context, List<ChapterBean> data) {

        mContext = context;
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_chapterlist, parent, false);
        return new ViewHolder(view);
    }

//    private ProgressDialog mpd;

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv_chapter.setText(mData.get(position).getChapterName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(position);
//                mpd = Tools.showProgressDialog(mContext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_chapter;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_chapter = (TextView) itemView.findViewById(R.id.tv_chapter_name);
        }
    }
}
