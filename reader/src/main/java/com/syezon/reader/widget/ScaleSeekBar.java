package com.syezon.reader.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.syezon.reader.R;

import java.util.ArrayList;

/**
 * 带刻度的seekBar
 * Created by jin on 2016/9/23.
 */
public class ScaleSeekBar extends View {
    private int width;
    private int height;
    private int downX = 0;
    private int downY = 0;
    private int upX = 0;
    private int upY = 0;
    private int moveX = 0;
    private int moveY = 0;
    private int perWidth = 0;
    private Paint mPaint;
    private int mLineColor = Color.WHITE;
    private Paint buttonPaint;
    private Canvas canvas;
    private Bitmap bitmap;
    private Bitmap thumb;
    private Bitmap spot;
    private int cur_sections = 2;
    private ResponseOnTouch responseOnTouch;
    private int bitMapHeight = 38;//第一个点的起始位置起始，图片的长宽是76，所以取一半的距离
    private ArrayList<String> section_title;

    public ScaleSeekBar(Context context) {
        super(context);
    }

    public ScaleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        cur_sections = 0;
        bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
        thumb = BitmapFactory.decodeResource(getResources(), R.mipmap.thumb_white);
        spot = BitmapFactory.decodeResource(getResources(), R.mipmap.spot);
        bitMapHeight = thumb.getHeight();
        mPaint = new Paint(Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);//锯齿不显示
        mPaint.setStrokeWidth(3);
        buttonPaint = new Paint(Paint.DITHER_FLAG);
        buttonPaint.setAntiAlias(true);
        initData(null);
    }

    /**
     * 实例化后调用，设置bar的段数和文字
     */
    public void initData(ArrayList<String> section) {
        if (section != null) {
            section_title = section;
        } else {
            String[] str = new String[]{"", "", "", "", "", "", ""};
            section_title = new ArrayList<String>();
            for (int i = 0; i < str.length; i++) {
                section_title.add(str[i]);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        width = widthSize;

        float scaleX = widthSize / 1080;
        float scaleY = heightSize / 1920;
        //控件的高度
        height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, getResources().getDisplayMetrics());
        setMeasuredDimension(width, height);
        width = width - bitMapHeight / 2;
        perWidth = (width - section_title.size() * spot.getWidth() - thumb.getWidth()) / (section_title.size() - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setAlpha(255);
        mPaint.setColor(mLineColor);
        //画线
        canvas.drawLine(bitMapHeight, height / 2, width - bitMapHeight, height / 2, mPaint);
        int section = 0;
        //画小圆点
        while (section < section_title.size()) {
            mPaint.setAlpha(255);
            canvas.drawBitmap(spot, thumb.getWidth() / 2 + section * (width / section_title.size()), height / 2 - thumb.getHeight() / 2, mPaint);
            section++;
        }
        //画游标
        canvas.drawBitmap(thumb, thumb.getWidth() / 2 + cur_sections * (width / section_title.size()), height / 2 - thumb.getHeight() / 2, buttonPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                responseTouch(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                moveX = (int) event.getX();
                moveY = (int) event.getY();
                responseTouch(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                upX = (int) event.getX();
                upY = (int) event.getY();
                responseTouch(upX, upY);
                responseOnTouch.onTouchResponse(cur_sections);
                break;
        }
        return true;
    }

    private void responseTouch(int x, int y) {
        cur_sections = x / (width / section_title.size());//当前的位置除于每段的长度就是当前的point
//        Log.e("pro","x+"="cur"+cur_sections);
        //防止画到界外
        if (cur_sections > section_title.size() - 1) {
            cur_sections = section_title.size() - 1;
        }
        invalidate();
    }

    public interface ResponseOnTouch {
        void onTouchResponse(int position);
    }

    //设置监听
    public void setResponseOnTouch(ResponseOnTouch response) {
        responseOnTouch = response;
    }

    //设置选择的图标
    public void setThumb(int bitmapResId) {
        thumb = BitmapFactory.decodeResource(getResources(), bitmapResId);
        invalidate();
    }

    //设置线的颜色
    public void setLineColor(int color) {
        mLineColor = color;
        invalidate();
    }

    //设置进度
    public void setProgress(int progress) {
        cur_sections = progress;
                Log.e("progress","progress=="+progress);
                invalidate();
    }
}
