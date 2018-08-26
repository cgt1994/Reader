package com.syezon.reader.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.syezon.reader.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 书城界面
 * Created by jin on 2016/9/13.
 */
public class BookStoreFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout mRl_class, mRl_sift, mRl_sort;
    private TextView mTv_class, mTv_sift, mTv_sort;
    private View mView_class, mView_sift, mView_sort;

    private FragmentManager mManager;
    private FragmentTransaction mTransition;
    private Fragment mClassFragment, mSiftFragmnet, mSortFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookstore, container, false);
        initView(view);
        initFragment();
        return view;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("BookStoreFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("BookStoreFragment");
    }

    private void initFragment() {
        mClassFragment = new ClassFragment();
        mSiftFragmnet = new SiftFragment();
        mSortFragment = new SortFragment();
        mManager = getFragmentManager();
        mManager.beginTransaction().replace(R.id.frame_content, mClassFragment).commit();
    }

    private void initView(View view) {
        mRl_class = (RelativeLayout) view.findViewById(R.id.rl_class);
        mTv_class = (TextView) view.findViewById(R.id.tv_class);
        mView_class = view.findViewById(R.id.view_class);
        mRl_sift = (RelativeLayout) view.findViewById(R.id.rl_sift);
        mTv_sift = (TextView) view.findViewById(R.id.tv_sift);
        mView_sift = view.findViewById(R.id.view_sift);
        mRl_sort = (RelativeLayout) view.findViewById(R.id.rl_sort);
        mTv_sort = (TextView) view.findViewById(R.id.tv_sort);
        mView_sort = view.findViewById(R.id.view_sort);

        mRl_class.setOnClickListener(this);
        mRl_sift.setOnClickListener(this);
        mRl_sort.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        resetSelect();
        mTransition = mManager.beginTransaction();
        switch (v.getId()) {
            case R.id.rl_class:
                mTv_class.setTextColor(getActivity().getResources().getColor(R.color.title_bg));
                mView_class.setBackgroundColor(getActivity().getResources().getColor(R.color.title_bg));
                mTransition.replace(R.id.frame_content, mClassFragment);

                break;
            case R.id.rl_sift:
                mTv_sift.setTextColor(getActivity().getResources().getColor(R.color.title_bg));
                mView_sift.setBackgroundColor(getActivity().getResources().getColor(R.color.title_bg));
                mTransition.replace(R.id.frame_content, mSiftFragmnet);
                break;
            case R.id.rl_sort:
                mTv_sort.setTextColor(getActivity().getResources().getColor(R.color.title_bg));
                mView_sort.setBackgroundColor(getActivity().getResources().getColor(R.color.title_bg));
                mTransition.replace(R.id.frame_content, mSortFragment);
                break;
        }
        mTransition.commit();
    }

    private void resetSelect() {
        mTv_class.setTextColor(getActivity().getResources().getColor(R.color.foot_unchecked));
        mView_class.setBackgroundColor(getActivity().getResources().getColor(R.color.divide));
        mTv_sift.setTextColor(getActivity().getResources().getColor(R.color.foot_unchecked));
        mView_sift.setBackgroundColor(getActivity().getResources().getColor(R.color.divide));
        mTv_sort.setTextColor(getActivity().getResources().getColor(R.color.foot_unchecked));
        mView_sort.setBackgroundColor(getActivity().getResources().getColor(R.color.divide));
    }
}
