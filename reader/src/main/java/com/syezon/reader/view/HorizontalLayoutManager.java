package com.syezon.reader.view;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Administrator on 2017/3/31.
 */

public class HorizontalLayoutManager extends RecyclerView.LayoutManager {
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return null;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() < 0 || state.isPreLayout()) {
            return;
        }

        super.onLayoutChildren(recycler, state);
        detachAndScrapAttachedViews(recycler);
    }
}
