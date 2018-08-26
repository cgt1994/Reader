package com.syezon.reader.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.syezon.reader.R;
import com.syezon.reader.view.CYTextView;

import java.lang.reflect.Field;

/**
 * 分页工具类
 * Created by jin on 2016/9/8.
 */
public class DividePagesUtil {

    private Context mContext;
    private int mViewWidth;//当前页的宽度
    private int mScreenDensity;//当前屏幕密度
    private int mCurViewLines;//当前页能显示的行数
    private static Paint mPaint;//当前textView的paint

//    public NativeADDataRef getmAdItem() {
//        return mAdItem;
//    }

    private static DividePagesUtil instance = null;

    public static DividePagesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new DividePagesUtil(context);
        }
        return instance;
    }

    public DividePagesUtil(Context context) {

//        if (mAdItem == null) {
//            if (SPHelper.getNativeADIsOpen(context)) {
//                ADUtils.showNativeAD(context, this);
//                Log.e("adver123", "初始化");
//            }
//        }
        mContext = context;
    }

    // 通过反射返回状态栏高度
    private int getActionBarHeigh() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return mContext.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return 0;
    }

    private int getNavigationBarHeigh() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    //获取当前页的高度
    public int getCurViewHeight() {
        WindowManager m = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        m.getDefaultDisplay().getMetrics(metrics);
//        获取 高度 宽度像素
        mViewWidth = metrics.widthPixels;
        mScreenDensity = (int) metrics.density;
        Log.e("tag", "density" + mScreenDensity);
        int viewheight = metrics.heightPixels;
        SPHelper.setPhoneHeight(mContext, viewheight);
//        Log.e("tag","viewheight"+viewheight);
        int barheight = getActionBarHeigh();
        int navigationheight = getNavigationBarHeigh();
        //viewheight=viewheight-(barheight+navigationheight);
        return viewheight;
    }

    /**
     * 获取当前页能显示的行数
     *
     * @param paddingTop    距离上面距离
     * @param paddingBottom 距离下面距离
     * @param linesPadding  行间距
     * @param textSize      字体大小
     */
    private int getCurLines(int paddingTop, int paddingBottom, int linesPadding, int textSize) {
        mCurViewLines = (getCurViewHeight() - paddingTop - paddingBottom)
                / ((linesPadding + textSize));
        Log.e("getCurLines", "viewHeight:" + getCurViewHeight() + "," + paddingTop + "," + paddingBottom + "," + linesPadding + "," + textSize);
        Log.e("getCurLines", "curLines:" + mCurViewLines);
        return mCurViewLines;
    }

    //计算当前模式下字数和行数
    public void getCurViewInfo(int i) {

        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_read_content, null);
        CYTextView textView = (CYTextView) layout.findViewById(R.id.tv_content);
        if (SPHelper.getTextFix(mContext) == -1)
            SPHelper.setTextFix(mContext, textView.textFix());
        Log.e("tag", SPHelper.getBookTextSize(mContext) + "TextSize");
        if (i == 0) {
            textView.setTextSize(SPHelper.getBookTextSize(mContext));
        } else {
            textView.setTextSize(i);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(params);
        //计算行数
        Log.e("tag", textView.getPaddingTop() + " " + textView.getPaddingBottom() + "  " + textView.GetHeight());
        getCurLines(textView.getPaddingTop(), textView.getPaddingBottom(), textView.GetHeight(), 0);
        SPHelper.setBookLines(mContext, mCurViewLines);//顶部一行放标题
        SPHelper.setCutLines(mContext, mScreenDensity);
        //计算字数
        Log.e("zishu", "mViewWidth" + (mViewWidth - textView.getPaddingLeft() - textView.getPaddingRight()) + "textView.getTextSize())" + textView.GetTextsize());
//        是因为textview设置了padding=10dp
        Log.e("real", textView.getRealSize() + "真实字体大小");
        SPHelper.setCurTxtNums(mContext, ((mViewWidth - textView.getPaddingLeft() - textView.getPaddingRight()) / textView.getRealSize()));
    }


}
