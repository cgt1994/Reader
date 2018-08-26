package com.syezon.reader.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.adapter.HistorySearchAdapter;
import com.syezon.reader.db.HistoryDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.HistorySearchBean;
import com.syezon.reader.utils.Tools;
import com.syezon.reader.widget.HotFlowLayout;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * 搜索界面
 * Created by jin on 2016/9/26.
 */
public class SearchActivity extends Activity implements View.OnClickListener, HistorySearchAdapter.IClickListener, HotFlowLayout.IChildClickListener {

    private EditText mEt_search;
    private TextView mTv_sure, mTv_hot_tip;
    private RecyclerView mRecycle_history;
    private HotFlowLayout mHotFlowLayout;

    private List<String> mHotData;

    private List<HistorySearchBean> mHistoryData;
    private HistoryDBHelper mHistoryHelper;
    private HistorySearchAdapter mHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_search);
        initView();
        initData();
        initSearchListener();
        getHotSearch();

    }

    //点击输入法的确定按钮
    private void initSearchListener() {
        mEt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String search = v.getText().toString();
                    if (search != null && !"".equals(search)) {
                        add2History(search);
                        gotoSearch(search);
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void initData() {
        mHotData = new ArrayList<>();
        mHotFlowLayout.setOnChildClickListener(this);

        mHistoryHelper = new HistoryDBHelper(this);
        mHistoryData = mHistoryHelper.getHistoryList();
        mRecycle_history.setLayoutManager(new LinearLayoutManager(this));
        mHistoryAdapter = new HistorySearchAdapter(this, mHistoryData);
        mHistoryAdapter.setOnClickListenr(this);
        mRecycle_history.setAdapter(mHistoryAdapter);
    }

    private void initView() {
        mEt_search = (EditText) findViewById(R.id.et_search);
        mTv_sure = (TextView) findViewById(R.id.tv_sure_search);
        mTv_hot_tip = (TextView) findViewById(R.id.tv_hot_search_tip);
        mRecycle_history = (RecyclerView) findViewById(R.id.recycle_history);
        mHotFlowLayout = (HotFlowLayout) findViewById(R.id.hotFlowLayout);

        mTv_sure.setOnClickListener(this);
        mEt_search.addTextChangedListener(watcher);
    }

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

            if (s.toString().length() != 0) {
                mTv_sure.setText("确定");
            } else {
                mTv_sure.setText("取消");
            }
        }
    };

    //设置热门标签
    private void fillHotData() {
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 10;
        lp.rightMargin = 10;
        lp.topMargin = 5;
        lp.bottomMargin = 5;
        for (int i = 0; i < mHotData.size(); i++) {
            TextView view = new TextView(this);
            view.setText(mHotData.get(i));
            view.setTextColor(Color.BLACK);
            view.setPadding(8, 8, 8, 8);
            view.setTextSize(16);
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.hot_search_bg));
            mHotFlowLayout.addView(view, lp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sure_search:
                String search = mEt_search.getText().toString();
                if (search != null && !"".equals(search)) {
                    MobclickAgent.onEvent(SearchActivity.this, "search", search);
                    add2History(search);
                    gotoSearch(search);
                } else {
                    finish();
                }

                break;
        }
    }

    //跳转
    private void gotoSearch(String search) {
        Intent intent = new Intent(SearchActivity.this, NovelListActivity.class);
        intent.putExtra("isSearch", true);
        intent.putExtra("search", search);
        startActivity(intent);
    }

    //热门标签点击
    @Override
    public void childClick(int position) {
        mEt_search.setText(mHotData.get(position));
        mEt_search.setSelection(mEt_search.getText().length());
        add2History(mHotData.get(position));
        gotoSearch(mHotData.get(position));
    }

    //历史记录的点击
    @Override
    public void onItemClick(int position) {
        mEt_search.setText(mHistoryData.get(position).getSearchContent());
        mEt_search.setSelection(mEt_search.getText().length());
        gotoSearch(mHistoryData.get(position).getSearchContent());
        //修改排序
        HistorySearchBean bean = mHistoryData.get(position);
        bean.setSearchTime(System.currentTimeMillis() + "");
        mHistoryHelper.updateHistory(bean);
        mHistoryData.clear();
        mHistoryData.addAll(mHistoryHelper.getHistoryList());
        mHistoryAdapter.notifyDataSetChanged();

    }

    //历史记录删除
    @Override
    public void onItemDeleteClick(int position) {
        mHistoryHelper.deleteHistory(mHistoryData.get(position).getId());
        mHistoryData.remove(position);
        mHistoryAdapter.notifyDataSetChanged();
    }

    //添加到历史记录中
    private void add2History(String content) {
        HistorySearchBean bean = new HistorySearchBean();
        bean.setSearchContent(content);
        bean.setSearchTime(System.currentTimeMillis() + "");
        mHistoryHelper.addHistory(bean);
        mHistoryData.clear();
        mHistoryData.addAll(mHistoryHelper.getHistoryList());
        mHistoryAdapter.notifyDataSetChanged();
    }

    //获取热门搜索列表
    private void getHotSearch() {
        OkHttpUtils.get().url(APIDefine.HOT_SEARCH).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                mTv_hot_tip.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", "response:" + response);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        JSONArray data = root.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            mHotData.add(data.getString(i));
                        }
                        fillHotData();
                    } else {
                        mTv_hot_tip.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SearchActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SearchActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

}
