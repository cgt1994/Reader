package com.syezon.reader.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

/**
 * Created by xuzb on 10/22/14.
 */
public abstract class SlidingAdapter<T> {

    private View[] mViews;
    private int currentViewIndex;
    private SlidingLayout slidingLayout;

    public SlidingAdapter() {
        mViews = new View[3];
//
        currentViewIndex = 0;
    }


    public void setSlidingLayout(SlidingLayout slidingLayout) {
        this.slidingLayout = slidingLayout;
    }

    public View getUpdatedCurrentView() {
        //Log.e("TAG","get update cur view");
        View curView = mViews[currentViewIndex];
        if (curView == null) {
            curView = getView(null, getCurChapter(), getCurrent());
            mViews[currentViewIndex] = curView;
        } else {
            View updateView = getView(curView, getCurChapter(), getCurrent());
            if (curView != updateView) {
                curView = updateView;
                mViews[currentViewIndex] = updateView;
            }
        }
        return curView;
    }

    public View getCurrentView() {
        //Log.e("TAG","get cur view");
        View curView = mViews[currentViewIndex];
        if (curView == null) {
            curView = getView(null, getCurChapter(), getCurrent());
            mViews[currentViewIndex] = curView;
        }
        return curView;
    }

    public View getView(int index) {
        return mViews[(index + 3) % 3];
    }

    public int getIndex() {
        return currentViewIndex;
    }

    ;

    private void setView(final int index, View view) {
        mViews[(index + 3) % 3] = view;
//        mViews[(index + 3) % 3].setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.e("onclick","  第 "+index);
//                if (index==2)
//                return true;
//                else {
//                    return false;
//                }
//            }
//
//
//        });
    }

    public View getUpdatedNextView() {
        //  Log.e("TAG","get update next view");
        View nextView = getView(currentViewIndex + 1);
        boolean hasnext = hasNext();
        if (nextView == null && hasnext) {
            nextView = getView(null, getNextChapter(), getNext());
            setView(currentViewIndex + 1, nextView);
        } else if (hasnext) {
            View updatedView = getView(nextView, getNextChapter(), getNext());
            if (updatedView != nextView) {
                nextView = updatedView;
                setView(currentViewIndex + 1, nextView);
            }
        }
        return nextView;
    }

    public View getNextView() {
        //Log.e("TAG","get next view");
        View nextView = getView(currentViewIndex + 1);
        if (nextView == null && hasNext()) {
            nextView = getView(null, getNextChapter(), getNext());
            setView(currentViewIndex + 1, nextView);
        }
        return nextView;
    }

    public View getUpdatedPreviousView() {
        // Log.e("TAG","get update pre view");
        View prevView = getView(currentViewIndex - 1);
            boolean hasprev = hasPrevious();
        Log.e("q 2"," ");
        if (prevView == null && hasprev) {
            prevView = getView(null, getPreChapter(), getPrevious());
            setView(currentViewIndex - 1, prevView);
        } else if (hasprev) {
            View updatedView = getView(prevView, getPreChapter(), getPrevious());
            if (updatedView != prevView) {
                prevView = updatedView;
                setView(currentViewIndex - 1, prevView);
            }
        }
        return prevView;
    }

    public void setPreviousView(View view) {
        setView(currentViewIndex - 1, view);

    }

    public void setNextView(View view) {
        setView(currentViewIndex + 1, view);
      
    }

    public void setCurrentView(View view) {
        setView(currentViewIndex, view);

    }

    public View getPreviousView() {
        // Log.e("TAG","get pre view");
        View prevView = getView(currentViewIndex - 1);
        Log.e("q 3"," ");
        if (prevView == null && hasPrevious()) {
            prevView = getView(null, getPreChapter(), getPrevious());
            setView(currentViewIndex - 1, prevView);
        }
        return prevView;
    }

    public void moveToNext() {
        // Move to next elemen
           Log.e("get1","moveToNext被调用");
        computeNext();

        // Increase view index
        currentViewIndex = (currentViewIndex + 1) % 3;
    }

    public void moveToPrevious() {
        // Move to next elemento
        Log.e("get1 ","movetopre被调用");
        computePrevious();

        // Increase view index
        currentViewIndex = (currentViewIndex + 2) % 3;
    }

    public abstract View getView(View contentView, T t1, T t2);

    public abstract T getCurrent();

    public abstract T getNext();

    public abstract T getPrevious();

    public abstract T getCurChapter();

    public abstract T getNextChapter();

    public abstract T getPreChapter();

    public abstract boolean hasNext();

    public abstract boolean hasPrevious();

    protected abstract void computeNext();

    protected abstract void computePrevious();

    public Bundle saveState() {
        return null;
    }

    public void restoreState(Parcelable parcelable, ClassLoader loader) {
        currentViewIndex = 0;
        if (mViews != null) {
            mViews[0] = null;
            mViews[1] = null;
            mViews[2] = null;
        }
    }


    public void notifyDataSetChanged() {
        if (slidingLayout != null) {
            Log.e("TAG", "reset view");
            slidingLayout.resetFromAdapter();
            slidingLayout.postInvalidate();
        }
    }
}
