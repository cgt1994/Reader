package com.syezon.reader.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syezon.reader.R;
import com.syezon.reader.activity.NovelListActivity;
import com.syezon.reader.adapter.NovelClassAdapter;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.NovelClassBean;
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
 * 分类界面
 * Created by jin on 2016/9/26.
 */
public class ClassFragment extends Fragment implements NovelClassAdapter.ItemClickListener {

    private RecyclerView mRecycle_class;

    private List<NovelClassBean> mNovelClassData;
    private NovelClassAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class, container, false);
        initData();
        initView(view);
        return view;
    }

    private void initData() {
        mNovelClassData = new ArrayList<>();
        getNovelClass();
    }

    private void initView(View view) {
        mRecycle_class = (RecyclerView) view.findViewById(R.id.recycle_class);
        mRecycle_class.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new NovelClassAdapter(getActivity(), mNovelClassData);
        mRecycle_class.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    //item点击
    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getActivity(), NovelListActivity.class);
        intent.putExtra("isSearch", false);
        intent.putExtra("class", mNovelClassData.get(position).getName());
        intent.putExtra("id", mNovelClassData.get(position).getId());
        startActivity(intent);
    }

    private ProgressDialog mDialog;

    private void getNovelClass() {
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

        OkHttpUtils.postString().url(APIDefine.GET_CLASS_LIST)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "get novel class list error:" + e);
                Tools.closeProgressDialog(mDialog);
            }

            @Override
            public void onResponse(String response, int id) {
//                Log.e("TAG", "get novel class list:" + response);
                Tools.closeProgressDialog(mDialog);
                Log.e("yyy", response);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        mNovelClassData.clear();
                        String baseUrl = root.getString("baseUrl");
                        JSONArray data = root.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            NovelClassBean bean = new NovelClassBean();
                            JSONObject value = data.getJSONObject(i);

                            bean.setId(value.getInt("id"));
                            bean.setImg(baseUrl + value.getString("cover"));
                            bean.setName(value.getString("name"));
                            bean.setCount(value.getInt("count"));
                            mNovelClassData.add(bean);
                        }
                        Log.e("yyy", mNovelClassData.size() + " ");

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
        MobclickAgent.onPageStart("ClassFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ClassFragment");
    }


}
