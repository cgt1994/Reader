package com.syezon.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.github.jdsjlzx.recyclerview.ProgressStyle;
import com.github.jdsjlzx.util.RecyclerViewStateUtils;
import com.github.jdsjlzx.view.LoadingFooter;
import com.syezon.reader.R;
import com.syezon.reader.adapter.ChapterListAdapter;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.utils.BookFactory;
import com.syezon.reader.utils.CacheUtils;
import com.syezon.reader.utils.InfoUtils;
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
 * 书籍目录界面
 * Created by jin on 2016/9/27.
 */
public class BookChapterListActivity extends Activity implements View.OnClickListener, ChapterListAdapter.IItemClickListener {

    private TextView mTv_title;
    private ImageView mIv_back;
    private LRecyclerView mRecycle_chapter;
    private LinearLayoutManager mLL = null;
    private ChapterListAdapter mDataAdapter;
    private LRecyclerViewAdapter mAdapter;
    private List<ChapterBean> mData;
    private int mPage = 1;
    private boolean isRefresh;
    private BookFactory mBookFactory;
    private BookCaseBean mBookCaseBean;
    private boolean hasReceiver = false;
    private LinearLayout ll_jump;
    private TextView tv_go;
    private EditText et_chapter;

    //缓存完成后刷新书籍刷新书籍
    public class RefreshDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("cacheOne", false) && !hasReceiver) {
                hasReceiver = true;
                Log.e("TAG", "receive broadcast in chapterListActivity");
                //在跳转进页面中
                Intent intentRead = new Intent(BookChapterListActivity.this, ReadActivity.class);
                intentRead.putExtra("bookInfo", mBookCaseBean);
                intentRead.putExtra("isNetBook", true);
                startActivity(intentRead);
            }
        }
    }

    //    private ProgressDialog mpd;
    private RefreshDataReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_chapter_list);
        mBookCaseBean = (BookCaseBean) getIntent().getSerializableExtra("bookInfo");
        mBookFactory = new BookFactory(this, mBookCaseBean.getBook_name(), null);

        initView();
        initRecycleView();
        initReceiver();

    }

    public void onResume() {
        super.onResume();
//        if (mDialog != null) {
//            Tools.closeProgressDialog(mDialog);
//            mDialog = null;
//        }
        MobclickAgent.onPageStart("BookChapterListActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("BookChapterListActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    private void initReceiver() {
        mReceiver = new RefreshDataReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("cacheComplete");
        registerReceiver(mReceiver, filter);
    }

    private void initRecycleView() {
        mData = new ArrayList<>();
        mRecycle_chapter.setLayoutManager(new LinearLayoutManager(this));

        mDataAdapter = new ChapterListAdapter(this, mData);
        mDataAdapter.setOnItemClickListener(this);
        mAdapter = new LRecyclerViewAdapter(mDataAdapter);
        mRecycle_chapter.setAdapter(mAdapter);
        mRecycle_chapter.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        mRecycle_chapter.setArrowImageView(R.drawable.ic_pulltorefresh_arrow);
        mRecycle_chapter.setLScrollListener(new LRecyclerView.LScrollListener() {
            @Override
            public void onRefresh() {
                RecyclerViewStateUtils.setFooterViewState(mRecycle_chapter, LoadingFooter.State.Normal);
                isRefresh = true;
                mPage = 1;
                getChapterList(mPage);
            }

            @Override
            public void onScrollUp() {

            }

            @Override
            public void onScrollDown() {

            }

            @Override
            public void onBottom() {
//                isRefresh = false;
//                //上拉加载
//                LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mRecycle_chapter);
//                if (state == LoadingFooter.State.Loading) {
//                    return;
//                }
//                mPage++;
//                RecyclerViewStateUtils.setFooterViewState(BookChapterListActivity.this, mRecycle_chapter, 10, LoadingFooter.State.Loading, null);
//                getChapterList(mPage);
            }

            @Override
            public void onScrolled(int distanceX, int distanceY) {

            }

            @Override
            public void onScrollStateChanged(int state) {

            }
        });
        isRefresh = true;
        getChapterList(1);
    }

    private void initView() {
        mTv_title = (TextView) findViewById(R.id.title_center);
        mIv_back = (ImageView) findViewById(R.id.title_left);
        mIv_back.setVisibility(View.VISIBLE);
        et_chapter = (EditText) findViewById(R.id.et_chapter);
        ll_jump = (LinearLayout) findViewById(R.id.ll_jump);
        ll_jump.setVisibility(View.VISIBLE);
        tv_go = (TextView) findViewById(R.id.tv_go);
        tv_go.setOnClickListener(this);
        mRecycle_chapter = (LRecyclerView) findViewById(R.id.recycle_chapter);
        mLL = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void scrollToPositionWithOffset(int position, int offset) {
                super.scrollToPositionWithOffset(position, offset);

            }


        };
        mRecycle_chapter.setLayoutManager(mLL);
        mTv_title.setText(getString(R.string.title_chapter));
        mIv_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_left:
                finish();
                break;
            case R.id.tv_go:
                String sChapter=et_chapter.getText().toString();
//                Toast.makeText(BookChapterListActivity.this, sChapter.length()+"", Toast.LENGTH_SHORT).show();
                int chapter=0;
                if(sChapter.length()>0) {
                    chapter = Integer.parseInt(sChapter);
                }

                int screen = SPHelper.getPhoneHeight(this);
                if (chapter > mData.size()) {
                    Toast.makeText(BookChapterListActivity.this, "本文只有" + mData.size() + "章哦", Toast.LENGTH_SHORT).show();
                    return;
                }

                hideKeyboard();
                Log.e("tag", "" + chapter + " " + mData.size());
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecycle_chapter.getLayoutManager();
//                mRecycle_chapter.scrollToPosition(chapter);
                linearLayoutManager.scrollToPositionWithOffset(chapter, screen / 2);
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen=imm.isActive();
        if(isOpen) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //点击item
    @Override
    public void onItemClick(int position) {

        if (mDialog != null) {
            Tools.closeProgressDialog(mDialog);
        }
        mDialog = Tools.showProgressDialog(this);

        //去阅读界面
        ChapterBean bean = mData.get(position);
        if (!InfoUtils.cachingBook.contains(bean.getBookName())) {
            InfoUtils.cachingBook.add(bean.getBookName());
        }
        Log.e("chap", bean.getChapterName() + "  " + bean.getChapterId() + "  " + bean.getPage());
        //缓存一章
        hasReceiver = false;
        SPHelper.setCurChapterNO(this, bean.getBookName(), position + 1);
        SPHelper.setCurrentPage(this, bean.getBookName(), 1);
        SPHelper.setCurChapterName(this, bean.getBookName(), bean.getChapterName());
        Log.e("tag", "第" + position + "章已经缓存" + saveBook2Case() + mBookFactory.checkFileExist(position + 1));
        if (saveBook2Case()) {
            //判断文件是否已经缓存在本地了
            if (mBookFactory.checkFileExist(position + 1)) {
                Intent intentRead = new Intent(BookChapterListActivity.this, ReadActivity.class);
                intentRead.putExtra("bookInfo", mBookCaseBean);
                intentRead.putExtra("isNetBook", true);
                startActivity(intentRead);
//                mDialog.dismiss();
            } else {
                Log.e("huancunq", "position" + position);

                ;
                CacheUtils.cacheFile(this, bean.getBookName(), mBookCaseBean.getBook_id(), position, position, false);
                CacheUtils.cacheFile(this, bean.getBookName(), mBookCaseBean.getBook_id(), position + 2, position + 2, false);
                CacheUtils.cacheFile(this, bean.getBookName(), mBookCaseBean.getBook_id(), position + 1, position + 1, true);

//                mDialog.dismiss();
            }
        } else {
            Toast.makeText(this, getString(R.string.add2bookcase_failure), Toast.LENGTH_SHORT).show();

        }
        if (mDialog != null) {
            Log.e("diss", "dialog diss");
            Tools.closeProgressDialog(mDialog);
        }
    }

    private boolean saveBook2Case() {
        mBookCaseBean.setCache(0);
        mBookCaseBean.setBook_type(1);
        mBookCaseBean.setAdd_time(System.currentTimeMillis() + "");

        BookCaseDBHelper helper = new BookCaseDBHelper(this);
        if (helper.selectBookCaseByName(mBookCaseBean.getBook_name()) != null) {
            return true;
        }
        Log.e("addto", "2");
        if (helper.addToBookCase(mBookCaseBean)) {
            return true;
        } else {
            return false;
        }
    }


    private ProgressDialog mDialog;

    //加载目录列表
    private void getChapterList(int page) {
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
        params.put("id", mBookCaseBean.getBook_id());
        params.put("pageno", 0);
        OkHttpUtils.postString().url(APIDefine.GET_NOVEL_DIR)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "get  novel chapter list error:" + e);
                if (isRefresh) {
                    Tools.closeProgressDialog(mDialog);
                    isRefresh = false;
                    mRecycle_chapter.refreshComplete();
                }
                RecyclerViewStateUtils.setFooterViewState(mRecycle_chapter, LoadingFooter.State.Normal);
            }

            @Override
            public void onResponse(String response, int id) {
                // Log.e("TAG", "get novel chapter list response:" + response);
                if (isRefresh) {
                    Tools.closeProgressDialog(mDialog);
                    mRecycle_chapter.refreshComplete();
                }
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    String baseUrl = root.getString("baseUrl");
                    if (1 == rc) {
                        if (isRefresh) {
                            isRefresh = false;
                            mData.clear();
                        }
                        JSONArray data = root.getJSONArray("data");
                        if (data.length() > 0) {
                            for (int i = 0; i < data.length(); i++) {
                                ChapterBean bean = new ChapterBean();
                                bean.setBookName(mBookCaseBean.getBook_name());
                                JSONObject value = data.getJSONObject(i);
                                bean.setChapterName(value.getString("name"));
                                bean.setChapterId(value.getInt("no"));
                                bean.setChapterPosition(baseUrl + value.getString("url"));

                                mData.add(bean);
                            }
                            //保存最后一章
                            mBookCaseBean.setLast_chapter_no(mData.size() - 1);
                            mBookCaseBean.setLast_chapter(mData.get(mData.size() - 1).getChapterName());
                            Log.e("datasize", data.length() + " ");
                            RecyclerViewStateUtils.setFooterViewState(mRecycle_chapter, LoadingFooter.State.Normal);
                        } else {
                            RecyclerViewStateUtils.setFooterViewState(mRecycle_chapter, LoadingFooter.State.TheEnd);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
