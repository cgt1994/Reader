package com.syezon.reader.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.ToastUtil;
import com.syezon.reader.utils.Tools;

import java.text.DecimalFormat;
import java.util.List;

/**
 * 书架列表的适配器
 * Created by jin on 2016/9/13.
 */
public class BookCaseListAdapter extends RecyclerView.Adapter<BookCaseListAdapter.BookCaseViewHolder> {
    private InfoUtils infoUtils;
    private Context mContext;
    private List<BookCaseBean> mData;


    //点击事件回调接口
    public interface IBookCaseListClickListener {
        void onItemClickListener(int position);

        void onDetailClickListener(int position);

        void onCacheClickListener(int position);

        void onTopClickListener(int position);

        void onDeleteClickListener(int position);
    }

    private IBookCaseListClickListener mListener;

    public void setOnClickListener(IBookCaseListClickListener listener) {
        mListener = listener;
    }

    public BookCaseListAdapter(Context context, List<BookCaseBean> data) {
        mContext = context;
        mData = data;
        infoUtils = InfoUtils.getInstance(context);
    }


    @Override
    public BookCaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_view_bookcase, parent, false);
        return new BookCaseViewHolder(view, mContext);
    }

    public void removeHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            int savaedChapters;
            int position = msg.arg1;
            BookDetail bookDetail = (BookDetail) msg.obj;
            try {


//            判断书是否在缓存中
                if (!InfoUtils.cachingBook.contains(bookDetail.bookname) && mData.get(position).getBook_name().equals(bookDetail.bookname)) {
                    return;
                }
                Log.e("cantion", bookDetail.bookname + " " + InfoUtils.cachingBook.size() + " " + position);
                int hasCache = SPHelper.getCache(mContext, bookDetail.bookname).size();

                if (hasCache != -1) {
                    savaedChapters = hasCache;
                } else {
                    savaedChapters = 0;
                }

                int chapters = SPHelper.getBookLength(mContext, bookDetail.bookname);
                Log.e("hasCachec", bookDetail.bookname + "  " + position + " " + hasCache + " " + chapters);
//            List<String> downloadedChapters = infoUtils.indexChapter.get(book.getBook_name());

//            Log.e("download11", chaptersInfo.size() + " ");
                TextView textView = ((BookDetail) msg.obj).textview;
                if (savaedChapters <= chapters && chapters != 0) {
                    float result = (float) (savaedChapters * 10000 / chapters) / 100;

//                保留两位小数
                    DecimalFormat df = new DecimalFormat("######0.00");
                    textView.setText(df.format(result) + "%");
                } else {
                    textView.setText("100%");
                }
                if (savaedChapters != chapters || InfoUtils.cachingBook.contains(bookDetail.bookname)) {
                    Message msg1 = new Message();
                    msg1.arg1 = position;
                    msg1.obj = bookDetail;
                    handler.sendMessageDelayed(msg1, 100);
                }

            } catch (Exception e) {
            }


        }
    };

    class BookDetail {
        TextView textview;
        String bookname;

        public BookDetail(TextView textview, String bookname) {
            this.textview = textview;
            this.bookname = bookname;
        }

    }

    private PopupWindow mPopupWindow;

    @Override
    public void onBindViewHolder(final BookCaseViewHolder viewHolder, final int position) {
        final BookCaseBean book = mData.get(position);

        if (!SPHelper.getHasGuide(mContext)) {
            SPHelper.setHasGuide(mContext, true);
            View guideview = View.inflate(mContext, R.layout.guide_popuwindow, null);
            guideview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                    }
                }
            });
            mPopupWindow = new PopupWindow(guideview, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setTouchable(true);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            backgroundAlpha(0.3f);
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    backgroundAlpha(1f);
                }
            });
            int sreenHeight = SPHelper.getPhoneHeight(mContext);
            mPopupWindow.showAtLocation(guideview,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, sreenHeight * 1 / 3);
        }
        if (book.getBook_type() == 0) {
            viewHolder.iv_book.setImageResource(R.mipmap.guichuideng);
        } else {
            Picasso.with(mContext).load(book.getBook_img()).placeholder(R.mipmap.error_pic).into(viewHolder.iv_book);
        }
        Log.e("bookt", book.getBook_name() + " " + book.getBook_type());
        if (book.getBook_type() == 1) {
            viewHolder.tv_name.setText(book.getBook_name());
        } else {
            String wifiBook = book.getBook_name().split("_")[0];
            viewHolder.tv_name.setText(wifiBook);
        }

        if (book.getBook_type() != 1) {
            viewHolder.tv_author.setVisibility(View.GONE);
            viewHolder.tv_publish.setVisibility(View.GONE);
        } else {
            viewHolder.tv_author.setText(book.getBook_author());
        }
        int chapters = SPHelper.getBookLength(mContext, book.getBook_name());
        if (chapters != 0 && book.getBook_type() == 1) {
//            Log.e("onBindViewHolder", infoUtils.indexChapter.get(book.getBook_name()).size() + "//" + chapters + "//" + book.getBook_name() + "//");
//            List<String> savedChapters = infoUtils.indexChapter.get(book.getBook_name());
//            if (savedChapters != null) {
            viewHolder.tv_chaptersava.setVisibility(View.VISIBLE);
            int hasCache = SPHelper.getCache(mContext, book.getBook_name()).size();
//            Integer savaedChapters = InfoUtils.savaedChapters.get(book.getBook_name());
            if (hasCache != -1) {
                Log.e("jindu", hasCache + "  " + chapters);
                double result = ((int) ((hasCache * 10000 / chapters))) / 100.0;

                DecimalFormat df = new DecimalFormat("######0.00");

                Log.e("percent", result + " ");
                viewHolder.tv_chaptersava.setText(df.format(result) + "%");

//                if (savaedChapters.intValue() == 0) {
//                    viewHolder.tv_chaptersava.setVisibility(View.GONE);
//                }
            } else {
                viewHolder.tv_chaptersava.setVisibility(View.GONE);
            }

            Message message = new Message();
            message.what = 1;
            message.arg1 = position;
            message.obj = new BookDetail(viewHolder.tv_chaptersava, book.getBook_name());

            handler.sendMessage(message);


        } else {
            viewHolder.tv_chaptersava.setVisibility(View.GONE);
        }

//        viewHolder.tv_chaptersava.setText(book.getCache());
        String lastChapter = mData.get(position).getLast_chapter();
        String updateTime = mData.get(position).getBook_update_time();
        if (lastChapter != null && !"".equals(lastChapter)) {
            viewHolder.tv_publish.setText("最新章节:" + lastChapter);
        } else if (updateTime != null && !"".equals(updateTime)) {
            viewHolder.tv_publish.setText("更新时间:" + updateTime);
        } else {
            viewHolder.tv_publish.setText("");
        }

        //是否已经缓存
        if (book.getCache() == -1) {
            viewHolder.linear_cache.setEnabled(false);
            viewHolder.linear_cache.setAlpha(0.5f);
        } else {
            viewHolder.linear_cache.setEnabled(true);
            viewHolder.linear_cache.setAlpha(1.0f);
        }
        //判读是否是通过wifi传书接收的
        if (book.getBook_type() == 2) {
            viewHolder.linear_detail.setVisibility(View.GONE);
            viewHolder.linear_cache.setVisibility(View.GONE);
        } else {
            viewHolder.linear_detail.setVisibility(View.VISIBLE);
            viewHolder.linear_cache.setVisibility(View.VISIBLE);
        }
        //点击事件
        viewHolder.linear_show.setOnClickListener(new OnItemClickListener(viewHolder, position, 1));
        viewHolder.linear_show.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View popupView = View.inflate(mContext, R.layout.layout_popupwindow, null);
                ImageView iv_popu_img = (ImageView) popupView.findViewById(R.id.iv_popu_img);

                TextView tv_popu_name = (TextView) popupView.findViewById(R.id.tv_popu_name);

                TextView tv_popu_author = (TextView) popupView.findViewById(R.id.tv_popu_author);
                tv_popu_author.setText(book.getBook_author());

                TextView btn_detail = (TextView) popupView.findViewById(R.id.btn_detail);
                TextView btn_cache = (TextView) popupView.findViewById(R.id.btn_cache);
                TextView btn_top = (TextView) popupView.findViewById(R.id.btn_top);
                TextView btn_delete = (TextView) popupView.findViewById(R.id.btn_delete);

                if (book.getBook_type() != 1) {
                    tv_popu_author.setVisibility(View.GONE);
                }
                if (book.getBook_type() == 0) {

                    iv_popu_img.setImageResource(R.mipmap.guichuideng);
                } else {
                    Picasso.with(mContext).load(book.getBook_img()).placeholder(R.mipmap.error_pic).into(iv_popu_img);
                }
                if (book.getBook_type() == 1) {
                    tv_popu_name.setText(book.getBook_name());
                } else {
                    String wifiBook = book.getBook_name().split("_")[0];
                    tv_popu_name.setText(wifiBook);
                }

                if (book.getBook_type() != 1) {
                    btn_cache.setVisibility(View.GONE);
                    btn_detail.setVisibility(View.GONE);
                }

                btn_detail.setOnClickListener(new OnItemClickListener(viewHolder, position, 2));
                btn_cache.setOnClickListener(new OnItemClickListener(viewHolder, position, 3));
                btn_top.setOnClickListener(new OnItemClickListener(viewHolder, position, 4));
                btn_delete.setOnClickListener(new OnItemClickListener(viewHolder, position, 5));


                mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                mPopupWindow.setTouchable(true);
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mPopupWindow.setAnimationStyle(R.style.popo_animation);
                backgroundAlpha(0.5f);
                mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        backgroundAlpha(1f);
                    }
                });

                if (Tools.checkDeviceHasNavigationBar(mContext)) {
//                   int height=SPHelper.get
                    mPopupWindow.showAtLocation(popupView,
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, getNavigationBarHeigh());
                } else {
                    mPopupWindow.showAtLocation(popupView,
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
//                mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));


                return false;
            }
        });
        viewHolder.linear_detail.setOnClickListener(new OnItemClickListener(viewHolder, position, 2));
        viewHolder.linear_cache.setOnClickListener(new OnItemClickListener(viewHolder, position, 3));
        viewHolder.linear_top.setOnClickListener(new OnItemClickListener(viewHolder, position, 4));
        viewHolder.linear_delete.setOnClickListener(new OnItemClickListener(viewHolder, position, 5));
    }

    private int getNavigationBarHeigh() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        ((Activity) mContext).getWindow().setAttributes(lp);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class OnItemClickListener implements View.OnClickListener {
        private int position;
        private BookCaseViewHolder holder;
        private int type;

        public OnItemClickListener(BookCaseViewHolder holder, int position, int type) {

            this.position = position;
            this.holder = holder;
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
//            holder.swipeSwitch.smoothCloseMenu();
            switch (type) {
                case 1:
                    mListener.onItemClickListener(position);
                    break;
                case 2:
                    mListener.onDetailClickListener(position);
                    break;
                case 3:
                    mListener.onCacheClickListener(position);
                    if (!isNetworkAvailable(mContext)) {
                        ToastUtil.showToast(mContext, "抱歉，你的网络出现问题，请检查网络", Toast.LENGTH_SHORT);
                        handler.removeCallbacksAndMessages(null);
                        return;
                    }

                    BookCaseBean book = mData.get(position);
                    if (InfoUtils.cachingBook.contains(book.getBook_name())) {
                        return;
                    } else {
                        InfoUtils.cachingBook.add(book.getBook_name());
                    }

                    Message message = new Message();
                    message.what = 1;
                    message.arg1 = position;
                    holder.tv_chaptersava.setVisibility(View.VISIBLE);
                    BookDetail bookDetail = new BookDetail(holder.tv_chaptersava, book.getBook_name());
                    message.obj = bookDetail;
                    Log.e("tv", (holder == null) + " " + (holder.tv_chaptersava == null) + " ");
                    handler.sendMessage(message);
                    break;
                case 4:
                    mListener.onTopClickListener(position);
                    break;
                case 5:
                    handler.removeCallbacksAndMessages(null);
                    Log.e("delete", mData.get(position).getBook_name());
                    InfoUtils.cachingBook.remove(mData.get(position).getBook_name());
                    mListener.onDeleteClickListener(position);


                    break;
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static class BookCaseViewHolder extends RecyclerView.ViewHolder {
        TextView tv_chaptersava;

        ImageView iv_book;
        TextView tv_name;
        TextView tv_author;
        TextView tv_publish;
        LinearLayout linear_show;
        LinearLayout linear_detail;
        LinearLayout linear_cache;
        LinearLayout linear_top;
        LinearLayout linear_delete;
        ImageView iv_guide;

        TextView tv_detail, tv_cache, tv_top, tv_delete;

        public BookCaseViewHolder(View itemView, Context context) {
            super(itemView);

            iv_book = (ImageView) itemView.findViewById(R.id.book_img);
            tv_name = (TextView) itemView.findViewById(R.id.book_name);
            tv_author = (TextView) itemView.findViewById(R.id.book_author);
            tv_publish = (TextView) itemView.findViewById(R.id.book_publish);
            linear_show = (LinearLayout) itemView.findViewById(R.id.linear_show);
            linear_detail = (LinearLayout) itemView.findViewById(R.id.linear_detail);
            linear_cache = (LinearLayout) itemView.findViewById(R.id.linear_cache);
            linear_top = (LinearLayout) itemView.findViewById(R.id.linear_top);
            linear_delete = (LinearLayout) itemView.findViewById(R.id.linear_delete);
            tv_chaptersava = (TextView) itemView.findViewById(R.id.tv_chaptersava);

            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "iconfont.ttf");
            tv_detail = (TextView) itemView.findViewById(R.id.book_detail);
            tv_detail.setTypeface(typeface);
            tv_cache = (TextView) itemView.findViewById(R.id.book_cache);
            tv_cache.setTypeface(typeface);
            tv_top = (TextView) itemView.findViewById(R.id.book_top);
            tv_top.setTypeface(typeface);
            tv_delete = (TextView) itemView.findViewById(R.id.book_delete);
            tv_delete.setTypeface(typeface);
        }
    }
}
