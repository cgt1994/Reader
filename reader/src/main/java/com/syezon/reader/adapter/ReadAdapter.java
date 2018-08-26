package com.syezon.reader.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.NativeADDataRef;
import com.syezon.reader.R;
import com.syezon.reader.constant.Constant;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.utils.ADUtils;
import com.syezon.reader.utils.BookFactory;
import com.syezon.reader.utils.CacheUtils;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.view.CYTextView;
import com.syezon.reader.widget.ICacheFileListener;
import com.syezon.reader.widget.SlidingAdapter;
import com.syezon.reader.widget.SlidingLayout;

import java.util.List;

/**
 * 阅读内容的adapter
 * Created by jin on 2016/8/30.
 */
public class ReadAdapter extends SlidingAdapter<String> implements ADUtils.INativeADListener {


    private int mCurPage = 1;//当前页数
    private int mCurChapter = 1;
    private String mCurChapterName;//当前章节的名称
    private int mTotalPages = 1;//当前总页数
    private Context mContext;
    private BookFactory mFactory;
    private int mTextSize = 20;
    private String mEncoding;
    private int mBgColorId = R.color.white_bg;
    private int mTextColorId = R.color.book_name_black;
    private boolean isNetBook = true;
    private BookCaseBean bookCaseBean;
    private boolean nextHasReady = false;//下一章准备好了
    private boolean preHasReady = false;//上一章准备好了

    private boolean justNext = false;//用于记录是否刚刚换到下一章
    private boolean justPre = false;//用于记录是否刚刚换到上一章
    private boolean readyNext = true;//换章但是上一章还未显示出来的时候
    private boolean readyPre = false;//换章但是下一章还未显示出来的时候
    private int mLastChapter;
    private boolean mSpecialPre = false;
    private boolean mSpecialNext = false;
    private String mBookname;

    //广告相关

    private IFLYNativeAd mNativeAD;
    private NativeADDataRef mAdItem;

    private boolean needReportExposured = false;

    private LinearLayout nativeAD;

    //    private RelativeLayout videoContainer;


    private SlidingLayout slidingLayout;
    private List<ChapterBean> chapterList;

    public ReadAdapter(Context context, String bookName, String encoding, boolean isNetBook, ICacheFileListener listener, SlidingLayout slidingLayout, BookCaseBean bookCaseBean) {
        getAd(context);


        mContext = context;
        mFactory = new BookFactory(context, bookName, listener);
        mEncoding = encoding;
        mBookname = bookName;
        this.bookCaseBean = bookCaseBean;
        this.isNetBook = isNetBook;
        this.slidingLayout = slidingLayout;
        this.chapterList = chapterList;
        //根据广告开关来选择是否加载原生广告

    }

    public void getAd(Context context) {
        if (SPHelper.getNativeADIsOpen(context)) {
//            ADUtils.showNativeAD(context, this);
        }
    }

    private final class ViewHolder {
        TextView tvChapter;
        CYTextView tvContent;
        RelativeLayout relativeAD;
        ImageView native_ad_icon;
        ImageView native_ad_img;
        TextView native_ad_name;
        TextView native_ad_desc;
        TextView tv_time;
        LinearLayout imageViewll;
        TextView recommend_txt;
        FrameLayout frame_read;
        ImageView show_pic;
        TextView tv_chapter_next;
    }


    @Override
    public View getView(View contentView, String chapter, final String content) {
        ViewHolder viewHolder = new ViewHolder();
        if (contentView == null) {
            contentView = LayoutInflater.from(mContext).inflate(R.layout.view_read_content, null);
            viewHolder.frame_read = (FrameLayout) contentView.findViewById(R.id.frame_read);
            viewHolder.tvChapter = (TextView) contentView.findViewById(R.id.tv_chapter);
            viewHolder.tvContent = (CYTextView) contentView.findViewById(R.id.tv_content);
            viewHolder.tv_time = (TextView) contentView.findViewById(R.id.tv_time);
            viewHolder.relativeAD = (RelativeLayout) contentView.findViewById(R.id.layout_native_ad);
            viewHolder.tv_chapter_next = (TextView) contentView.findViewById(R.id.tv_chapter_next);
            //原生广告
            viewHolder.recommend_txt = (TextView) contentView.findViewById(R.id.recommend_txt);
            viewHolder.native_ad_icon = (ImageView) contentView.findViewById(R.id.native_ad_icon);
            viewHolder.native_ad_img = (ImageView) contentView.findViewById(R.id.native_ad_img);
            viewHolder.native_ad_name = (TextView) contentView.findViewById(R.id.native_ad_name);
            viewHolder.native_ad_desc = (TextView) contentView.findViewById(R.id.native_id_desc);
            //视屏广告
            viewHolder.imageViewll = (LinearLayout) contentView.findViewById(R.id.imageViewll);
            viewHolder.show_pic = (ImageView) contentView.findViewById(R.id.show_pic);
            contentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) contentView.getTag();
        }
//        广告

        nativeAD = (LinearLayout) contentView.findViewById(R.id.layout_ad);
        LinearLayout.LayoutParams adparams = new LinearLayout.LayoutParams(
                SConfig.SCREEN_WIDTH, 2 * SConfig.SCREEN_WIDTH / 3);
        Log.e("tag", SConfig.SCREEN_WIDTH + " 适配 " + 2 * SConfig.SCREEN_WIDTH / 3);

        viewHolder.imageViewll.setLayoutParams(adparams);
//        videoContainer = (RelativeLayout) contentVie.w.findViewById(R.id.layout_video_ad);
        Log.e("tag", " 适配完成 ");
        Log.e("bug chapter", "chapter" + chapter);
        if (chapter.equals("null")) {
            chapter = "引言";

        }
//        Log.e("zhangmo", "  " + content.length() + " " + getCurPage() + " " + getTotalPages(getCurChapterNO()) + getNextChapter());


//        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
//        String formattedDate = df.format(c.getTime());
//        viewHolder.tv_time.setText(formattedDate);

        if (chapter == " ") {
            chapter = chapterList.get(getCurChapterNO() + 1).getChapterName();
        }
        viewHolder.tvChapter.setText(chapter);
        viewHolder.tvChapter.setBackgroundColor(mContext.getResources().getColor(mBgColorId));
        viewHolder.tvChapter.setTextColor(mContext.getResources().getColor(mTextColorId));
        viewHolder.tvContent.setBackgroundColor(mContext.getResources().getColor(mBgColorId));
        viewHolder.tvContent.setTextColor(mContext.getResources().getColor(mTextColorId));
        viewHolder.tvContent.setTextSize(mTextSize);
        if (content.equals(Constant.NO_MOREINFOMATION) || content.equals(Constant.ERROR_INFOMATION)) {
            viewHolder.tvContent.setGravity(Gravity.CENTER);
        } else {
            viewHolder.tvContent.setGravity(Gravity.NO_GRAVITY);
        }
        viewHolder.tvContent.SetText(content);
        viewHolder.relativeAD.setBackgroundColor(mContext.getResources().getColor(mBgColorId));

        if (getTotalPages(getCurChapterNO()) == getCurPage() || getCurPage() == 0) {
            slidingLayout.setInterruput(false);
        } else {
            slidingLayout.setInterruput(true);
        }
        Log.e("chaptername", mCurChapter + "" + getNextChapterName(mCurChapter) + "  " + getCurChapter() + "  " + getNextChapter());
//        if (content.length() == 0 && !isNetBook) {
//
//            viewHolder.tv_chapter_next.setVisibility(View.VISIBLE);
////            本地的第一张封面是书名
//            if (getCurChapterNO() == 1 && getCurPage() == 1 && SPHelper.getHasIntroduction(mContext, mBookname)) {
//                viewHolder.tv_chapter_next.setText(mBookname.substring(0, mBookname.indexOf("_")));
//            } else {
//                if (getCurPage() == getTotalPages(getCurChapterNO())) {
//                    viewHolder.tv_chapter_next.setText(getNextChapter());
//
//                } else {
//                    viewHolder.tv_chapter_next.setText(getCurChapter());
//                }
//            }
//        } else {
//            viewHolder.tv_chapter_next.setVisibility(View.GONE);
//        }
        if (isNetBook) {
            if (content.equals(Constant.ERROR_INFOMATION)) {
                viewHolder.tv_chapter_next.setVisibility(View.VISIBLE);
                if (getCurPage() == getTotalPages(getCurChapterNO())) {
                    viewHolder.tv_chapter_next.setText(getNextChapter());

                } else {
                    viewHolder.tv_chapter_next.setText(getCurChapter());
                }
                viewHolder.tv_chapter_next.setTextColor(mContext.getResources().getColor(mTextColorId));
                viewHolder.tvContent.SetText("");
                viewHolder.tvChapter.setText("");
            } else {
                viewHolder.tv_chapter_next.setVisibility(View.GONE);
            }
        } else {
            if (content.length() == 0) {
                viewHolder.tv_chapter_next.setVisibility(View.VISIBLE);
                viewHolder.tvContent.SetText("");
                viewHolder.tvChapter.setText("");
//            本地的第一张封面是书名
                if (getCurChapterNO() == 1 && getCurPage() == 1 && SPHelper.getHasIntroduction(mContext, mBookname)) {
                    viewHolder.tv_chapter_next.setText(mBookname.substring(0, mBookname.indexOf("_")));
                } else {
                    if (getCurPage() == getTotalPages(getCurChapterNO())) {
                        viewHolder.tv_chapter_next.setText(getNextChapter());

                    } else {
                        viewHolder.tv_chapter_next.setText(getCurChapter());
                    }

                }
                viewHolder.tv_chapter_next.setTextColor(mContext.getResources().getColor(mTextColorId));
            } else {
                viewHolder.tv_chapter_next.setVisibility(View.GONE);
            }
        }
        Log.e("qwew", viewHolder.tvChapter.getText().toString() + "  ");
        if (isNetBook) {
            if (getCurPage() == 1) {

                CacheUtils.cacheFile(mContext, mBookname, bookCaseBean.getBook_id(), getCurChapterNO() + 1, getCurChapterNO() + 1, false);
                CacheUtils.cacheFile(mContext, mBookname, bookCaseBean.getBook_id(), getCurChapterNO() + 2, getCurChapterNO() + 2, false);
            }
            if (getCurPage() == getTotalPages(getCurChapterNO()) - 1) {
                CacheUtils.cacheFile(mContext, mBookname, bookCaseBean.getBook_id(), getCurChapterNO() + 2, getCurChapterNO() + 2, false);
                CacheUtils.cacheFile(mContext, mBookname, bookCaseBean.getBook_id(), getCurChapterNO() - 1, getCurChapterNO() - 1, false);
            }
        }
        Log.e("parent", (slidingLayout == null) + " ");
//        nativeAD.setVisibility(View.GONE);
//        videoContainer.setVisibility(View.GONE);
        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, viewHolder.tvContent.GetHeight(viewHolder.tvContent.countLine(content)));
        viewHolder.tvContent.setLayoutParams(params);


//        判断特殊位置
        if (isNetBook) {
            Log.e("get7 adapter", getCurPage() + " " + getCurChapterNO());
            if (getCurPage() == getTotalPages(getCurChapterNO()) || getCurPage() - 1 == getTotalPages(getCurChapterNO())) {
// 设置本章最后一页的特殊位置
                setSpecialPos(0);
            } else {
                setSpecialPos(-1);
            }
// 设置本章第一页的特殊位置
            if (getCurPage() == 2) {
                setSpecialPos(1);
            } else {
                setSpecialPos(-1);
            }
        }
        Log.e("get8 bugcause", "当前章节:" + getCurChapterNO() + " 当前章节的页数:" + getTotalPages(getCurChapterNO()) + " 当前页:" + getCurPage() + "   ");
        Log.e("bbb", "当前章节:" + getCurChapterNO() + " 当前章节的页数:" + getTotalPages(getCurChapterNO()) + " 当前页:" + getCurPage() + "   " + viewHolder.tvContent.countLine(content) + "   " + content);
//        if ((viewHolder.tvContent.getLineCount() < (2 * (SPHelper.getBookLines(mContext) / 3)))) {//空间足够
//        if (viewHolder.tvContent.GetTextHeight() < ((2 * SPHelper.getPhoneHeight(mContext)) / 3)) {//空间足够
//        Log.e("tag",mTotalPages+"当前页"+getCurrent());

//        if (getCurPage() + 1 == getTotalPages(getCurChapterNO()) && viewHolder.tvContent.countLine(getCurrent()) < 10) {
//                view.setVisibility(View.VISIBLE);
        Log.e("showadis", viewHolder.tvContent.countLine(content) + " " + content);
        Log.e("adver", (viewHolder.tvContent.countLine(content) < 12) + " " + (mAdItem != null) + " " + (SPHelper.getNativeADIsOpen(mContext)) + content);
        if (viewHolder.tvContent.countLine(content) < 0.7 * SConfig.SCREEN_HEIGHT && getTotalPages(getCurChapterNO()) > 3 && false) {

            Log.e("bbb", "  小于10行");

//            if (mCurChapter % 1 == 0 && !content.equals(Constant.ERROR_INFOMATION)) {//显示原生广告
//
//                if (mAdItem != null && SPHelper.getNativeADIsOpen(mContext)) {
//                    Set set = SPHelper.getAdChapter(mContext);
//                    set.add(getCurChapterNO());
//                    SPHelper.saveAdChapter(mContext, set);
//                    nativeAD.setVisibility(View.VISIBLE);
////                    videoContainer.setVisibility(View.GONE);
//                    Log.e("adshow", "展现广告" + mCurChapter + " " + chapter);
//
//                    showNativeAd(viewHolder.native_ad_icon, viewHolder.native_ad_img, viewHolder.native_ad_name, viewHolder.native_ad_desc, viewHolder.recommend_txt, chapter);
////                    videoContainer.removeAllViews();
//                    //  ADUtils.showVideoAD(mContext, videoContainer, progress, this);//加载视屏广告
//                } else if (mVideoReady && SPHelper.getVideoADIsOpen(mContext)) {//原生广告未准备好，视屏广告准备好了
//                    nativeAD.setVisibility(View.GONE);
////                    videoContainer.setVisibility(View.VISIBLE);
//                    //  showVideoAd();
//                } else {
//                    nativeAD.setVisibility(View.GONE);
////                    videoContainer.setVisibility(View.GONE);
//                }
//            }
        } else {
            nativeAD.setVisibility(View.GONE);
//            videoContainer.setVisibility(View.GONE);
        }
//        }
        return contentView;
    }


    public void setStyle(int colorId, int txtColorId, int txtSize) {
        mBgColorId = colorId;
        mTextColorId = txtColorId;
        mTextSize = txtSize;
    }

//    //显示原生广告
//    private void showNativeAd(ImageView icon, ImageView img, TextView name, TextView desc, TextView come, final String chpter) {
//        if (mAdItem.getIcon() != null) {
//            Picasso.with(mContext).load(mAdItem.getIcon()).into(icon);
//        }
//
////        Log.e("icon",mAdItem.getIcon());
//        Picasso.with(mContext).load(mAdItem.getImage()).into(img);
//        name.setText(mAdItem.getTitle());
//        desc.setText(mAdItem.getSubTitle());
//        Log.e("qweewq", mAdItem.getTitle() + "//" + mAdItem.getSubTitle());
//        come.setText(mAdItem.getAdSourceMark() + "广告");
//        //曝光原生广告展示，
//        needReportExposured = true;
////
////        Toast.makeText(mContext, mAdItem.onExposured(nativeAD)+" ", Toast.LENGTH_SHORT).show();
//        img.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MobclickAgent.onEvent(mContext, "ad_click", mAdItem.getTitle());
//                //是下载类的就给个提示
//                if (mAdItem.getAdtype() == NativeADDataRef.AD_DOWNLOAD) {
//                    Toast.makeText(mContext, "开始下载", Toast.LENGTH_SHORT).show();
//                }
//                mAdItem.onClicked(v);
//
//            }
//
//
//        });
//        //上传点击位置
//        img.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mNativeAD.setParameter(AdKeys.CLICK_POS_DX, event.getX() + "");
//                        mNativeAD.setParameter(AdKeys.CLICK_POS_DY, event.getY() + "");
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        mNativeAD.setParameter(AdKeys.CLICK_POS_UX, event.getX() + "");
//                        mNativeAD.setParameter(AdKeys.CLICK_POS_UY, event.getY() + "");
//                        break;
//                    default:
//                        break;
//                }
//                return false;
//            }
//        });
//
//    }


    public void setNextHasReady(boolean nextHasReady) {
        this.nextHasReady = nextHasReady;
    }

    public void setPreHasReady(boolean preHasReady) {
        this.preHasReady = preHasReady;
    }

    public void setCurPage(int page) {
        Log.e("get setCurPage", page + " ");
        mCurPage = page;
    }

    public int getCurPage() {
        return mCurPage;
    }

    public void setCurChapterNO(int chapter, String chapterName) {
        mCurChapter = chapter;
        mFactory.setCurChapter(chapter);
        mCurChapterName = chapterName;
        mTotalPages = mFactory.getTotalPages(isNetBook, chapter);
    }

    //设置上次退出的时候是否是特殊位置,即是否是上下章之间，当上下章之间来回切换就会造成这个问题
    public void setSpecialPos(int pos) {
        Log.e("get6 setSpecialPos", pos + " ");
        if (pos == 0) {
            mSpecialNext = true;
            mSpecialPre = false;
        } else if (pos == 1) {
            mSpecialPre = true;
            mSpecialNext = false;
        } else {
            mSpecialPre = false;
            mSpecialNext = false;
        }
    }

    public int getCurChapterNO() {
        return mCurChapter;
    }

    //返回当前页信息
    public String getCurPageInfo() {
        return mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage);
    }

    //返回当前页信息
    public String getNextChapterInfo() {
        return mFactory.getChapterInfo(isNetBook, mCurChapter + 1, 1);
    }

    public String getPreChapterInfo() {
        return mFactory.getChapterInfo(isNetBook, mCurChapter - 1, 1);
    }

    //获取当前章节名称
    public String getCurChapter() {
        return mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage).split(":")[1];
    }

    //返回当前页章标题
    public String getCurChapterName(int chapter, int page) {
        return mFactory.getChapterInfo(isNetBook, chapter, page).split(":")[1];
    }

    //获取当前页内容
    @Override
    public String getCurrent() {
        String content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage, mEncoding);

        Log.e("get Current", mCurChapter + " " + mCurPage);
        return content;
    }

    //预读后一页章节名称
    public String getNextChapter() {
        String content;
        if (isNetBook) {
            if (mCurPage == getTotalPages(mCurChapter)) {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter + 1, getTotalPages(mCurChapter)).split(":")[1];
            } else {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage + 1).split(":")[1];
            }
            Log.e("getNeChapter", mCurChapter + " " + (mCurPage + 1));
            return content;
        }
        if (mSpecialNext) {//上次退出的时候是在特殊位置
            Log.e("yudu", mCurChapter + "");

            content = mFactory.getChapterInfo(isNetBook, mCurChapter + 1, 1).split(":")[1];
            Log.e("yudu", mCurChapter + "" + content);
        } else {
            content = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage + 1).split(":")[1];
        }
        return content;
    }

    //预读下一页内容
    @Override
    public String getNext() {
        String content;
        if (isNetBook) {
            if (mCurPage >= getTotalPages(mCurChapter)) {

                content = mFactory.readBookByPage(isNetBook, mCurChapter + 1, 1, mEncoding);
                Log.e("getNext 2 q ", "本章节是" + mCurChapter + "本页是 " + mCurPage + "本章一共有" + getTotalPages(mCurChapter) + "下章一共有" + getTotalPages(mCurChapter + 1) + " " + content);
                return content;
            }
//            if (mSpecialNext) {
//                mSpecialNext = false;
//                Log.e("getNext2 e", " 这是特殊位置" + "第" + mCurChapter + "章" + mCurPage + " " + getTotalPages(mCurChapter) + " ");
//                content = mFactory.readBookByPage(isNetBook, mCurChapter, 1, mEncoding);
//
//                return content;}
            else {
                content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage + 1, mEncoding);
                Log.e("getNext 2 w ", mCurChapter + " " + (mCurPage + 1));
                return content;
            }
        }
        if (mSpecialNext) {//上次退出的时候是在特殊位置
            mSpecialNext = false;
            content = mFactory.readBookByPage(isNetBook, mCurChapter + 1, 1, mEncoding);
            Log.e("getNext 1", (mCurChapter + 1) + " " + 1);
        } else {
            Log.e("getNext 4", (mCurChapter) + " " + mCurPage);
            content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage + 1, mEncoding);

        }

        return content;
    }

    public String getNextChapterName(int chapter) {
        String name = mFactory.getNextChapterName(chapter + 1);
        return name;
    }

    //预读前一页章节名称
    public String getPreChapter() {

        String content;
        if (isNetBook) {
            if (mCurPage - 1 == 0) {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter - 1, getTotalPages(mCurChapter - 1)).split(":")[1];
            } else {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage - 1).split(":")[1];
            }
            Log.e("ppp getPreChapter2", mCurChapter + " " + (mCurPage - 1));
            return content;
        }
        if (mSpecialPre) {//上次退出的时候是在特殊位置
            int page = mFactory.getTotalPages(isNetBook, mCurChapter - 1);
            content = mFactory.getChapterInfo(isNetBook, mCurChapter - 1, page).split(":")[1];
            Log.e("getPreChapter1", mCurChapter - 1 + " " + page);

        } else {
            if ((mCurPage - 1) == 0) {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter - 1, getTotalPages(mCurChapter - 1)).split(":")[1];
            } else {
                content = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage - 1).split(":")[1];
            }
            Log.e("ppp getPreChapter3", mCurChapter + " " + (mCurPage - 1));
        }
        return content;
    }

    //预读前一页内容
    @Override
    public String getPrevious() {
        String content;
        if (isNetBook) {
            if (mCurPage - 1 == 0) {
                content = mFactory.readBookByPage(isNetBook, mCurChapter - 1, getTotalPages(mCurChapter - 1), mEncoding);
            } else {
                content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage - 1, mEncoding);
            }
            Log.e("get8  getPrevious2", mCurChapter + " " + (mCurPage - 1));
            return content;
        }
        if (mSpecialPre) {//上次退出的时候是在特殊位置
            mSpecialPre = false;
            if (mCurPage == 1) {
                int page = mFactory.getTotalPages(isNetBook, mCurChapter - 1);
                content = mFactory.readBookByPage(isNetBook, mCurChapter - 1, page, mEncoding);
            } else {
                content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage - 1, mEncoding);
            }
            Log.e(" get8 getPrevious1", mCurPage + "  " + (mCurChapter - 1) + " ");
        } else {

            if (mCurPage - 1 == 0) {
                int page = mFactory.getTotalPages(isNetBook, mCurChapter - 1);
                content = mFactory.readBookByPage(isNetBook, mCurChapter - 1, page, mEncoding);
            } else {
                content = mFactory.readBookByPage(isNetBook, mCurChapter, mCurPage - 1, mEncoding);
            }
            Log.e(" get8 getPrevious3", mCurChapter + " " + (mCurPage - 1));
        }
        return content;
    }

    @Override
    public boolean hasNext() {
//        Log.e("get hasNext ", "第" + mCurChapter + "章" + mCurPage + "页" + mFactory.hasPre(isNetBook, mCurPage, mCurChapter));
        if (mFactory.hasNext(isNetBook, mCurPage, mCurChapter, mCurChapterName) || nextHasReady) {
            nextHasReady = false;
            Log.e("get8", "有下一章");
            return true;
        } else {
            Log.e("get8", "没有下一章了");
            return false;
        }
    }

    @Override
    public boolean hasPrevious() {
        Log.e("hasPrevious", " ");
//        Log.e("get hasPrevious ", "第" + mCurChapter + "章" + mCurPage + "页" + mFactory.hasPre(isNetBook, mCurPage, mCurChapter));
        if (mFactory.hasPre(isNetBook, mCurPage, mCurChapter) || preHasReady) {
            preHasReady = false;
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected void computeNext() {
//        if (needReportExposured) {
//            //这个时候在进行曝光，当广告加载的时候其还不可见
//            needReportExposured = false;
//
//            Toast.makeText(mContext, mAdItem.onExposured(nativeAD) + " ", Toast.LENGTH_SHORT).show();
//        }
        ++mCurPage;
        Log.e("get3 ", mCurPage + " " + getCurChapterNO() + " " + getTotalPages(getCurChapterNO()));
        if (mCurPage == getTotalPages(getCurChapterNO())) {
            setSpecialPos(0);
            Log.e("get7 ", "setSpecialPos");
        }

        if (mCurPage > (mTotalPages + 1)) {//从第二页翻到第一页反复
            readyNext = true;
        } else if (mCurPage == (mTotalPages + 1)) {//从第二页翻到上一章最后一页
            justPre = true;
        }
        if (mCurPage > mTotalPages) {
            if (hasNext()) {
                mLastChapter = mCurChapter;
                ++mCurChapter;
                //上一章的最后一页还在显示，这个时候就是处于刚刚切换到下一章
                if (mCurPage > mTotalPages) {
                    justNext = true;
                }
                //不同情况下开始页不同
                if (justPre) {//翻到最后一页又反回去
                    Log.e("compute", "//翻到最后一页又反回去");
                    mCurPage = 1;
                } else if (readyNext) {//翻到第一页又翻会上一章
                    Log.e("compute", "翻到第一页又翻会上一章");
                    mCurPage = 2;
                } else {//连续翻页
                    mCurPage = 0;
                }

                mCurChapterName = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage).split(":")[1];
                mTotalPages = mFactory.getTotalPages(isNetBook, mCurChapter);
            }
        }
        if (mLastChapter != mCurChapter) {
            readyNext = false;
        }
        if (justPre || mCurPage >= 2) {
            justNext = false;
            readyPre = false;
        }
    }

    @Override
    protected void computePrevious() {
        if (needReportExposured) {
            //这个时候在进行曝光，当广告加载的时候其还不可见
            needReportExposured = false;
            mAdItem.onExposured(nativeAD);
        }

        --mCurPage;
        Log.e("get8 computePrevious", "  curpage" + mCurPage);
        if (isNetBook) {
            if (mCurPage < 0) {//从最后第二页翻到最后一页反复
                readyPre = true;
            } else if (mCurPage == 0) {//从最后第二页翻到下一章第一页在返回
                justNext = true;
            }
        } else {

            if (mCurPage < 0) {//从最后第二页翻到最后一页反复
                readyPre = true;
            } else if (mCurPage == 0) {//从最后第二页翻到下一章第一页在返回
                justNext = true;
            }
        }
//        if (mCurPage <=0) {//从最后第二页翻到最后一页反复
//            justNext = true;
//        }
//            readyPre = true;
//        } else if (mCurPage == 0) {//从最后第二页翻到下一章第一页在返回
//
//        }
        if (mCurPage < 1) {
            if (hasPrevious()) {
                Log.e("q 10", " ");
                mLastChapter = mCurChapter;
                --mCurChapter;
                if (mCurPage < 1) {
                    justPre = true;
                }
                if (justNext) {
                    Log.e("compute", "//翻到下一章第一页在翻回来");
                    mCurPage = mFactory.getTotalPages(isNetBook, mCurChapter);//翻到下一章第一页在翻回来
                } else if (readyPre) {
                    Log.e("compute", "//翻到最后一页在翻到前一页反复");
                    Log.e("compute", "章节" + mCurChapter + (mFactory.getTotalPages(isNetBook, mCurChapter)));
                    mCurPage = mFactory.getTotalPages(isNetBook, mCurChapter) - 1;//翻到最后一页在翻到前一页反复
                    Log.e("compute", "after" + mCurPage);
                } else {
                    Log.e("compute", "//连续翻页");
//                    mCurPage = mFactory.getTotalPages(isNetBook, mCurChapter);//连续翻页
//                    mCurPage = mFactory.getTotalPages(isNetBook, mCurChapter) + 1;
                }
                mCurChapterName = mFactory.getChapterInfo(isNetBook, mCurChapter, mCurPage).split(":")[1];
                mTotalPages = mFactory.getTotalPages(isNetBook, mCurChapter);
            }
        }
        if (mLastChapter != mCurChapter) {
            readyPre = false;
        }
        if (justNext || mTotalPages - mCurPage >= 1) {
            justPre = false;
            readyNext = false;
        }
    }

    //检查文件是否存在
    public boolean checkFileExist(int chapter) {
        return mFactory.checkFileExist(chapter);
    }

    //读取指定章节的信息
    public String getAssignChapter(int chapterNO) {
        int page = mFactory.readBookByChapterNo(isNetBook, chapterNO);
        return mFactory.getChapterInfo(isNetBook, mCurChapter, page);
    }

    //获取指定章节的页号信息
    public int getAssignPage(int chapterNO) {
        return mFactory.readBookByChapterNo(isNetBook, chapterNO);
    }

    //获取该章节的总页数
    public int getTotalPages(int chapter) {
        return mFactory.getTotalPages(isNetBook, chapter);
    }

    //原生广告
    @Override
    public void onLoadNativeAD(NativeADDataRef adItem) {
        Log.e("adver1", "adItem" + adItem);
        mAdItem = adItem;

    }

    @Override
    public void getNativeAdObj(IFLYNativeAd nativeAd) {
        mNativeAD = nativeAd;
    }


}
