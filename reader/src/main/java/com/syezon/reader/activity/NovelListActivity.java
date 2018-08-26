package com.syezon.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.syezon.reader.R;
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
 * 小说列表界面
 * Created by jin on 2016/9/26.
 */
public class NovelListActivity extends Activity implements OnItemClickListener, View.OnClickListener {

    private LRecyclerView mRecycle_sort;
    private ImageView mIv_back;
    private TextView mTv_title;
    private TextView mTv_no_result;

    private List<NovelBean> mData = new ArrayList<>();
    private NovelListAdapter mDataAdapter;
    private LRecyclerViewAdapter mAdapter;

    private int mPage = 1;
    private boolean isRefresh = true;

    private int mClassID;
    private String mClassName;
    private String mNovelName;
    private boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_novel_list);
        isSearch = getIntent().getBooleanExtra("isSearch", false);
        if (isSearch) {
            mNovelName = getIntent().getStringExtra("search");
        } else {
            mClassName = getIntent().getStringExtra("class");
            mClassID = getIntent().getIntExtra("id", 0);
        }

        mTv_title = (TextView) findViewById(R.id.title_center);
        mIv_back = (ImageView) findViewById(R.id.title_left);
        mIv_back.setVisibility(View.VISIBLE);
        mIv_back.setOnClickListener(this);
        mTv_no_result = (TextView) findViewById(R.id.tv_no_data);
        mRecycle_sort = (LRecyclerView) findViewById(R.id.recycle_novel);
        mRecycle_sort.setLayoutManager(new LinearLayoutManager(this));
        mDataAdapter = new NovelListAdapter(this, mData);
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
                if (isSearch) {
                    getSearchList();
                } else {
                    getNovelList(mPage);
                }
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
                ++mPage;
                if (isSearch) {
                    RecyclerViewStateUtils.setFooterViewState(NovelListActivity.this, mRecycle_sort, 5, LoadingFooter.State.TheEnd, null);
                } else {
                    RecyclerViewStateUtils.setFooterViewState(NovelListActivity.this, mRecycle_sort, 5, LoadingFooter.State.Loading, null);
                    getNovelList(mPage);
                }
            }

            @Override
            public void onScrolled(int distanceX, int distanceY) {

            }

            @Override
            public void onScrollStateChanged(int state) {

            }
        });
        if (isSearch) {
            mTv_title.setText(getString(R.string.title_search));
            mTv_no_result.setText(getString(R.string.search_no_result));
            getSearchList();
        } else {
            mTv_title.setText(mClassName);
            mTv_no_result.setText(getString(R.string.novel_no_result));
            getNovelList(1);
        }

    }

    @Override
    public void onClick(View v) {
        finish();
    }

    //item点击
    @Override
    public void onItemClick(View view, int position) {
        //进详情页

        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookid", mData.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }


    private ProgressDialog mDialog;

    //获取搜索数据列表
    private void getSearchList() {
        if (isRefresh) {
            mDialog = Tools.showProgressDialog(this);
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
        params.put("token", SPHelper.getUserToken(this));
        params.put("key", mNovelName);
        OkHttpUtils.postString().url(APIDefine.SEARCH_NOVEL)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                if (isRefresh) {
                    isRefresh = false;
                    mRecycle_sort.refreshComplete();
                }
                mTv_no_result.setVisibility(View.VISIBLE);
                mRecycle_sort.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                // Log.e("TAG", "search response:" + response);
                if (isRefresh) {
                    mRecycle_sort.refreshComplete();
                    Tools.closeProgressDialog(mDialog);
                }
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        if (isRefresh) {
                            mData.clear();
                            isRefresh = false;
                        }
                        String baseUrl = root.getString("baseUrl");
                        JSONArray data = root.getJSONArray("data");
                        if (data.length() > 0) {
                            for (int i = 0; i < data.length(); i++) {
                                NovelBean bean = new NovelBean();
                                JSONObject value = data.getJSONObject(i);
                                bean.setId(value.getInt("articleId"));
                                bean.setImg(baseUrl + value.getString("cover"));
                                bean.setName(value.getString("name"));
                                bean.setAuthor(value.getString("author"));
                                mData.add(bean);
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mTv_no_result.setVisibility(View.VISIBLE);
                            mRecycle_sort.setVisibility(View.GONE);
                        }
                    } else {
                        mTv_no_result.setVisibility(View.VISIBLE);
                        mRecycle_sort.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //获取小说数据列表
    private void getNovelList(int page) {
        if (isRefresh) {
            mDialog = Tools.showProgressDialog(this);
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
        params.put("token", SPHelper.getUserToken(this));
        params.put("id", mClassID);
        params.put("page", page);
        OkHttpUtils.postString().url(APIDefine.GET_ASSIGN_CLASS).content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Tools.closeProgressDialog(mDialog);
                Log.e("TAG", "error:" + e);
                if (isRefresh) {
                    isRefresh = false;
                    mRecycle_sort.refreshComplete();
                }
                RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.Normal);

                mTv_no_result.setVisibility(View.VISIBLE);
                mRecycle_sort.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                // Log.e("TAG", "get novel list:" + response);
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
                                if (value.has("lastChapter")) {
                                    bean.setChapter(value.getString("lastChapter"));
                                }

                                mData.add(bean);
                            }
                            RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.Normal);
                        } else {
                            if (mData.size() > 0) {
                                RecyclerViewStateUtils.setFooterViewState(mRecycle_sort, LoadingFooter.State.TheEnd);
                            } else {
                                //一开始就没有数据
                                mTv_no_result.setVisibility(View.VISIBLE);
                                mRecycle_sort.setVisibility(View.GONE);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mTv_no_result.setVisibility(View.VISIBLE);
                        mRecycle_sort.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("NovelListActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("NovelListActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

}
