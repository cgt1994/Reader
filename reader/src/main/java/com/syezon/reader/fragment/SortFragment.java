package com.syezon.reader.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.syezon.reader.R;
import com.syezon.reader.activity.BookDetailActivity;
import com.syezon.reader.adapter.NovelListAdapter;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.NovelBean;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.Tools;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 排行界面
 * Created by jin on 2016/9/26.
 */
public class SortFragment extends Fragment implements OnItemClickListener {

    private LRecyclerView mRecycle_sort;

    private List<NovelBean> mData = new ArrayList<>();
    private NovelListAdapter mDataAdapter;
    private LRecyclerViewAdapter mAdapter;

    private int mPage = 1;
    private boolean isRefresh = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRecycle_sort = (LRecyclerView) view.findViewById(R.id.recycle_novel);
        mRecycle_sort.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDataAdapter = new NovelListAdapter(getActivity(), mData);
        mAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecycle_sort.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mRecycle_sort.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecycle_sort.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        mRecycle_sort.setLScrollListener(new LRecyclerView.LScrollListener() {
            @Override
            public void onRefresh() {
                //下拉刷新
                RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.Normal);
                mPage = 1;
                isRefresh = true;
                getSortNovelList(mPage);
            }

            @Override
            public void onScrollUp() {

            }

            @Override
            public void onScrollDown() {

            }

            @Override
            public void onBottom() {
                isRefresh = false;
                //上拉加载
                LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecycle_sort);
                if (state == LoadingFooter.State.Loading) {
                    return;
                }
                mPage++;
                RecyclerViewStateUtils.setFooterViewState(getActivity(), mRecycle_sort, 10, LoadingFooter.State.Loading, null);
                getSortNovelList(mPage);
            }

            @Override
            public void onScrolled(int distanceX, int distanceY) {

            }

            @Override
            public void onScrollStateChanged(int state) {

            }
        });
        isRefresh = true;
        getSortNovelList(1);
    }

    //item点击
    @Override
    public void onItemClick(View view, int position) {
        //进详情页
        Log.e("qwer", position + " " + mData.get(position).getName() + mData.get(position).getId());
        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra("bookid", mData.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    private ProgressDialog mDialog;

    //获取小说数据列表
    private void getSortNovelList(int page) {
        if (isRefresh) {
            mDialog = Tools.showProgressDialog(getActivity());
        }
        String cid = "";
        if (SConfig.IMSI != null) {
            cid = SConfig.IMSI;
        } else if (SConfig.IMEI != null) {
            cid = SConfig.IMEI;
        } else {
            cid = SConfig.MAC;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("token", SPHelper.getUserToken(getActivity()));
        params.put("page", page);

        OkHttpUtils.postString().url(APIDefine.GET_SORT_LIST)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "get sort novel list error:" + e);
                if (isRefresh) {
                    Tools.closeProgressDialog(mDialog);
                    isRefresh = false;
                    mRecycle_sort.refreshComplete();
                }
                RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.Normal);
            }

            @Override
            public void onResponse(String response, int id) {
//                Log.e("TAG", "get sort novel list:" + response);
                if (isRefresh) {
                    Tools.closeProgressDialog(mDialog);
                    mRecycle_sort.refreshComplete();
                }

                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        if (isRefresh) {
                            isRefresh = false;
                            mData.clear();
                        }
                        JSONArray data = root.getJSONArray("data");
                        if (data.length() > 0) {
                            String baseUrl = root.getString("baseUrl");
                            for (int i = 0; i < data.length(); i++) {
                                NovelBean bean = new NovelBean();
                                JSONObject value = data.getJSONObject(i);
                                bean.setId(value.getInt("articleId"));
                                bean.setImg(baseUrl + value.getString("cover"));
                                bean.setName(value.getString("name"));
                                bean.setAuthor(value.getString("author"));
                                bean.setIntro(value.getString("intro"));
                                bean.setChapter(value.getString("lastChapter"));
                                mData.add(bean);
                            }
                            RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.Normal);
                        } else {
                            RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.TheEnd);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SortFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SortFragment");
    }

}
