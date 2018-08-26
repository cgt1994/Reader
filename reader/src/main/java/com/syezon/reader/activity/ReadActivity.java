package com.syezon.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.NativeADDataRef;
import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.adapter.BookChapterListAdapter;
import com.syezon.reader.adapter.ReadAdapter;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.db.PageDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.service.BookIndexService;
import com.syezon.reader.utils.ADUtils;
import com.syezon.reader.utils.BrightnessUtils;
import com.syezon.reader.utils.CacheUtils;
import com.syezon.reader.utils.DividePagesUtil;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.ReadMenuUtils;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.ToastUtil;
import com.syezon.reader.utils.Tools;
import com.syezon.reader.view.BatteryView;
import com.syezon.reader.widget.ICacheFileListener;
import com.syezon.reader.widget.SlidingLayout;
import com.syezon.reader.widget.slider.OverlappedSlider;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;

/**
 * 阅读数据界面
 * Created by jin on 2016/8/30.
 */
public class ReadActivity extends Activity implements SlidingLayout.OnTapListener, ReadMenuUtils.IUserChangedListener, BookChapterListAdapter.IItemClickListener, ICacheFileListener, ADUtils.INativeADListener {

    private static final String TAG = ReadActivity.class.getSimpleName();

    private DrawerLayout mDrawLayout;
    private SlidingLayout mSlidingLayout;
    private RecyclerView mChapter_list;
    private LinearLayout mLinear_no_chapter_list;
    private List<ChapterBean> mChapterData;
    private ReadAdapter mReadAdapter;
    private OverlappedSlider mSlider;
    private String mBookName;
    private TextView tv_battery;
    private BatteryView view_bv;
    private BookCaseBean mBookInfo = new BookCaseBean();
    private BookChapterListAdapter mChapterAdapter;
    private ChapterDBHelper mChapterDBHelper;
    private BookCaseDBHelper mBookCaseDBHelper;
    private boolean isGetFromMem = false;//是否从内存中读取章节信息

    private ReadMenuUtils mMenu;
    private int mBgColor = R.color.white_bg;
    private int mTxtColor = R.color.book_name_black;
    private int mReadTxtColor = R.color.book_name_black;
    private boolean isNetBook = false;
    private int mOpt = 1;
    private TextView tv_time;
    private ProgressDialog mDialog;
    private boolean isJumpChapter = false;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Intent intent = new Intent();
            intent.setAction("cacheComplete");
            ReadActivity.this.sendBroadcast(intent);
            Log.e("fenye222", "发送成功");
        }
    };

    @Override
    public void onLoadNativeAD(NativeADDataRef Item) {
        adItem = Item;
        Log.e("adver2", adItem + " ");
        final View adView = View.inflate(ReadActivity.this, R.layout.popu_ad, null);
        try {
            if (!isFinishing()) {

                showNativeAd(adView);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void getNativeAdObj(IFLYNativeAd nativeAd) {
        mNativeAD = nativeAd;
    }


    //缓存一章完成
    public class RefreshDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String str = sdf.format(new Date());
                tv_time.setText(str);

                return;

            }
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int current = intent.getExtras().getInt("level");// 获得当前电量
                int total = intent.getExtras().getInt("scale");// 获得总电量
                float percent = (float) current / (float) total;
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
//                Toast.makeText(ReadActivity.this, isCharging + " ", Toast.LENGTH_SHORT).show();
                view_bv.setBatterStatus(isCharging);
                view_bv.setWideAndHeight(percent);
                tv_battery.setText( (current * 100 / total) + "%");
                Log.e("percent", current + " " + total + " " + percent);
                return;
            }
            mSlidingLayout.setCanMove(true);
//            mSlidingLayout.resetFromAdapter();


            if (mDialog != null && mDialog.isShowing()) {
                Tools.closeProgressDialog(mDialog);
            }
//            if (intent.getBooleanExtra("havaCache",false)){
//                return;
//            }
            //缓存下一章或者上一章完成
            if (intent.getBooleanExtra("cacheOne", false)) {
                int chapter = intent.getIntExtra("chapter", 1);
//                if (mOpt == 0) {//当往前翻的时候，页数这个时候才统计出来
//                    Log.e("ggg", chapter + " " + mReadAdapter.getTotalPages(chapter));
//                    //为了停留在当前页
////                    SPHelper.setCurrentPage(ReadActivity.this, mBookName, mReadAdapter.getTotalPages(chapter));
//                } else {
//                    //为了停留在当前页
////                    SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
//                }
//                SPHelper.setCurChapterNO(ReadActivity.this, mBookName, chapter-1);
//                SPHelper.setCurChapterNO(ReadActivity.this,mBookName,);
                SPHelper.setCurChapterName(ReadActivity.this, mBookName, mReadAdapter.getCurChapter());
                if (isJumpChapter) {//跳章
                    isJumpChapter = false;
                    Log.e("get6", "跳章");
//                    SPHelper.setSpecialPos(ReadActivity.this, mBookName, 0);
                    setUserAttr();
                } else {
                    //正常上一页和下一页的翻动
                    if (mOpt == 0) {
                        Log.e("get huadong", "chapter==" + chapter + "  " + (mReadAdapter.getTotalPages(chapter) + 2) + "  ");
//                        mReadAdapter.setCurPage(mReadAdapter.getTotalPages(chapter) + 2);

//                        mReadAdapter.setCurPage(mReadAdapter.getTotalPages(chapter) + 2);

                        mReadAdapter.setCurChapterNO(chapter, SPHelper.getCurChapterName(ReadActivity.this, mBookName));
                        mReadAdapter.setPreHasReady(true);
//                        if ()
//                        mSlider.moveToPrevious();

                    } else {
                        Log.e("get huadong", chapter + "  " + (-1) + "");
//                        mReadAdapter.setCurPage(-1);
//                        mReadAdapter.setCurChapterNO(chapter, SPHelper.getCurChapterName(ReadActivity.this, mBookName));
                        mReadAdapter.setNextHasReady(true);
//                        mSlider.moveToNext();
                    }
                }
            }
        }
    }

    private RefreshDataReceiver mReceiver;
    private InfoUtils infoUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        handler.sendEmptyMessage(1);
        if (getIntent().hasExtra("bookInfo")) {
            mBookInfo = (BookCaseBean) getIntent().getSerializableExtra("bookInfo");
            mBookName = mBookInfo.getBook_name();
        } else {
            mBookName = getIntent().getStringExtra("bookName");
        }
        infoUtils = InfoUtils.getInstance(this);
        isNetBook = getIntent().getBooleanExtra("isNetBook", false);
        if (infoUtils.pageDetail.containsKey(mBookName)) {
            isGetFromMem = true;
        } else {
            isGetFromMem = false;
            mChapterDBHelper = new ChapterDBHelper(this, mBookName);
        }
        mBookCaseDBHelper = new BookCaseDBHelper(this);
        initView();
        initChapterList();
        //查询当前的总目录情况
        getChapterList();
        setUserAttr();
        initReceiver();

    }

    private void initReceiver() {
        mReceiver = new RefreshDataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("cacheComplete");
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);

    }

    private int tsize = 20;

    //设置用户属性
    private void setUserAttr() {
        if (mDialog != null && mDialog.isShowing()) {
            Tools.closeProgressDialog(mDialog);
        }
        int curChapter = SPHelper.getCurChapterNO(this, mBookName);
        int curPage = SPHelper.getCurrentPage(this, mBookName);

        String curChapterName = SPHelper.getCurChapterName(this, mBookName);
        Log.e("name", curChapterName);
        mBgColor = R.color.white_bg;
        mTxtColor = R.color.book_name_black;
        mReadTxtColor = R.color.book_name_black;
        switch (SPHelper.getReaderStyle(this)) {
            case 0:
                mBgColor = R.color.color1;
                mTxtColor = R.color.white_yellow;
                mReadTxtColor = R.color.book_name_black;
                break;
            case 1:
                mBgColor = R.color.color2;
                mTxtColor = R.color.pink_blue;
                mReadTxtColor = R.color.book_name_black;
                break;
            case 2:
                mBgColor = R.color.color3;
                mTxtColor = R.color.yellow_brown;
                mReadTxtColor = R.color.book_name_black;
                break;
            case 3:
                mBgColor = R.color.night_bg;
                mTxtColor = R.color.night_blue;
                mReadTxtColor = R.color.gray;
                break;
        }
        resetReadView(curChapter, curChapterName, curPage, mBgColor, mReadTxtColor, SPHelper.getBookTextSize(this));
    }

    private void initView() {
        tv_time = (TextView) findViewById(R.id.tv_time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String str = sdf.format(new Date());
        tv_time.setText(str);
        tv_battery = (TextView) findViewById(R.id.tv_battery);
        view_bv = (BatteryView) findViewById(R.id.view_bv);
        mDrawLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mSlidingLayout = (SlidingLayout) findViewById(R.id.slidingLayout);
        mChapter_list = (RecyclerView) findViewById(R.id.chapter_list);
        mLinear_no_chapter_list = (LinearLayout) findViewById(R.id.no_chapter_list);
        mDrawLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭滑动显示菜单
        mSlidingLayout.setOnTapListener(this);//触摸事件

    }

    LinearLayoutManager mLL = null;

    private void initChapterList() {
        mChapterData = new ArrayList<>();

//        final LinearLayoutManager finalMLL = mLL;
        mLL = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void scrollToPositionWithOffset(int position, int offset) {
                super.scrollToPositionWithOffset(position, offset);

            }
        };
        mChapter_list.setLayoutManager(mLL);
        mChapterAdapter = new BookChapterListAdapter(this, mChapterData, mBookInfo);
        Log.e("mmm", mChapterData.size() + " ");
        mChapter_list.setAdapter(mChapterAdapter);
        mChapterAdapter.setOnItemClickListener(this);
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

    //加载章节目录
    private void getChapterList() {
        if (isNetBook) {//从网络加载
            if (isNetworkAvailable(this)) {
                getChapterListOnline();
            } else {
                List<ChapterBean> chapterList = SPHelper.getObject(this, mBookName + "chapterlist");
                if (chapterList != null) {
                    Log.e("chapterList", chapterList.size() + " " + mChapterData.size());
                    for (int i = 0; i < chapterList.size(); i++) {
                        Log.e("chapterList", chapterList.get(i).getChapterName() + " ");

                    }
                    mChapterData.clear();
                    mChapterData.addAll(chapterList);
                    mBookInfo.setLast_chapter_no(mChapterData.size());
//                SPHelper.setBookLength(ReadActivity.this, mBookName, mChapterData.size());
                    mBookInfo.setLast_chapter(mChapterData.get(mChapterData.size() - 1).getChapterName());
                    mBookCaseDBHelper.updateBookCase(mBookInfo, BookCaseDBHelper.BOOK_CHECK_UPDATE);
                    mChapterAdapter.notifyDataSetChanged();
                } else {
                    if (isNetworkAvailable(this)) {
                        getChapterListOnline();
                    }

                }
            }
        } else {
            //从内存中读取
            if (isGetFromMem) {
                List<String> list = infoUtils.indexChapter.get(mBookName);
                Log.e("add1", "add list " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    ChapterBean bean = new ChapterBean();
                    String info = list.get(i);
                    String[] infos = info.split("\\|");
                    bean.setPage(Integer.valueOf(infos[0]));
                    bean.setChapterId(Integer.valueOf(infos[1]));
                    Log.e("add", Integer.valueOf(infos[1]) + "");
                    bean.setChapterName(infos[2]);
                    Log.e("add", infos[2] + "");
                    bean.setChapterStart(Long.parseLong(infos[3]));
                    bean.setChapterEnd(Long.parseLong(infos[4]));
                    bean.setIsLastChapter(Integer.valueOf(infos[5]));
                    mChapterData.add(bean);
                }
                mChapterAdapter.notifyDataSetChanged();
            } else {
                List<ChapterBean> list;
                if (isNetBook) {

                    list = mChapterDBHelper.readBookChapterList();
                } else {
                    list = infoUtils.readChapterList(mBookName);
                }

                if (list != null) {
                    mChapterData.clear();
                    mChapterData.addAll(list);
                    mChapterAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        Log.e("paint", (int) (spValue * fontScale + 0.5f) + "   size ");
        return (int) (spValue * fontScale + 0.5f);
    }

    public int countLine(String s) {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);

        int width = this.getResources().getDisplayMetrics().widthPixels - (int) (SConfig.SCREEN_SCALE * 20);
        mPaint.setTextSize(sp2px(this, SPHelper.getBookTextSize(this)));
        int line = 0;
        int istart = 0;
        int viewnum = SPHelper.getCurTxtNums(this) + 1;
        int w = 0;
        int nowwords = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            float[] widths = new float[1];
            String srt = String.valueOf(ch);
            mPaint.getTextWidths(srt, widths);

            if (ch == '\n') {
                nowwords = 0;
                line++;
                istart = i + 1;
                w = 0;
            } else if (nowwords == viewnum) {
                line++;
                istart = i;
                i--;
                w = 0;
                nowwords = 0;
            } else {
                w += (int) (Math.ceil(widths[0]));
                if (w > width) {
                    nowwords = 0;
                    line++;
                    istart = i;
                    i--;
                    w = 0;
                } else {
                    if (i == (s.length() - 1)) {
                        nowwords = 0;
                        line++;
                    }
                }
            }
        }
        Log.e("lines", line + " ");

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + 15;
        int a = (line) * m_iFontHeight + 2;
        return a;


    }

    private IFLYNativeAd mNativeAD;
    private PopupWindow mPopupWindow;
    private NativeADDataRef adItem;

    //重置阅读界面
    private void resetReadView(final int curChapter, String curChapterName, int curPage, int bgColor, int txtColor, int txtSize) {
        mReadAdapter = new ReadAdapter(this, mBookName, SPHelper.getBookEnCoding(this, mBookName), isNetBook, this, mSlidingLayout, mBookInfo);

        if (curPage > mReadAdapter.getTotalPages(curChapter)) {
            curPage = mReadAdapter.getTotalPages(curChapter);
        }
        mReadAdapter.setCurPage(curPage);
        mReadAdapter.setCurChapterNO(curChapter, curChapterName);
        Log.e("get6", SPHelper.getSpecialPos(this, mBookName) + " " + "  123");
        mReadAdapter.setSpecialPos(SPHelper.getSpecialPos(this, mBookName));
        mReadAdapter.setStyle(bgColor, txtColor, txtSize);
        mSlidingLayout.setAdapter(mReadAdapter);
        mSlider = new OverlappedSlider();
        mSlidingLayout.setSlider(mSlider);
        mSlider.setmScroller(new OverlappedSlider.SrcollListener() {
            @Override
            public void isMoving() {
                cancelPopuWindow();
            }
        });
        mSlidingLayout.setOnSlideChangeListener(new SlidingLayout.OnSlideChangeListener() {
            @Override
            public void onSlideScrollStateChanged(int touchResult) {

// if (isNetworkAvailable(this)) {
                if (!isNetworkAvailable(ReadActivity.this)) {
                    return;
                }
                if (mPopupWindow != null && mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
//                当前章节 用来记录广告
                int curNo;
                Log.e("hang", touchResult + "  " + countLine(mReadAdapter.getNext()) + "  " + SConfig.SCREEN_HEIGHT);
                Log.e("pqw", touchResult + " " + mReadAdapter.getCurChapterNO() + " " + mReadAdapter.getCurPage() + " " + mReadAdapter.getTotalPages(mReadAdapter.getCurChapterNO()));
//              0是右滑 1是左滑
                if (touchResult == 0) {
                    if (mReadAdapter.getCurPage() == mReadAdapter.getTotalPages(mReadAdapter.getCurChapterNO())) {

                        curNo = mReadAdapter.getCurChapterNO() + 1;
                        if (SPHelper.getNativeADIsOpen(ReadActivity.this)) {
                            showPicOrAd(curNo);
                        }

//                        tv_chapter_next.setVisibility(View.VISIBLE);
//                        tv_chapter_next.setText("新的篇章");

                    } else {
//                        tv_chapter_next.setVisibility(View.GONE);
                        curNo = mReadAdapter.getCurChapterNO();
                    }

                    Log.e("curNo1", curNo + " ");
                } else {
                    if (mReadAdapter.getCurPage() == 2) {
//                        tv_chapter_next.setVisibility(View.VISIBLE);
//                        tv_chapter_next.setText("新的篇章");
                        curNo = mReadAdapter.getCurChapterNO();
                        if (SPHelper.getNativeADIsOpen(ReadActivity.this)) {
                            showPicOrAd(curNo);
                        }
                    } else {
//                        tv_chapter_next.setVisibility(View.GONE);
                        curNo = mReadAdapter.getCurChapterNO();
                    }
                    Log.e("curNo1", curNo + " ");
                }
                Log.e("qqa", curNo + " ");
                switch (touchResult) {
                    case 0:

//                        Log.e("zhangjie",mReadAdapter.get       ()+1);

                        if (countLine(mReadAdapter.getNext()) < 0.5 * SConfig.SCREEN_HEIGHT) {


                        } else {
                            cancelPopuWindow();
                        }
                        break;
                    case 1:
//
                        if (countLine(mReadAdapter.getPrevious()) < 0.5 * SConfig.SCREEN_HEIGHT) {
//                            showPicOrAd(curNo);

                        } else {
                            cancelPopuWindow();
                        }
                        break;
                }

            }

            @Override
            public void onSlideSelected(Object obj) {

            }
        });
        mChapterAdapter.setTxtColor(txtColor);
        mChapter_list.setBackgroundColor(getResources().getColor(bgColor));
    }

    private void showPicOrAd(int curNo) {
        float rate = SPHelper.getRate(ReadActivity.this);
        float ran = (float) Math.random();
        if (ran < rate || curNo < 4) {
            showPic(curNo);
        } else {
            ADUtils.showNativeAD(ReadActivity.this, ReadActivity.this);
        }
    }

    private void showPic(int curNo) {
        View view = View.inflate(this, R.layout.popu_show, null);
        final ImageView show_pic = (ImageView) view.findViewById(R.id.show_pic);
        TextView tv_show = (TextView) view.findViewById(R.id.tv_show);
        final TextView tv_loading = (TextView) view.findViewById(R.id.tv_loading);
        final RelativeLayout show_background = (RelativeLayout) view.findViewById(R.id.show_background);
//        LinearLayout rl_show = (LinearLayout) view.findViewById(R.id.rl_show);


        if (InfoUtils.showPic.length() != 0) {
//                int ran = (int) (InfoUtils.showPic.length() * Math.random());
            int ran = curNo * InfoUtils.RANDOM % InfoUtils.showPic.length();
            Log.e("ran", ran + " ");
            JSONObject jsonObject = InfoUtils.showPic.optJSONObject(ran);
            String mess = jsonObject.optString("message");
            String pic = jsonObject.optString("pic");
            String type = jsonObject.optString("type");
            Log.e("gif", pic + " " + type + " " + InfoUtils.RANDOM + " " + mess);
            int height = 0;
            tv_show.setText(mess);
            MobclickAgent.onEvent(ReadActivity.this, "show_pic", pic);
            if (type.equals("gif")) {
                Glide.with(this).load(pic).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        tv_loading.setVisibility(View.GONE);
                        return false;
                    }
                }).into(show_pic);

                height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            } else {
//                show_pic.setVisibility(View.GONE);
                height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                Glide.with(this).load(pic).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        tv_loading.setVisibility(View.GONE);
                        show_pic.setImageDrawable(new BitmapDrawable(resource));
//                        Bitmap blurbitmap = BlurBitmapUtil.blurBitmap(ReadActivity.this, resource, 20f);
//                        show_background.setBackground(new BitmapDrawable(blurbitmap));
                    }
                });

                Glide.with(this).load(pic).bitmapTransform(new BlurTransformation(this)).into(new SimpleTarget<GlideDrawable>() {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        Log.e("glide", "onLoadStarted");
                        super.onLoadStarted(placeholder);
                        tv_loading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        Log.e("glide", "onResourceReady");
                        tv_loading.setVisibility(View.GONE);
                        show_background.setBackground(resource);
                        show_pic.setVisibility(View.VISIBLE);
                    }
                });

            }
            mPopupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT, height, true);
            mPopupWindow.setTouchable(false);
            mPopupWindow.setFocusable(false);

            mPopupWindow.showAtLocation(view,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30);
        }

    }

    private void cancelPopuWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();

        }
    }

    public void showNativeAd(View v) {


        TextView recommend_txt = (TextView) v.findViewById(R.id.recommend_txt);
        ImageView native_ad_icon = (ImageView) v.findViewById(R.id.native_ad_icon);
        ImageView native_ad_img = (ImageView) v.findViewById(R.id.native_ad_img);
        TextView native_ad_name = (TextView) v.findViewById(R.id.native_ad_name);
        TextView native_ad_desc = (TextView) v.findViewById(R.id.native_id_desc);
        LinearLayout layout_ad = (LinearLayout) v.findViewById(R.id.layout_ad);

        if (adItem.getIcon() != null) {
            Picasso.with(this).load(adItem.getIcon()).into(native_ad_icon);
        }
        Log.e("image", adItem.getImage());
        native_ad_name.setText(adItem.getTitle());
        native_ad_desc.setText(adItem.getSubTitle());
        recommend_txt.setText(adItem.getAdSourceMark() + " 广告");
        MobclickAgent.onEvent(ReadActivity.this, "show_ad_id", adItem.getTitle());

        native_ad_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(ReadActivity.this, "ad_click", adItem.getTitle());
                //是下载类的就给个提示
                if (adItem.getAdtype() == NativeADDataRef.AD_DOWNLOAD) {
                    Toast.makeText(ReadActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                }
                adItem.onClicked(v);

            }


        });
        //上传点击位置
        native_ad_img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mNativeAD.setParameter(AdKeys.CLICK_POS_DX, event.getX() + "");
                        mNativeAD.setParameter(AdKeys.CLICK_POS_DY, event.getY() + "");
                        break;
                    case MotionEvent.ACTION_UP:
                        mNativeAD.setParameter(AdKeys.CLICK_POS_UX, event.getX() + "");
                        mNativeAD.setParameter(AdKeys.CLICK_POS_UY, event.getY() + "");
                        break;
                    default:
                        break;
                }
                return false;
            }
        });


        Picasso.with(this).load(adItem.getImage()).into(native_ad_img);
        LinearLayout.LayoutParams adparams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 2 * SConfig.SCREEN_WIDTH / 3);

        native_ad_img.setLayoutParams(adparams);

        mPopupWindow = new PopupWindow(v, (int) (SConfig.SCREEN_WIDTH - SConfig.SCREEN_SCALE * 10), LinearLayout.LayoutParams.WRAP_CONTENT, true);

        mPopupWindow.setFocusable(false);
//        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAtLocation(v,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30);

        if (adItem.onExposured(v.findViewById(R.id.layout_ad))) {
//            Toast.makeText(ReadActivity.this, "曝光成功", Toast.LENGTH_SHORT).show();

        } else {

//            Toast.makeText(ReadActivity.this, "曝光失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("event", "触摸");
        return super.onTouchEvent(event);


    }

    @Override
    public void onSingleTap(MotionEvent event) {

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int x = (int) event.getX();
        Log.e("touch", "screenWidth==" + screenWidth + "x==" + x);
        //中间区域点击无效
        if (x > (2 * (screenWidth / 3))) {
            mSlidingLayout.slideNext();//下一页
            cancelPopuWindow();
        } else if (x <= (screenWidth / 3)) {
            mSlidingLayout.slidePrevious();//上一页
            cancelPopuWindow();
        } else {
            //显示菜单
            int totalChapter = 0;
            if (isGetFromMem && !isNetBook) {
                totalChapter = infoUtils.indexChapter.get(mBookName).size();
            } else {
                totalChapter = mChapterData.size();
            }
            //每次显示菜单的时候就更新当前章节名称
            String chapterInfo = mReadAdapter.getCurPageInfo();
            String[] infos = chapterInfo.split(":");
            showReadMenu(SPHelper.getReaderStyle(this), infos[1], Integer.valueOf(infos[0]), totalChapter);
        }
    }

    //显示菜单
    private void showReadMenu(int style, String curName, int curProgress, int maxProgress) {

        if (mMenu == null) {
            mMenu = new ReadMenuUtils(this);
        }
        mMenu.setChangedListener(this);
        mMenu.setUserAttr(style, curName, curProgress, maxProgress, mBgColor, mTxtColor);
        mMenu.showMenu(mDrawLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ReadActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
        //设置屏幕亮度
        BrightnessUtils.setScreenBrightness(this, SPHelper.getReadBrightness(this), true);
        //不自动锁屏
        if (SPHelper.getNotAutoLock(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ReadActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
        boolean special = false;
        //获取章节信息
        String chapterInfo = mReadAdapter.getCurPageInfo();
        String[] infos = chapterInfo.split(":");
        //当前章节号
        int chapter = Integer.valueOf(infos[0]);
        int totalPage = mReadAdapter.getTotalPages(chapter);
        int curPage = mReadAdapter.getCurPage();
        //翻过第一页之后又翻回第一页
        if (curPage > totalPage) {
            chapter++;
            curPage = 1;
            SPHelper.setSpecialPos(this, mBookName, 1);
            special = true;
        }
        //翻到下一章第一页之后又翻回上一章最后一页
        if (curPage < 1) {
            chapter--;
            curPage = mReadAdapter.getTotalPages(chapter);
            Log.e("get6", "翻页");
            SPHelper.setSpecialPos(this, mBookName, 0);
            special = true;
        }
        //正常翻到最后一页
        if (curPage == totalPage) {
            Log.e("get6", "最后一页");
            SPHelper.setSpecialPos(this, mBookName, 0);
            special = true;
        }
        //正常翻到第一页
        if (curPage == 1) {
            SPHelper.setSpecialPos(this, mBookName, 1);
            special = true;
        }
        //保存读书的进度
        SPHelper.setCurrentPage(this, mBookName, curPage);
        SPHelper.setCurChapterNO(this, mBookName, chapter);
        SPHelper.setCurChapterName(this, mBookName, mReadAdapter.getCurChapterName(chapter, curPage));
//        Log.e("TAG", "current position:" + chapter + "," + curPage);
        if (!special) {
            SPHelper.setSpecialPos(this, mBookName, -1);
        }
    }

    @Override
    public void onBackClick() {
        finish();
    }

    //章节列表点击item
    @Override
    public void onChapterItemClick(int position) {

        mDrawLayout.closeDrawer(mChapter_list);
        mOpt = 1;
//        mReadAdapter.setSpecialPos(1);
        if (mDialog != null) {
            Tools.closeProgressDialog(mDialog);
            mDialog = null;
        }
        mDialog = Tools.showProgressDialog(this);
        ChapterDBHelper chapterDBHelper = new ChapterDBHelper(this, mBookName);
        ChapterBean chapterBean1 = chapterDBHelper.readBookOneChapter(position);
        ChapterBean chapterBean2 = null;
        if (position - 1 > 0) {
            chapterBean2 = chapterDBHelper.readBookOneChapter(position - 1);
        }

//        ChapterBean chapterBean = mChapterData.get(position - 1);
        isJumpChapter = true;
        if (chapterBean1 != null && chapterBean1.getPage() == 0) {
            Log.e("chapterclick", chapterBean1.getChapterName() + " " + chapterBean1.getChapterPosition() + " " + chapterBean1.getPage() + " " + chapterBean1.getChapterStart() + " " + chapterBean1.getChapterEnd());

            SPHelper.setCurChapterNO(ReadActivity.this, mBookName, position);
            SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
            if (position > 1) {
                ToDevied(position - 1, chapterBean2, false);
            }

            ToDevied(position, chapterBean1, true);
            chapterDBHelper.closeDB();
            return;
        }
        String chapterName;
        if (isNetBook) {
            chapterName = mChapterData.get(position - 1).getChapterName();
            SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
        } else {
            String info = mReadAdapter.getAssignChapter(position);
            String[] infos = info.split(":");
            chapterName = infos[1];

            SPHelper.setCurrentPage(ReadActivity.this, mBookName, mReadAdapter.getAssignPage(position));
        }
        SPHelper.setCurChapterName(ReadActivity.this, mBookName, chapterName);
        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, position);
        if (mReadAdapter.checkFileExist(position) || !isNetBook) {
            //文件本地已经缓存了，
            setUserAttr();

            SPHelper.setSpecialPos(this, mBookName, 1);//设置特殊位置
        } else {
            //文件不存在的时候去缓存
            Log.e("huancun2", "position==" + position);
            for (int i = position - 1; i <= position + 1; i++) {
//                if (SPHelper.getCache(this, mBookName).contains(i)) {
////
//                    return;
//                } else {

                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), i, i, i == position);
//                }
            }
        }
    }

    private void ToDevied(int position, ChapterBean chapterBean1, Boolean needopen) {
        Intent intent = new Intent(ReadActivity.this, BookIndexService.class);
        if (chapterBean1 != null) {
            intent.putExtra("filePath", chapterBean1.getChapterPosition());
            intent.putExtra("bookName", mBookName);
            intent.putExtra("cacheOne", true);
            intent.putExtra("chapter", position);//章节id
            intent.putExtra("chapterName", chapterBean1.getChapterName());
            intent.putExtra("needOpen", needopen);
            intent.putExtra("encoding", "utf-8");
            startService(intent);
        }

    }


    //显示目录列表
    @Override
    public void onChapterListClick() {
        //getChapterList();

        cancelPopuWindow();
//        mChapter_list.smoothScrollToPosition(mReadAdapter.getCurChapterNO());
        int screen = SPHelper.getPhoneHeight(this);
        Log.e("onChapterListClick", mReadAdapter.getCurChapterNO() + "  " + screen);
        mLL.scrollToPositionWithOffset(mReadAdapter.getCurChapterNO(), screen / 2);
        mDrawLayout.openDrawer(mChapter_list);
        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, mReadAdapter.getCurChapterNO());
        mChapterAdapter.notifyDataSetChanged();
        mMenu.dismissMenu();

    }

    //上一章
    @Override
    public void onPreChapterClick() {
//        int curPage = mReadAdapter.getCurPage();
//        //获取当前页数和章节名称
//        String info = mReadAdapter.getCurPageInfo();
//        String[] infos = info.split(":");
//        mMenu.setCurChapterName(infos[1]);
//        mMenu.setCurProgress(Integer.valueOf(infos[0]));
//        SPHelper.setCurChapterName(ReadActivity.this, mBookName, infos[1]);
//        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, Integer.valueOf(infos[0]));
//        SPHelper.setCurrentPage(ReadActivity.this, mBookName, curPage - 1);
//        setUserAttr();
        cancelPopuWindow();
        String info = mReadAdapter.getPreChapterInfo();
        String[] infos = info.split(":");
        mMenu.setCurChapterName(infos[1]);

        mMenu.setCurProgress(Integer.valueOf(infos[0]));
        SPHelper.setCurChapterName(ReadActivity.this, mBookName, infos[1]);

        upReadUtil(infos);
        Log.e("name", infos[1]);
        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, Integer.valueOf(infos[0]));
        SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
        setUserAttr();
    }

    private void upReadUtil(String[] infos) {
        int totalChapter = 0;
        if (isGetFromMem && !isNetBook) {
            totalChapter = infoUtils.indexChapter.get(mBookName).size();
        } else {
            totalChapter = mChapterData.size();
        }
        mMenu.updateView(infos[1], Integer.valueOf(infos[0]), totalChapter);
    }

    //下一章
    @Override
    public void onNextChapterClick() {
//        int curPage = mReadAdapter.getCurPage();
        //获取当前页数和章节名称
//        String info = mReadAdapter.getCurPageInfo();
//        String[] infos = info.split(":");
//        mMenu.setCurChapterName(infos[1]);
//        mMenu.setCurProgress(Integer.valueOf(infos[0]));
//        SPHelper.setCurChapterName(ReadActivity.this, mBookName, infos[1]);
//        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, Integer.valueOf(infos[0]));
//        SPHelper.setCurrentPage(ReadActivity.this, mBookName, curPage + 1);
//        setUserAttr();
        cancelPopuWindow();
        String info = mReadAdapter.getNextChapterInfo();
        String[] infos = info.split(":");
        mMenu.setCurChapterName(infos[1]);
        mMenu.setCurProgress(Integer.valueOf(infos[0]));

        SPHelper.setCurChapterName(ReadActivity.this, mBookName, infos[1]);
        upReadUtil(infos);
        SPHelper.setCurChapterNO(ReadActivity.this, mBookName, Integer.valueOf(infos[0]));
        SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
        setUserAttr();
    }

    //改变进度
    @Override
    public void onProgressChanged(int progress, int opt) {
        //获取当前页数和章节名称
//        mChapter_list.smoothScrollToPosition(progress);
//        if (mDialog != null) {
//            Tools.closeProgressDialog(mDialog);
//            mDialog = null;
//        }
//        mDialog = Tools.showProgressDialog(this);

        String chapterName;
        if (isNetBook) {
            chapterName = mChapterData.get(progress - 1).getChapterName();
            SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
            mMenu.setProgressBar(progress, mChapterData.size());

        } else {
            String info = mReadAdapter.getAssignChapter(progress);
            String[] infos = info.split(":");
            chapterName = infos[1];
            SPHelper.setCurrentPage(ReadActivity.this, mBookName, mReadAdapter.getAssignPage(progress));
            mMenu.setProgressBar(progress, mChapterData.size());
        }

        mMenu.setCurChapterName(chapterName);
        if (opt == ReadMenuUtils.PROGRESS_CHANGED) {
            mOpt = 1;
            isJumpChapter = true;
            SPHelper.setCurChapterName(ReadActivity.this, mBookName, chapterName);
            SPHelper.setCurChapterNO(ReadActivity.this, mBookName, progress);
//            如果本地有缓存但是没分页
            ChapterDBHelper chapterDBHelper = new ChapterDBHelper(this, mBookName);
            ChapterBean chapterBean1 = chapterDBHelper.readBookOneChapter(progress);
            ChapterBean chapterBean2 = null;
            if (progress - 1 > 0) {
                chapterBean2 = chapterDBHelper.readBookOneChapter(progress - 1);
            }

//        ChapterBean chapterBean = mChapterData.get(position - 1);

            if (chapterBean1 != null && chapterBean1.getPage() == 0) {
                Log.e("chapterclick", chapterBean1.getChapterName() + " " + chapterBean1.getChapterPosition() + " " + chapterBean1.getPage() + " " + chapterBean1.getChapterStart() + " " + chapterBean1.getChapterEnd());

                SPHelper.setCurChapterNO(ReadActivity.this, mBookName, progress);
                SPHelper.setCurrentPage(ReadActivity.this, mBookName, 1);
                if (progress > 1) {
                    ToDevied(progress - 1, chapterBean2, false);
                }

                ToDevied(progress, chapterBean1, true);
                chapterDBHelper.closeDB();
                return;
            }

            if (mReadAdapter.checkFileExist(progress) || !isNetBook) {
                setUserAttr();
            } else {
                //文件不存在的时候去缓存
                Log.e("huancun3", "progress" + progress);
                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), progress - 3, progress - 3, false);
                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), progress - 2, progress - 2, false);
                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), progress - 1, progress - 1, false);
                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), progress + 1, progress + 1, false);
                CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), progress, progress, true);
            }
        }
    }

    //改变字体
    @Override
    public void onTxtSizeChanged(int progress) {

        MobclickAgent.onEvent(this, "textsizechange", (progress * 2 + 14) + " ");
//        if (!isNetBook && SPHelper.getSaveInfo(this)) {
//            ToastUtil.showToast(this, "正在后台储存分页信息，请稍后切换字体", Toast.LENGTH_SHORT);
//            return;
//        }
        Log.e("change", "changesize" + progress);
        tsize = progress;


        int chapter = mReadAdapter.getCurChapterNO();//当前章节
//        Log.e("tag", "当前" + chapter);
        if (mChapterDBHelper == null) {

            mChapterDBHelper = new ChapterDBHelper(this, mBookName);
            Log.e("tag", "mChapterDBHelper" + mBookName);
        }

        ChapterBean bean;
        if (isNetBook) {
            bean = mChapterDBHelper.readBookOneChapter(chapter);
            PageDBHelper pageDb = new PageDBHelper(this, mBookName);
            pageDb.delete();
        } else {
            bean = infoUtils.readChapter(mBookName, chapter);
//            移除分页信息
            infoUtils.remove(mBookName);
//            Log.e("text123", chapter + bean.getChapterName() + " " + bean.getPage() + " " + bean.getChapterId());
        }


        if (bean != null) {//这个时候可以去从新分页
            int size = SPHelper.getBookInfoSize(ReadActivity.this, mBookName);
//            int size = SPHelper.getBookTextSize(ReadActivity.this);
            Log.e("haschange", "size" + size + "  " + (progress * 2 + 14));
            if (size != (progress * 2 + 14)) {
                SPHelper.setBookInfoSize(ReadActivity.this, mBookName, (progress * 2 + 14));
                ToastUtil.showToast(ReadActivity.this, "改变字体", Toast.LENGTH_SHORT);
            } else {
                return;

            }
            if (mDialog != null) {
                Tools.closeProgressDialog(mDialog);
                mDialog = null;
            }
            mDialog = Tools.showProgressDialog(this);
            SPHelper.setBookTextSize(this, progress * 2 + 14);

            Log.e("changesize", "现在是第" + mReadAdapter.getCurPage() + "页，" + "现在是第" + mReadAdapter.getCurChapterNO() + "章");
            SPHelper.setCurrentPage(this, mBookName, mReadAdapter.getCurPage());
            SPHelper.setCurChapterNO(this, mBookName, chapter);
//            SPHelper.setCurrentPage(this, mBookName, 1);

            //重新分页
            DividePagesUtil util = DividePagesUtil.getInstance(this);
            util.getCurViewInfo(progress * 2 + 14);
            isJumpChapter = true;
            //先对当前章节内容进行分页

            if (isNetBook) {
                //对已经存在的进行分页
                List<ChapterBean> data = mChapterDBHelper.readBookChapterList();
                Log.e("times", isNetBook + " " + data.size() + "一共存在的章节");
                ArrayList<ChapterBean> needdevidedata = new ArrayList<ChapterBean>();

                for (int i = 0; i < data.size(); i++) {
                    //对剩下的其他章节进行分页
                    int closeChapter = data.get(i).getChapterId();

                    if (closeChapter != chapter && Math.abs(closeChapter - chapter) < 2) {
                        needdevidedata.add(data.get(i));
                    }
                }
//            Toast.makeText(ReadActivity.this, isNetBook + mBookName + "这是网络小说", Toast.LENGTH_SHORT).show();
                if (infoUtils.pageDetail.get(mBookName) != null) {
                    infoUtils.pageDetail.get(mBookName).clear();
                    infoUtils.indexChapter.get(mBookName).clear();
                    Log.e("clear", "清除完毕");
                }
//            Log.e("changesize", "换字体的当前章节" + mReadAdapter.getCurChapter());
                todoDividePage(null, bean, true, true, isNetBook);
                Log.e("times", "size==" + needdevidedata.size());
                if (needdevidedata.size() > 0) {
                    Log.e("textchangechapter", needdevidedata.size() + " ");
                    todoDividePage(needdevidedata, bean, true, false, isNetBook);
                }
            } else {
                todoDividePage(null, bean, true, true, isNetBook);
            }


//            for (int i = 0; i < data.size(); i++) {
//                //对剩下的其他章节进行分页
//                if (data.get(i).getChapterId() != chapter) {
//                    todoDividePage(data.get(i), false);
//                }
//            }
        }
//        }
    }

    //去分页
    private void todoDividePage(ArrayList list, ChapterBean bean, boolean isCacheOne, boolean needopen, boolean isNetbook) {
        Intent intent = new Intent(this, BookIndexService.class);
        if (isNetBook) {
            intent.putExtra("filePath", bean.getChapterPosition());
        } else {
            intent.putExtra("filePath", SPHelper.getBookFilePath(ReadActivity.this, mBookName));
        }
        intent.putExtra("bookName", mBookName);
        intent.putExtra("cacheOne", isCacheOne);
        intent.putExtra("chapter", bean.getChapterId());
        intent.putExtra("encoding", SPHelper.getBookEnCoding(this, mBookName));
        if (!isNetbook) {
            intent.putExtra("bookType", 2);
        }

        intent.putStringArrayListExtra("needdevide", list);
        intent.putExtra("redevide", true);
        intent.putExtra("needOpen", needopen);
//        intent.putExtra("fenye", true);
        Log.e("fenye", "重新分页");
        startService(intent);
    }

    @Override
    public void onStyleChanged(int style) {
        SPHelper.setReadStyle(this, style);
        SPHelper.setCurrentPage(this, mBookName, mReadAdapter.getCurPage());
        SPHelper.setCurChapterNO(this, mBookName, mReadAdapter.getCurChapterNO());
        Log.e("setcur", mBookName + " " + mReadAdapter.getCurChapterNO() + " " + mReadAdapter.getCurPage());
        setUserAttr();
    }

    //改变亮度
    @Override
    public void onBrightnessChanged(int progress) {
        SPHelper.setReadBrightness(this, progress);
        BrightnessUtils.setScreenBrightness(this, progress, true);
    }

    //去缓存章节
    @Override
    public void onCacheFile(int chapter, int opt) {
        //禁止再次点击
        if (mDialog != null) {
            Tools.closeProgressDialog(mDialog);
            mDialog = null;
        }
        mOpt = opt;
//        mDialog = Tools.showProgressDialog(this);
//        mSlidingLayout.setCanMove(false);
        Log.e("get huancun", "cache file in get next chapter");
//        CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), chapter, chapter, true);
        Log.e("get huancun", "缓存章节");
        CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), chapter, chapter, false);

        CacheUtils.cacheFile(this, mBookName, mBookInfo.getBook_id(), chapter + 1, chapter + 1, false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mBookCaseDBHelper.closeDB();
        if (mMenu != null) {
            mMenu.dismissMenu();
        }
    }

    //加载网络端目录列表
    private void getChapterListOnline() {
        String cid = "";
        if (SConfig.IMSI != null) {
            cid = SConfig.IMSI;
        } else if (SConfig.IMEI != null) {
            cid = SConfig.IMEI;
        } else {
            cid = SConfig.MAC;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("token", SPHelper.getUserToken(this));
        params.put("id", mBookInfo.getBook_id());
        params.put("pageno", 0);
        OkHttpUtils.postString().url(APIDefine.GET_NOVEL_DIR)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                //Log.e("TAG", "get  novel chapter list error:" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                // Log.e("TAG", "get novel chapter list response:" + response);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    String baseUrl = root.getString("baseUrl");
                    if (1 == rc) {
                        JSONArray data = root.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            ChapterBean bean = new ChapterBean();
                            bean.setBookName(mBookName);
                            JSONObject value = data.getJSONObject(i);
                            bean.setChapterName(value.getString("name"));
                            bean.setChapterId(value.getInt("no"));
                            Log.e("chaponline", value.getInt("no") + " " + value.getString("name"));
                            bean.setChapterPosition(baseUrl + value.getString("url"));
                            mChapterData.add(bean);
                        }
                        //设置最后一章信息
                        Log.e("setinfo", mChapterData.size() + " " + mBookName);
                        mBookInfo.setLast_chapter_no(mChapterData.size());
                        SPHelper.setObject(ReadActivity.this, mBookName + "chapterlist", mChapterData);
                        SPHelper.setBookLength(ReadActivity.this, mBookName, mChapterData.size());
                        mBookInfo.setLast_chapter(mChapterData.get(mChapterData.size() - 1).getChapterName());
                        mBookCaseDBHelper.updateBookCase(mBookInfo, BookCaseDBHelper.BOOK_CHECK_UPDATE);
                        mChapterAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
