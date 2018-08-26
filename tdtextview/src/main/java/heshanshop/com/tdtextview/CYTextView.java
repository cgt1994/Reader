package heshanshop.com.tdtextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


;


/**
 * Created by Icy on 2016/11/11.
 */

public class CYTextView extends android.support.v7.widget.AppCompatTextView {
    public int mHeight; //文本的高度
    public int mWidth;//文本的宽度

    private Paint mPaint = null;
    private String mString = "";
    private float LineSpace = 0;//行间距
    private static int width;//屏幕宽度
    private Context mcontext;
    private float tsize;
    private float density;
    //  当前字体的宽高
    private int mTextWidth;
    private int mTextHeight;
    //    打印机模式（逐字打印）
    private boolean writeMode = false;
    //    逐行打印
    private boolean readMode = false;

    private int type = 0;
    private static final int WRITE_MODE = 1;
    private static final int READ_MODE = 2;

    //    渲染的画布
    private Bitmap printBitmap;
    private Canvas printCanvas;

    private int lineNumbers;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.e("messs", (mTextCurrentX + mTextWidth + mTextSpace > mWidth) + " " + mTextCurrentY + " ");
            if (printCanvas == null) {
                return;
            }
            switch (type) {
                case WRITE_MODE:
                    if (mPosition == 0) {
                        printCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    }
                    if (mTextCurrentX + mTextWidth + mTextSpace > mWidth) {
                        Log.e("amaz 1", mTextCurrentY + " " + mTextHeight);
                        mTextCurrentY += mTextHeight;
                        Log.e("amaz 2", mTextCurrentY + " " + mTextHeight);
                        mTextCurrentX = 0;
                    }
                    printCanvas.drawText(String.valueOf(mString.charAt(mPosition)), mTextCurrentX, mTextCurrentY, mPaint);

                    mPosition++;
                    mTextCurrentX += mTextWidth + mTextSpace;

                    invalidate();
                    if (mPosition >= mString.length() - 1) {
                        mHandler.removeCallbacksAndMessages(null);
                    } else {
                        mHandler.sendEmptyMessageDelayed(0, 300);
                    }
                    break;
                case READ_MODE:
                    if (mCurrentLines == 1) {
                        printCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    }
                    int lines = (mString.length() / lineNumbers) + 1;

                    Log.e("lines", lines + " " + mCurrentLines);
//                for (int i = 0; i < lines; i++) {
                    int lastP = lineNumbers * mCurrentLines;
                    if (lastP > mString.length()) {
                        lastP = mString.length();
                    }
                    mTextCurrentX = 0;
                    for (int i = lineNumbers * (mCurrentLines - 1); i < lastP; i++) {
                        Log.e("dafa", mPosition + " ");
                        printCanvas.drawText(String.valueOf(mString.charAt(mPosition)), mTextCurrentX, mCurrentLines * mTextHeight, mPaint);
                        mTextCurrentX += (mTextWidth + mTextSpace);
                        mPosition++;
                    }
//                printCanvas.drawText(mString.substring((mCurrentLines - 1) * lineNumbers, lastP), 0, (mCurrentLines) * mTextHeight, mPaint);

//                }
                    invalidate();
                    if (mCurrentLines >= lines) {
                        mHandler.removeCallbacksAndMessages(null);
                    } else {
                        mHandler.sendEmptyMessageDelayed(0, 3000);
                    }
                    mCurrentLines++;
                    break;
            }


        }
    };


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


        int textcolor = typedArray.getColor(R.styleable.CYTextView_cy_textColor, -1442840576);
        float linespace = typedArray.getDimension(R.styleable.CYTextView_lineSpacingExtra, 15);
        int typeface = typedArray.getColor(R.styleable.CYTextView_typeface, 0);
        tsize = typedArray.getDimension(R.styleable.CYTextView_cy_textSize, 0);

        typedArray.recycle();


        //设置 CY TextView的宽度和行间距

        LineSpace = linespace;


        // 构建paint对象
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(textcolor);
        mPaint.setTextSize(tsize);
        mPaint.setTextAlign(Paint.Align.LEFT);

        initPaint();
        initPrintBitmap();

        Rect rect = new Rect();
        mPaint.getTextBounds("你", 0, 1, rect);
        mTextWidth = rect.width();
        mTextHeight = rect.height();
        mTextCurrentY = mTextHeight;
        Log.e("tet", mTextWidth + " " + rect.width() + " " + rect.height());

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
        Log.e("view", "color" + color);
        invalidate();
    }

    @Override
    public void setTextSize(float size) {
        int pxSize = sp2px(mcontext, size);
        Log.e("view", "pxsize" + pxSize);
        tsize = size;
        mPaint.setTextSize(pxSize);
        invalidate();
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

    private int mPosition = 0;
    private int mCurrentLines = 1;

    private int mTextCurrentX;
    private int mTextCurrentY;

    private int mTextSpace = 0;

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        Log.e("ondd", "ondraw");
        if (TextUtils.isEmpty(mString)) {
            return;
        }
        if (writeMode || readMode) {
//            if (mPosition < mString.length() - 1) {

            canvas.drawBitmap(printBitmap, 0, 0, mBitPaint);
//            }
        } else {
            reset();
            Log.e("qwe11", "ondraw" + mString);

            for (int i = 0; i < mString.length(); i++) {
                if (mTextCurrentX + mTextWidth + mTextSpace > mWidth) {
                    mTextCurrentY += mTextHeight;
                    mTextCurrentX = 0;
                }
                canvas.drawText(String.valueOf(mString.charAt(i)), mTextCurrentX, mTextCurrentY, mPaint);
                mTextCurrentX += mTextWidth + mTextSpace;
            }
        }
//
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        Log.e("onMeasure", widthMode + " " + widthSize + " " + heightMode + " " + heightSize);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = widthSize;
            if (TextUtils.isEmpty(mString)) {

            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = heightSize;
        }
        mHeight = height;
        mWidth = width;
        lineNumbers = mWidth / (mTextWidth + mTextSpace);
        setMeasuredDimension(width, height);

    }


//    private void initHeight() {
//        //设置 CY TextView的初始高度为0
//        m_iTextHeight = 0;
//        Paint.FontMetrics fm = mPaint.getFontMetrics();
//        int m_iFontHeight = (int) Math.ceil(fm.descent - fm.top) + (int) LineSpace;
//        //大概计算 CY TextView所需高度
//
//        m_iTextHeight = (countLine()) * m_iFontHeight + 2;
//        Log.e("tag", "height" + m_iTextHeight);
//    }


    public void setText(String text) {
        mString = text;
        invalidate();
        Log.e("textword", text);
//        initHeight();
//
//       int i= mPaint.breakText(text,true,width,null);
//        Log.e("tag",text+"  "+i);
        // requestLayout();
        // invalidate();
    }


    public int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    private Paint mBitPaint;

    public void startReadMode() {
        reset();

        type = READ_MODE;
        mHandler.sendEmptyMessageDelayed(0, 1000);
        invalidate();
    }

    public void startWriteMode() {
        reset();
        type = WRITE_MODE;

        mHandler.sendEmptyMessageDelayed(0, 1000);
        invalidate();
    }

    private void initPrintBitmap() {
        if (printBitmap == null) {
            printBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            printCanvas = new Canvas(printBitmap);
        }
        Log.e("ccv", mTextCurrentX + " " + mTextCurrentY);
        printCanvas.drawText("我开始拉", mTextCurrentX, mTextCurrentY, mPaint);
    }

    private void initPaint() {
        if (mBitPaint == null) {
            mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBitPaint.setFilterBitmap(true);
            mBitPaint.setDither(true);
        }
    }

    private void reset() {

        mTextCurrentX = 0;
        mTextCurrentY = mTextHeight;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
}