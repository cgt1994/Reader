package com.syezon.reader.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.widget.ScaleSeekBar;

import java.text.DecimalFormat;

/**
 * 阅读界面的菜单选项
 * Created by jin on 2016/9/30.
 */
public class ReadMenuUtils extends PopupWindow implements View.OnClickListener {

    public static final int PROGRESS_CHANGING = 0;
    public static final int PROGRESS_CHANGED = 1;

    private View mConvertView;
    private LinearLayout mLinearTitle, mLinearMenu;
    private TextView mTv_back, mTv_bookcase;
    private View mView_center;
    private LinearLayout mLinear_progress, mLinear_style, mLinear_font, ll_statsbar;
    private TextView mTv_pre, mTv_next, mTv_chapter_name, tv_pro;
    private SeekBar mSb_progress;
    private TextView mTv_brightness_left, mTv_brightness_right;
    private SeekBar mSb_brightness;
    private TextView mTv_white, mTv_pink, mTv_yellow, mTv_night;
    private TextView mTv_font_left, mTv_font_right;
    private ScaleSeekBar mSb_font;

    private TextView mTv_chapter, mTv_progress, mTv_style, mTv_font;

    private Context mContext;
    private PopupWindow mPopWindow;

    //监听用户操作
    public interface IUserChangedListener {
        void onBackClick();

        void onChapterListClick();

        void onPreChapterClick();

        void onNextChapterClick();

        void onProgressChanged(int progress, int opt);

        void onTxtSizeChanged(int progress);

        void onBrightnessChanged(int progress);

        void onStyleChanged(int style);
    }

    private IUserChangedListener mListener;

    public void setChangedListener(IUserChangedListener listener) {
        mListener = listener;
    }

    public ReadMenuUtils(Context context) {
        mContext = context;
        if (mConvertView == null) {
            initView();
        }
    }

    //初始化菜单
    public void initView() {
        mConvertView = LayoutInflater.from(mContext).inflate(R.layout.view_read_menu, null);

        mLinearTitle = (LinearLayout) mConvertView.findViewById(R.id.linear_title);
        mTv_back = (TextView) mConvertView.findViewById(R.id.tv_back);
        mTv_bookcase = (TextView) mConvertView.findViewById(R.id.tv_bookcase);

        mView_center = mConvertView.findViewById(R.id.center);

        mLinearMenu = (LinearLayout) mConvertView.findViewById(R.id.linear_menu);
        mLinear_progress = (LinearLayout) mConvertView.findViewById(R.id.linear_progress);
        mTv_pre = (TextView) mConvertView.findViewById(R.id.tv_pre);
        mTv_next = (TextView) mConvertView.findViewById(R.id.tv_next);
        mTv_chapter_name = (TextView) mConvertView.findViewById(R.id.tv_cur_chapter);
        mSb_progress = (SeekBar) mConvertView.findViewById(R.id.sb_chapter);
        tv_pro = (TextView) mConvertView.findViewById(R.id.tv_pro);
        mLinear_style = (LinearLayout) mConvertView.findViewById(R.id.linear_style);
        mTv_brightness_left = (TextView) mConvertView.findViewById(R.id.tv_brightness_left);
        mTv_brightness_right = (TextView) mConvertView.findViewById(R.id.tv_brightness_right);
        mSb_brightness = (SeekBar) mConvertView.findViewById(R.id.sb_brightness);
        mTv_white = (TextView) mConvertView.findViewById(R.id.tv_white);
        mTv_pink = (TextView) mConvertView.findViewById(R.id.tv_pink);
        mTv_yellow = (TextView) mConvertView.findViewById(R.id.tv_yellow);
        mTv_night = (TextView) mConvertView.findViewById(R.id.tv_night);
        ll_statsbar = (LinearLayout) mConvertView.findViewById(R.id.ll_statsbar);
        mLinear_font = (LinearLayout) mConvertView.findViewById(R.id.linear_font);
        mTv_font_left = (TextView) mConvertView.findViewById(R.id.tv_font_left);
        mTv_font_right = (TextView) mConvertView.findViewById(R.id.tv_font_right);
        mSb_font = (ScaleSeekBar) mConvertView.findViewById(R.id.sb_font);

        mTv_chapter = (TextView) mConvertView.findViewById(R.id.tv_chapter);
        mTv_progress = (TextView) mConvertView.findViewById(R.id.tv_progress);
        mTv_style = (TextView) mConvertView.findViewById(R.id.tv_style);
        mTv_font = (TextView) mConvertView.findViewById(R.id.tv_font);
        if (SConfig.MANUFACTURER != null) {
            if (SConfig.MANUFACTURER.equals("vivo")) {
                ll_statsbar.setVisibility(View.VISIBLE);
            }
        }


        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "iconfont.ttf");
        mTv_back.setTypeface(typeface);
        mTv_back.setTextSize(18);
        mTv_chapter.setTypeface(typeface);
        mTv_chapter.setTextSize(18);
        mTv_progress.setTypeface(typeface);
        mTv_progress.setTextSize(18);
        mTv_style.setTypeface(typeface);
        mTv_style.setTextSize(20);
        mTv_font.setTypeface(typeface);
        mTv_pre.setTypeface(typeface);
        mTv_pre.setTextSize(18);
        mTv_next.setTypeface(typeface);
        mTv_next.setTextSize(18);
        mTv_brightness_left.setTypeface(typeface);
        mTv_brightness_left.setTextSize(14);
        mTv_brightness_right.setTypeface(typeface);
        mTv_brightness_right.setTextSize(20);
        mTv_font_left.setTypeface(typeface);
        mTv_font_left.setTextSize(12);
        mTv_font_right.setTypeface(typeface);
        mTv_font_right.setTextSize(18);
        mTv_night.setTypeface(typeface);

        mTv_back.setOnClickListener(this);
        mTv_bookcase.setOnClickListener(this);
        mTv_pre.setOnClickListener(this);
        mTv_next.setOnClickListener(this);
        mTv_white.setOnClickListener(this);
        mTv_pink.setOnClickListener(this);
        mTv_yellow.setOnClickListener(this);
        mTv_night.setOnClickListener(this);
        mSb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 0) {
                    mListener.onProgressChanged(progress, PROGRESS_CHANGING);
                    Log.e("progress",progress+"");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() > 0) {
                    mListener.onProgressChanged(seekBar.getProgress(), PROGRESS_CHANGED);
                }
            }
        });

        mSb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onBrightnessChanged(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSb_font.setResponseOnTouch(new ScaleSeekBar.ResponseOnTouch() {
            @Override
            public void onTouchResponse(int position) {
                mListener.onTxtSizeChanged(position);
            }
        });

        mTv_chapter.setOnClickListener(this);
        mTv_progress.setOnClickListener(this);
        mTv_style.setOnClickListener(this);
        mTv_font.setOnClickListener(this);
        mView_center.setOnClickListener(this);
    }

    public void updateView(String chaptername, int cur, int total) {
        mTv_chapter_name.setText(chaptername);
        setProgressBar(cur, total);
    }

    public void setProgressBar(double cur, double total) {
        DecimalFormat df = new DecimalFormat("######0.00");
        tv_pro.setText(df.format(cur * 100 / total) + "%");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
            case R.id.tv_bookcase:
                mListener.onBackClick();
                break;
            case R.id.tv_chapter:
                mListener.onChapterListClick();
                break;
            case R.id.tv_progress:
                mLinear_progress.setVisibility(View.VISIBLE);
                mLinear_style.setVisibility(View.GONE);
                mLinear_font.setVisibility(View.GONE);
                break;
            case R.id.tv_style:
                mLinear_progress.setVisibility(View.GONE);
                mLinear_style.setVisibility(View.VISIBLE);
                mLinear_font.setVisibility(View.GONE);
                break;
            case R.id.tv_font:
                mLinear_progress.setVisibility(View.GONE);
                mLinear_style.setVisibility(View.GONE);
                mLinear_font.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_pre:
                mListener.onPreChapterClick();
                break;
            case R.id.tv_next:
                mListener.onNextChapterClick();


                break;
            case R.id.tv_white:
                mListener.onStyleChanged(0);
                changeStyle(0, R.color.color1, R.color.white_yellow);
                break;
            case R.id.tv_pink:
                mListener.onStyleChanged(1);
//                changeStyle(1, R.color.color2, R.color.pink_blue);
                break;
            case R.id.tv_yellow:
                mListener.onStyleChanged(2);
//                changeStyle(2, R.color.color3, R.color.yellow_brown);
                break;
            case R.id.tv_night:
                mListener.onStyleChanged(3);
//                changeStyle(3, R.color.night_bg, R.color.night_blue);
                break;
            case R.id.center:
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
                break;
        }
    }

    //恢复之前用户操作后的状态
    public void setUserAttr(int style, String curChapterName, int curProgress, int progressMax, int bgColor, int txtColor) {
        changeStyle(0, R.color.color1, R.color.white_yellow);
        mTv_chapter_name.setText(curChapterName);
        DecimalFormat df = new DecimalFormat("######0.00");
        Log.e("jindu", curProgress + " " + progressMax);
        tv_pro.setText(df.format((double) curProgress * 100 / (double) progressMax) + "%");
        mSb_brightness.setProgress(SPHelper.getReadBrightness(mContext));
        Log.e("progress", "setUser" + ((SPHelper.getBookTextSize(mContext) - 14) / 2));
        mSb_font.setProgress((SPHelper.getBookTextSize(mContext) - 14) / 2);
        mSb_progress.setMax(progressMax);
        mSb_progress.setProgress(curProgress);
    }

    //设置当前章节名称
    public void setCurChapterName(String curChapterName) {
        mTv_chapter_name.setText(curChapterName);
    }

    //设置当前进度值
    public void setCurProgress(int progress) {
        mSb_progress.setProgress(progress);
    }

    //设置最大值
    public void setMaxProgress(int progress) {
        mSb_progress.setMax(progress);
    }

    //改变风格
    public void changeStyle(int style, int bgColor, int txtColor) {
        mTv_chapter.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_progress.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_style.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_font.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_back.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_bookcase.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_pre.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_next.setTextColor(mContext.getResources().getColor(txtColor));
        mTv_chapter_name.setTextColor(mContext.getResources().getColor(txtColor));

        mLinearTitle.setBackgroundColor(mContext.getResources().getColor(bgColor));
        mLinearMenu.setBackgroundColor(mContext.getResources().getColor(bgColor));

        setSBStyle(mSb_brightness, style);
        setSBStyle(mSb_progress, style);
    }

    //设置seekBar的样式
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSBStyle(SeekBar seekBar, int style) {
        switch (style) {
            case 0:
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbar_white));
                seekBar.setThumb(mContext.getResources().getDrawable(R.mipmap.thumb_white));
                mSb_font.setThumb(R.mipmap.thumb_white);
                mSb_font.setLineColor(0x50929292);
                break;
            case 1:
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbar_pink));
                seekBar.setThumb(mContext.getResources().getDrawable(R.mipmap.thumb_pink));
                mSb_font.setThumb(R.mipmap.thumb_pink);
                mSb_font.setLineColor(Color.WHITE);
                break;
            case 2:
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbar_yellow));
                seekBar.setThumb(mContext.getResources().getDrawable(R.mipmap.thumb_yellow));
                mSb_font.setThumb(R.mipmap.thumb_yellow);
                mSb_font.setLineColor(Color.WHITE);
                break;
            case 3:
                seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbar_night));
                seekBar.setThumb(mContext.getResources().getDrawable(R.mipmap.thumb_night));
                mSb_font.setThumb(R.mipmap.thumb_night);
                mSb_font.setLineColor(Color.WHITE);
                break;
        }
    }

    //显示菜单
    public void showMenu(View parent) {
        if (mPopWindow == null) {
            mPopWindow = new PopupWindow(mConvertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        }
        mPopWindow.setTouchable(true);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(0x50505050));
        mPopWindow.setAnimationStyle(R.style.menu_style);
        mPopWindow.showAsDropDown(parent, 0, -parent.getMeasuredHeight());
//        mPopWindow.showAsDropDown(parent, 0, 0);
    }

    public void dismissMenu() {
        if (mPopWindow != null) {
            mPopWindow.dismiss();
        }
    }
}
