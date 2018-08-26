package com.syezon.reader.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.adapter.SimilarBookAdapter;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
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
 * 书籍详情页
 * Created by jin on 2016/9/14.
 */
public class BookDetailActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int UPDATA_VIEW = 1;

    private ImageView mIv_back;
    private TextView mTv_title;
    private ImageView mIv_book;
    private TextView mTv_name, mTv_author, mTv_type, mTv_chapter;
    private TextView mTv_add, mTv_check;
    private TextView mTv_desc;
    private TextView mTv_read;
    private TextView mTv_similar_tip;
    private NoScrollGridView mGrid_similar;
    private ProgressDialog mDialog;

    private int mBookID;
    private SimilarBookAdapter mAdapter;
    private List<NovelBean> mLikeNovelData;
    private BookCaseBean mBookCaseBean = new BookCaseBean();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATA_VIEW:
                    try {
                        JSONObject root = (JSONObject) msg.obj;
                        String baseUrl = root.getString("baseUrl");
                        JSONObject data = root.getJSONObject("data");
                        JSONArray likeNovels = root.getJSONArray("likeNovels");
                        for (int i = 0; i < likeNovels.length(); i++) {
                            NovelBean bean = new NovelBean();
                            JSONObject value = likeNovels.getJSONObject(i);
                            bean.setId(value.getInt("articleId"));
                            bean.setName(value.getString("name"));
                            bean.setImg(baseUrl + value.get("cover"));
                            mLikeNovelData.add(bean);
                        }
                        if (mLikeNovelData.size() <= 0) {
                            mTv_similar_tip.setVisibility(View.GONE);
                        } else {
                            mTv_similar_tip.setVisibility(View.VISIBLE);
                        }
                        String bookName = data.getString("name");
                        String bookAuthor = data.getString("author");
                        String bookCover = baseUrl + data.getString("cover");
                        String lastChapter = data.getString("lastChapter");
                        int lastChapterNO = data.getInt("lastChapterno");
                        long updateTime = data.getLong("lastUpdate");
                        mAdapter.notifyDataSetChanged();
                        mTv_name.setText(bookName);
                        mTv_author.setText(bookAuthor);
                        mTv_chapter.setText(lastChapter);
                        mTv_desc.setText(data.getString("intro"));
                        mTv_type.setText(Tools.judgeNovelType(data.getInt("category")));
                        Picasso.with(BookDetailActivity.this).load(bookCover).placeholder(getResources().getDrawable(R.mipmap.error_pic)).into(mIv_book);

                        mBookCaseBean.setBook_id(mBookID);
                        mBookCaseBean.setBook_name(bookName);
                        mBookCaseBean.setBook_author(bookAuthor);
                        mBookCaseBean.setBook_img(bookCover);
                        mBookCaseBean.setBook_update_time(Tools.formatDate(updateTime));
                        mBookCaseBean.setLast_chapter(lastChapter);
                        mBookCaseBean.setLast_chapter_no(lastChapterNO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_detail);
        mBookID = getIntent().getIntExtra("bookid", 0);
        initView();
        getBookDetails();
    }

    private void initView() {
        mIv_back = (ImageView) findViewById(R.id.title_left);
        mTv_title = (TextView) findViewById(R.id.title_center);
        mIv_book = (ImageView) findViewById(R.id.book_img);
        mTv_name = (TextView) findViewById(R.id.book_name);
        mTv_author = (TextView) findViewById(R.id.book_author);
        mTv_type = (TextView) findViewById(R.id.book_type);
        mTv_chapter = (TextView) findViewById(R.id.book_chapter);
        mTv_add = (TextView) findViewById(R.id.tv_add);
        mTv_check = (TextView) findViewById(R.id.tv_check);
        mTv_desc = (TextView) findViewById(R.id.book_desc);
        mTv_read = (TextView) findViewById(R.id.book_read);
        mGrid_similar = (NoScrollGridView) findViewById(R.id.similar_book);
        mTv_similar_tip = (TextView) findViewById(R.id.tv_similar_tip);

        mIv_back.setVisibility(View.VISIBLE);
        mTv_title.setText(getString(R.string.title_detail));
        mIv_back.setOnClickListener(this);
        mTv_add.setOnClickListener(this);
        mTv_check.setOnClickListener(this);
        mTv_read.setOnClickListener(this);
        mLikeNovelData = new ArrayList<>();
        mAdapter = new SimilarBookAdapter(this, mLikeNovelData);
        mGrid_similar.setAdapter(mAdapter);
        mGrid_similar.setOnItemClickListener(this);
        mGrid_similar.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("BookDetailActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("BookDetailActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_left:
                finish();
                break;
            case R.id.tv_add:
                add2BookCase();
                break;
            case R.id.tv_check:
            case R.id.book_read:
                MobclickAgent.onEvent(this, "readbook", mBookCaseBean.getBook_name());

                Intent intent = new Intent(this, BookChapterListActivity.class);
                intent.putExtra("bookInfo", mBookCaseBean);
                startActivity(intent);
                break;
        }
    }

    //添加到书架
    private void add2BookCase() {
//        在添加到目录的时候要先获取章节列表
        getChapterListOnline();

        mBookCaseBean.setCache(0);
        mBookCaseBean.setBook_type(1);
        mBookCaseBean.setAdd_time(System.currentTimeMillis() + "");
        BookCaseDBHelper helper = new BookCaseDBHelper(this);
        if (helper.selectBookCaseByName(mBookCaseBean.getBook_name()) != null) {
            Toast.makeText(this, getString(R.string.add2bookcase_exist), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("addto", "3");
        if (helper.addToBookCase(mBookCaseBean)) {
            MobclickAgent.onEvent(this, "addbook", mBookCaseBean.getBook_name());
            Toast.makeText(this, getString(R.string.add2bookcase_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.add2bookcase_failure), Toast.LENGTH_SHORT).show();
        }

    }

    //相似图书的点击处理
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra("bookid", mLikeNovelData.get(position).getId());
        startActivity(intent);
    }

    //获取小说详情
    private void getBookDetails() {
        mDialog = Tools.showProgressDialog(this);
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
        params.put("id", mBookID);

        OkHttpUtils.postString().url(APIDefine.GET_NOVEL_DETAILS)
                .content(Tools.Map2Json(params))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "get book details error:" + e);
                Tools.closeProgressDialog(mDialog);
            }

            @Override
            public void onResponse(String response, int id) {
                Tools.closeProgressDialog(mDialog);
                //Log.e("TAG", "get book details response:" + response);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (rc == 1) {
                        Message msg = Message.obtain();
                        msg.what = UPDATA_VIEW;
                        msg.obj = root;
                        mHandler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private List<ChapterBean> mChapterData = new ArrayList<>();
    ;

    //加载网络端目录列表
    private void getChapterListOnline() {
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
                Log.e("zzz", e.getMessage());
                //Log.e("TAG", "get  novel chapter list error:" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e("zzz", "success" + response);
                // Log.e("TAG", "get novel chapter list response:" + response);
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    String baseUrl = root.getString("baseUrl");
                    if (1 == rc) {
                        JSONArray data = root.getJSONArray("data");
                        Log.e("zzz", mBookCaseBean.getBook_name() + " " + data.length() + " ");
                        for (int i = 0; i < data.length(); i++) {
                            ChapterBean bean = new ChapterBean();
                            bean.setBookName(mBookCaseBean.getBook_name());
                            JSONObject value = data.getJSONObject(i);
                            bean.setChapterName(value.getString("name"));
                            bean.setChapterId(value.getInt("no"));
                            bean.setChapterPosition(baseUrl + value.getString("url"));

                            mChapterData.add(bean);
                        }
                        //设置最后一章信息

                        SPHelper.setBookLength(BookDetailActivity.this, mBookCaseBean.getBook_name(), data.length());
                        SPHelper.setObject(BookDetailActivity.this, mBookCaseBean.getBook_name() + "chapterlist", mChapterData);
                    }
                } catch (JSONException e) {
                    Log.e("zzz", e.getMessage() + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }
}
