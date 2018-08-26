package com.syezon.reader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.utils.SPHelper;

import java.util.List;

/**
 * 章节列表的适配器
 * Created by jin on 2016/9/20.
 */
public class BookChapterListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEAD_VIEW = 0;

    private List<ChapterBean> mData;
    private Context mContext;
    private BookCaseBean mBookInfo;
    private int mTxtColorId = R.color.book_name_black;//字体颜色

    public interface IItemClickListener {
        void onChapterItemClick(int position);
    }

    private IItemClickListener mListener;

    public void setOnItemClickListener(IItemClickListener listener) {
        mListener = listener;
    }

    public BookChapterListAdapter(Context context, List<ChapterBean> data, BookCaseBean bean) {
        mContext = context;
        mData = data;
        Log.e("mmm", "gouzaoqi" + mData.size());
        mBookInfo = bean;
    }

    public void setTxtColor(int colorId) {
        mTxtColorId = colorId;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEAD_VIEW) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_chapterlist_head, parent, false);
            return new HeadViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_chapterlist, parent, false);
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeadViewHolder) {
            HeadViewHolder headViewHolder = (HeadViewHolder) holder;
            if (mBookInfo.getBook_type() == 0) {
                headViewHolder.img_book.setImageResource(R.mipmap.guichuideng);
            } else {
                Picasso.with(mContext).load(mBookInfo.getBook_img()).placeholder(R.mipmap.default_pic).into(headViewHolder.img_book);
            }
            Log.e("name", mBookInfo.getBook_type() + " " + mBookInfo.getBook_name());
            if (mBookInfo.getBook_type() != 1) {
                headViewHolder.tv_author.setVisibility(View.GONE);
                headViewHolder.tv_name.setText(mBookInfo.getBook_name().split("_")[0]);
            } else {
                headViewHolder.tv_author.setText(mBookInfo.getBook_author());
                headViewHolder.tv_name.setText(mBookInfo.getBook_name());
            }
            headViewHolder.tv_name.setTextColor(mContext.getResources().getColor(mTxtColorId));

            headViewHolder.tv_author.setTextColor(mContext.getResources().getColor(mTxtColorId));

        } else {

            NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
//            if(position==SPHelper.getCurChapterNO(mContext, mBookInfo.getBook_name()) + "") {
//
//            }
            String chapter = mData.get(position - 1).getChapterName();
            if (chapter == null) {
                chapter = "引言";
            }
            normalViewHolder.tv_chapter.setText(chapter);
            Log.e("curNo", SPHelper.getCurChapterNO(mContext, mBookInfo.getBook_name()) + "   " + position);
            if (SPHelper.getCurChapterNO(mContext, mBookInfo.getBook_name()) == position) {
                normalViewHolder.tv_chapter.setTextColor(mContext.getResources().getColor(R.color.chapter_select_509FDE));
            } else {
                normalViewHolder.tv_chapter.setTextColor(mContext.getResources().getColor(mTxtColorId));
            }
            normalViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(mContext,"点击了"+position+"",Toast.LENGTH_SHORT) .show();
                    mListener.onChapterItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEAD_VIEW;
        } else {
            return position;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
//        return mData.size();
    }

    //头部布局
    public static class HeadViewHolder extends RecyclerView.ViewHolder {

        ImageView img_book;
        TextView tv_name;
        TextView tv_author;

        public HeadViewHolder(View itemView) {
            super(itemView);
            img_book = (ImageView) itemView.findViewById(R.id.book_img);
            tv_name = (TextView) itemView.findViewById(R.id.book_name);
            tv_author = (TextView) itemView.findViewById(R.id.book_author);
        }
    }

    //普通的布局
    public static class NormalViewHolder extends RecyclerView.ViewHolder {

        TextView tv_chapter;

        public NormalViewHolder(View itemView) {
            super(itemView);
            tv_chapter = (TextView) itemView.findViewById(R.id.tv_chapter_name);
        }
    }
}
