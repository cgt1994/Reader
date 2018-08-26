package com.syezon.reader.widget.slider;

import android.view.MotionEvent;

import com.syezon.reader.widget.SlidingAdapter;
import com.syezon.reader.widget.SlidingLayout;

/**
 * Created by xuzb on 1/16/15.
 */
public interface Slider {
    public void init(SlidingLayout slidingLayout);

    public void resetFromAdapter(SlidingAdapter adapter);

    public boolean onTouchEvent(MotionEvent event);

    public void computeScroll();

    public void slideNext();

    public void slidePrevious();

}
