package com.syezon.reader.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.activity.BookDetailActivity;
import com.syezon.reader.adapter.SiftGridAdapter;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.NovelBean;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.Tools;
import com.syezon.reader.widget.NoScrollGridView;
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
 * 精选界面
 * Created by jin on 2016/9/26.
 */
public class SiftFragment extends Fragment {

    private static final int GETDATA_SUCCESS = 1;

    private TextView mTv_boy, mTv_girl, mTv_editor;
    private NoScrollGridView mGrid_boy, mGrid_gril, mGrid_editor;
    private SiftGridAdapter mBoyAdapter, mGrilAdapter, mEditorAdapter;
    private List<NovelBean> mBoyData, mGirlData, mEditorData;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETDATA_SUCCESS:
                    mTv_boy.setVisibility(View.VISIBLE);
                    mTv_girl.setVisibility(View.VISIBLE);
                    mTv_editor.setVisibility(View.VISIBLE);
                    mBoyAdapter.notifyDataSetChanged();
                    mGrilAdapter.notifyDataSetChanged();
                    mEditorAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sift, container, false);
        iniView(view);
        initData();
        return view;
    }

    private void initData() {
        mBoyData = new ArrayList<>();
        mGirlData = new ArrayList<>();
        mEditorData = new ArrayList<>();
        mBoyAdapter = new SiftGridAdapter(getActivity(), mBoyData);
        mGrid_boy.setAdapter(mBoyAdapter);
        mGrilAdapter = new SiftGridAdapter(getActivity(), mGirlData);
        mGrid_gril.setAdapter(mGrilAdapter);
        mEditorAdapter = new SiftGridAdapter(getActivity(), mEditorData);
        mGrid_editor.setAdapter(mEditorAdapter);
        mGrid_boy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //进详情页
                gotoDetails(mBoyData.get(position).getId());
            }
        });
        mGrid_gril.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoDetails(mGirlData.get(position).getId());
            }
        });

        mGrid_editor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoDetails(mEditorData.get(position).getId());
            }
        });
        getSiftData();
    }

    private void gotoDetails(int id) {
        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra("bookid", id);
        startActivity(intent);
    }

    private void iniView(View view) {
        mGrid_boy = (NoScrollGridView) view.findViewById(R.id.grid_boy);
        mGrid_gril = (NoScrollGridView) view.findViewById(R.id.grid_girl);
        mGrid_editor = (NoScrollGridView) view.findViewById(R.id.grid_editor);

        mTv_boy = (TextView) view.findViewById(R.id.tv_boy);
        mTv_girl = (TextView) view.findViewById(R.id.tv_girl);
        mTv_editor = (TextView) view.findViewById(R.id.tv_editor);
        mTv_boy.setVisibility(View.GONE);
        mTv_girl.setVisibility(View.GONE);
        mTv_editor.setVisibility(View.GONE);

        mGrid_boy.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGrid_gril.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGrid_editor.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    private ProgressDialog mDialog;

    private void getSiftData() {
        mDialog = Tools.showProgressDialog(getActivity());
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
        Log.e("TAG", "get sift list error:" + cid + " " + SPHelper.getUserToken(getActivity()));
        OkHttpUtils.postString().url(APIDefine.GET_SIFT_LIST)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "get sift list error:" + e);
                Tools.closeProgressDialog(mDialog);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("TAG", "get sift list data" + response);
                Tools.closeProgressDialog(mDialog);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        String baseUrl = root.getString("baseUrl");
                        JSONObject data = root.getJSONObject("data");
                        JSONArray boys = data.getJSONArray("boy");
                        JSONArray girls = data.getJSONArray("girl");
                        JSONArray recommend = data.getJSONArray("recommands");
                        for (int i = 0; i < boys.length(); i++) {
                            NovelBean bean = new NovelBean();
                            JSONObject value = boys.getJSONObject(i);
                            bean.setId(value.getInt("articleId"));
                            bean.setImg(baseUrl + value.getString("cover"));
                            bean.setName(value.getString("name"));
                            bean.setAuthor(value.getString("author"));
                            mBoyData.add(bean);
                        }
                        for (int i = 0; i < girls.length(); i++) {
                            NovelBean bean = new NovelBean();
                            JSONObject value = girls.getJSONObject(i);
                            bean.setId(value.getInt("articleId"));
                            bean.setImg(baseUrl + value.getString("cover"));
                            bean.setName(value.getString("name"));
                            bean.setAuthor(value.getString("author"));
                            mGirlData.add(bean);
                        }
                        for (int i = 0; i < recommend.length(); i++) {
                            NovelBean bean = new NovelBean();
                            JSONObject value = recommend.getJSONObject(i);
                            bean.setId(value.getInt("articleId"));
                            bean.setImg(baseUrl + value.getString("cover"));
                            bean.setName(value.getString("name"));
                            bean.setAuthor(value.getString("author"));
                            mEditorData.add(bean);
                        }
                        mHandler.sendEmptyMessage(GETDATA_SUCCESS);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("MineFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("MineFragment");
    }
}
