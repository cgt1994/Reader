package com.syezon.reader.view;

;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.utils.SPHelper;

import java.util.Vector;


/**
 * Created by Icy on 2016/11/11.
 */

public class CYTextView extends TextView {
    public static int m_iTextHeight; //文本的高度
    public static int m_iTextWidth;//文本的宽度

    private Paint mPaint = null;
    private String string = "";
    private float LineSpace = 0;//行间距
    private static int width;//屏幕宽度
    private Context mcontext;
    private float tsize;
    private float density;

    public CYTextView(Context context, AttributeSet set) {
        super(context, set);
        mcontext = context;
        TypedArray typedArray = context.obtainStyledAttributes(set, R.styleable.CYTextView);
        setdensity(context);
//        int width = typedArray.getInt(R.styleable. CYTextView_textwidth, 320);
//

        width = context.getResources().getDisplayMetrics().widthPixels - (int) (density * 20);
        Log.e("wide", width + "   " + (int) (density * 20));

//        float textsize = typedArray.getDimension(R.styleable.CYTextView_cy_textSize, 20);
        float textsize = SPHelper.getBookTextSize(context);
        Log.e("view", textsize + "    ddd 11");
        tsize = textsize;
        int textcolor = typedArray.getColor(R.styleable.CYTextView_cy_textColor, -1442840576);
        float linespace = typedArray.getDimension(R.styleable.CYTextView_lineSpacingExtra, 15);
        int typeface = typedArray.getColor(R.styleable.CYTextView_typeface, 0);

        typedArray.recycle();


        //设置 CY TextView的宽度和行间距
        m_iTextWidth = width;
        LineSpace = linespace;


        // 构建paint对象
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(textcolor);
        mPaint.setTextSize(sp2px(context, textsize));
        mPaint.setTextAlign(Paint.Align.LEFT);
        Log.e("meeee", SPHelper.getTextFix(mcontext) + " ");

        switch (typeface) {
            case 0:
                mPaint.setTypeface(Typeface.DEFAULT);
                break;
            case 1:
                mPaint.setTypeface(Typeface.SANS_SERIF);
                break;
            case 2:
                mPaint.setTypeface(Typeface.SERIF);
                break;
            case 3:
                mPaint.setTypeface(Typeface.MONOSPACE);
                break;
            default:
                mPaint.setTypeface(Typeface.DEFAULT);
                break;
        }

    }

    private void setdensity(Context context) {
        WindowManager m = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        m.getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        Log.e("view", "像素密度" + density);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    public void setTextSize(float size) {
        int pxSize = sp2px(mcontext, size);
        Log.e("view", "pxsize" + pxSize);
        tsize = size;
        mPaint.setTextSize(pxSize);
//        invalidate();
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        Log.e("paint", (int) (spValue * fontScale + 0.5f) + "   size ");
        return (int) (spValue * fontScale + 0.5f);
    }

    public float GetTextsize() {
        Log.e("view", "return" + tsize);
        return tsize * density;
    }


    public int getRealSize() {
        float[] widths = new float[1];
        String srt = "的";
        mPaint.getTextWidths(srt, widths);
        return (int) Math.ceil(widths[0]);
    }

    public int textFix() {
        float[] widths1 = new float[1];
        String srt1 = "“";
        mPaint.getTextWidths(srt1, widths1);
        float[] widths2 = new float[1];
        String srt2 = "的";
        mPaint.getTextWidths(srt2, widths2);
        float[] widths3 = new float[1];
        String srt3 = " ";
        mPaint.getTextWidths(srt3, widths3);
        Log.e("mear", "“占" + (int) Math.ceil(widths1[0]));
        Log.e("mear", "的占" + (int) Math.ceil(widths2[0]));
        Log.e("mear", "空格占" + (int) Math.ceil(widths3[0]));
        return (int) ((Math.ceil(widths2[0]) - Math.ceil(widths1[0])) / Math.ceil(widths3[0]));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("qwe11", "ondraw" + string);
//        if (TextUtils.isEmpty(string)) {
//            return;
//        }
        char ch;
        int w = 0;
        int istart = 0;
        int m_iFontHeight;
        int m_iRealLine = 0;
        int x = ((int) (density * 20) + width - SPHelper.getCurTxtNums(mcontext) * getRealSize()) / 2;
        Log.e("paint", x + "=x " + SPHelper.getCurTxtNums(mcontext) + " " + getRealSize());
        int y = (int) -LineSpace;

        Vector m_String = new Vector();

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        y = y - (int) Math.ceil(fm.ascent - fm.top);
        m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;//计算字体高度（字体高度＋行间距）
       
        int nowwords = 0;
//        Log.e("viewnum text",SPHelper.getCurTxtNums(mcontext)+" ");
        int viewnum = SPHelper.getCurTxtNums(mcontext) + 1;
        for (int i = 0; i < string.length(); i++) {
            nowwords++;

            ch = string.charAt(i);
//            Log.e("textword", ch + " ");
            float[] widths = new float[1];
            String srt = String.valueOf(ch);
            mPaint.getTextWidths(srt, widths);

            if (ch == '\n') {
                nowwords = 0;
                m_iRealLine++;
                m_String.addElement(string.substring(istart, i));
                istart = i + 1;
                w = 0;
            } else if (nowwords == viewnum && (w + (int) (Math.ceil(widths[0]))) < m_iTextWidth) {
                m_iRealLine++;
                m_String.addElement(string.substring(istart, i));
                istart = i;
                i--;
                w = 0;
                nowwords = 0;
            } else {

                w += (int) (Math.ceil(widths[0]));

                if (w > m_iTextWidth) {
                    nowwords = 0;
                    m_iRealLine++;
                    m_String.addElement(string.substring(istart, i));
                    istart = i;
                    i--;
                    w = 0;
                } else {
                    if (i == (string.length() - 1)) {
                        nowwords = 0;
                        m_iRealLine++;
                        m_String.addElement(string.substring(istart, string.length()));
                    }
                }
            }
//            Log.e("textword", m_iRealLine + " ");
        }
        Rect indexBound = new Rect();
        mPaint.getTextBounds(string, 0, string.length(), indexBound);

        for (int i = 0, j = 0; i < m_iRealLine; i++, j++) {
            String text;
            if (SPHelper.getTextFix(mcontext) == 2) {
                text = ((String) (m_String.elementAt(i))).replaceAll("“", " “ ").replaceAll("”", " ” ");
            } else {
                text = (String) m_String.elementAt(i);
            }
//            Log.e("bug1111", text);
            canvas.drawText(text, x, y + m_iFontHeight * (j + 1), mPaint);
        }
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = measureHeight(heightMeasureSpec);
        int measuredWidth = measureWidth(widthMeasureSpec);
        this.setMeasuredDimension(measuredWidth, measuredHeight);
        this.setLayoutParams(new LinearLayout.LayoutParams(measuredWidth, measuredHeight));
        Log.e("onMeasure", "wide" + measuredWidth + "height" + measuredHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        // Default size if no limits are specified.
        initHeight();
        int result = m_iTextHeight;
//        if (specMode == MeasureSpec.AT_MOST) {
//            Log.e("onMeasure", "AT_MOST" );
//            // Calculate the ideal size of your
//            // control within this maximum size.
//            // If your control fills the available
//            // space return the outer bound.
//            result = specSize;
//        } else if (specMode == MeasureSpec.EXACTLY) {
//            Log.e("onMeasure", "AT_MOST" );
//            // If your control can fit within these bounds return that value.
//            result = specSize;
//        }
//        Log.e("tag", "result" + result);
        return result;
    }

    private void initHeight() {
        //设置 CY TextView的初始高度为0
        m_iTextHeight = 0;
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;
        //大概计算 CY TextView所需高度

        m_iTextHeight = (countLine()) * m_iFontHeight + 2;
        Log.e("tag", "height" + m_iTextHeight);
    }

    public int GetHeight() {
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;
        return m_iFontHeight;
    }

    public int GetHeight(int line) {
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;
        int a = (line) * m_iFontHeight + 2;
        return a;
    }

    public int countLine() {

        int line = 0;
        int istart = 0;
        int w = 0;
        int viewnum = SPHelper.getCurTxtNums(mcontext) + 1;
        int nowwords = 0;
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
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
                if (w > m_iTextWidth) {
                    nowwords = 0;
                    line++;
                    istart = i;
                    i--;
                    w = 0;
                } else {
                    if (i == (string.length() - 1)) {
                        nowwords = 0;
                        line++;
                    }
                }
            }
        }
        return line + 2;
    }

    public int countLine(String s) {

        int line = 0;
        int istart = 0;
        int viewnum = SPHelper.getCurTxtNums(mcontext) + 1;
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


        return line + 2;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // Default size if no limits are specified.
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            // Calculate the ideal size of your control
            // within this maximum size.
            // If your control fills the available space
            // return the outer bound.
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            // If your control can fit within these bounds return that value.
            result = specSize;
        }
        return result;
    }

    public void SetText(String text) {
        string = text;
        Log.e("textword", text);
//        initHeight();
//
//       int i= mPaint.breakText(text,true,width,null);
//        Log.e("tag",text+"  "+i);
        // requestLayout();
        // invalidate();
    }

    public int GetTextHeight() {
        return m_iTextHeight;
    }

    public int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int GetLineHeight() {
        return (int) (tsize + LineSpace);
    }
}