package com.syezon.reader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.syezon.reader.utils.SConfig;

/**
 * Created by jin on 2017/2/21.
 */
public class BatteryView extends View {
    //    电池身体的画笔
    private Paint batteryPaint;
    //    电池容量的画笔
    private Paint mPowerPaint;


    private float mBatteryStroke = 1f;
    /**
     * 电池参数
     */
    private float mBatteryHeight; // 电池的高度
    private float mBatteryWidth; // 电池的宽度
    ;
    private float mCapHeight;
    private float mCapWidth;
    /**
     * 矩形
     */
    private RectF mBatteryRect;
    private RectF mCapRect;
    private RectF mPowerRect;
    private float mBatteryMargin;
    private float mPowerMargin;

    public BatteryView(Context context) {
        super(context);
        initView();
    }

    public void setBatterStatus(boolean isCharge) {
        if (isCharge) {
            mPowerPaint.setColor(Color.parseColor("#43C504"));
        } else {
            mPowerPaint.setColor(Color.GRAY);
        }
        invalidate();
    }

    public void setWideAndHeight(float percent) {
        Log.e("power", mPowerMargin + " " + mBatteryWidth + " " + percent + " " + mBatteryHeight);
        mPowerRect = new RectF(mPowerMargin, mPowerMargin, mPowerMargin + percent * (mBatteryWidth), (float) (mPowerMargin + mBatteryHeight - 2 * SConfig.SCREEN_SCALE));

        invalidate();
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * 屏幕高宽
     */
    private int measureWidth;
    private int measureHeigth;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("onMeasure", "调用");
        measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureHeigth = MeasureSpec.getSize(heightMeasureSpec);

        Log.e("onMeasure", "调用" + measureWidth + " " + measureHeigth);
//        setMeasuredDimension((int) mBatteryWidth, (int) mBatteryHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension((int) mBatteryWidth, (int) mBatteryHeight);
    }

    private Paint capPaint;

    private void initView() {
        mBatteryMargin = (float) (5 * SConfig.SCREEN_SCALE);
        mPowerMargin = (float) (6 * SConfig.SCREEN_SCALE);
        mBatteryHeight = (float) (10 * SConfig.SCREEN_SCALE);
        mBatteryWidth = (float) (22.5 * SConfig.SCREEN_SCALE);
        mCapHeight = (float) (4 * SConfig.SCREEN_SCALE);
        mCapWidth = (float) (1.5 * SConfig.SCREEN_SCALE);

        Log.e("margin", mBatteryMargin + " " + mPowerMargin);
//        画身体
        batteryPaint = new Paint();
        batteryPaint.setColor(Color.GRAY);
        batteryPaint.setAntiAlias(true);
        batteryPaint.setStyle(Paint.Style.STROKE);
        batteryPaint.setStrokeWidth(mBatteryStroke);

        mBatteryRect = new RectF(mBatteryMargin, mBatteryMargin, mBatteryMargin + mBatteryWidth, mBatteryMargin + mBatteryHeight);

        /**
         * 设置电量画笔
         */
        mPowerPaint = new Paint();
        mPowerPaint.setColor(Color.GRAY);
        mPowerPaint.setAntiAlias(true);
        mPowerPaint.setStyle(Paint.Style.FILL);
        mPowerPaint.setStrokeWidth(mBatteryStroke);


//        mPowerRect = new RectF(mPowerMargin, mPowerMargin, mPowerMargin + mPowerWidth, mPowerMargin + mBatteryHeight - 2);


        mCapRect = new RectF(mBatteryMargin + mBatteryWidth, mBatteryMargin + (mBatteryHeight - mCapHeight) / 2, mBatteryMargin + mBatteryWidth + mCapWidth, mBatteryMargin + mBatteryHeight - (mBatteryHeight - mCapHeight) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mBatteryRect, batteryPaint);
        if (mPowerRect != null) {

            canvas.drawRect(mPowerRect, mPowerPaint);
        }
        canvas.drawRect(mCapRect, batteryPaint);
    }
}
