package com.syezon.reader.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.syezon.reader.R;
import com.syezon.reader.activity.BookDetailActivity;
import com.syezon.reader.activity.ReadActivity;
import com.syezon.reader.adapter.BookCaseListAdapter;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.db.PageDBHelper;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.service.BookIndexService;
import com.syezon.reader.service.GetCharsetService;
import com.syezon.reader.utils.CacheUtils;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.SPHelper;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 书架界面
 * Created by jin on 2016/9/13.
 */
public class BookCaseFragment extends Fragment implements BookCaseListAdapter.IBookCaseListClickListener {

    private RecyclerView mRv_bookcase;
    public static List<BookCaseBean> mData;
    private BookCaseDBHelper mDBHelper;
    private BookCaseListAdapter mAdapter;

    private BookCaseBean mReadBean;//要读取的书本
    private boolean hasReceiver = false;
    private InfoUtils infoUtils;
    private ProgressBar pb;

    //缓存完成后刷新书籍刷新书籍
    public class RefreshDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("fenye222", intent.getBooleanExtra("cacheOne", false) + " " + !hasReceiver + " " + (mReadBean != null));
            if (intent.getBooleanExtra("cacheOne", false) && !hasReceiver && mReadBean != null) {

                hasReceiver = true;
                // Log.e("TAG", "receive broadcast in bookCaseFragment");
                //在跳转进页面中
                boolean isNetBook = mReadBean.getBook_type() == 1 ? true : false;
                Intent intentRead = new Intent(getActivity(), ReadActivity.class);
                intentRead.putExtra("bookInfo", mReadBean);
                intentRead.putExtra("isNetBook", isNetBook);
                startActivity(intentRead);
            }
            mData.clear();
            mData.addAll(mDBHelper.queryBookCase());
            mAdapter.notifyDataSetChanged();
        }
    }

    class RefreshViewReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("fenye222", intent.getBooleanExtra("cacheOne", false) + " " + !hasReceiver + " " + (mReadBean != null));

            pb.setVisibility(View.GONE);
            backgroundAlpha(1.0f);

        }
    }

    private RefreshDataReceiver mReceiver;
    private RefreshViewReceiver viewReceiver;

    //刷新数据
    public void onFreshData() {
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookcase, container, false);
        mDBHelper = new BookCaseDBHelper(getActivity());
        initData();
        initView(view);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mDialog != null) {
//            if (mDialog.isShowing()) {
//                Log.e("mDialog", "dismiss");
//                mDialog.dismiss();
//            }
//        }

        mAdapter.removeHandler();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mData.size() == 0) {
            Toast.makeText(getActivity(), "请去书城或者wifi传书添加书籍", Toast.LENGTH_SHORT).show();
        }
        MobclickAgent.onPageStart("BookCaseFragment"); //统计页面，"MainScreen"为页面名称，可自定义
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.dismiss();
//        }

        initReceiver();
    }

    @Override
    public void onPause() {
        MobclickAgent.onPageEnd("BookCaseFragment");
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);

    }

    private void initReceiver() {
        mReceiver = new RefreshDataReceiver();
        IntentFilter filter = new IntentFilter();

        filter.addAction("cacheComplete");
        getActivity().registerReceiver(mReceiver, filter);
        viewReceiver = new RefreshViewReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("enterRead");
        getActivity().registerReceiver(viewReceiver, filter);
    }

    private void initView(View view) {
        mRv_bookcase = (RecyclerView) view.findViewById(R.id.bookcase_list);
        mRv_bookcase.setLayoutManager(new LinearLayoutManager(getActivity()));
        pb = (ProgressBar) view.findViewById(R.id.pb);
        mAdapter = new BookCaseListAdapter(getActivity(), mData);
        mAdapter.setOnClickListener(this);
        mRv_bookcase.setAdapter(mAdapter);
    }

    private void initData() {
        if (infoUtils == null) {
            infoUtils = InfoUtils.getInstance(getActivity());
        }
        if (mData == null) {
            mData = mDBHelper.queryBookCase();
        } else {
            mData.clear();
            Log.e("tianjia", mDBHelper.queryBookCase().size() + "");
            mData.addAll(mDBHelper.queryBookCase());
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClickListener(int position) {
        pb.postInvalidate();
        pb.setVisibility(View.VISIBLE);
        InfoUtils.RANDOM = (int) (Math.random() * 10 + 1);
        backgroundAlpha(0.5f);
//        ProgressDialog
//        mDialog= Tools.showProgressDialog(getActivity());
        String bookName = mData.get(position).getBook_name();
        MobclickAgent.onEvent(getActivity(), "bookcase_bookName", bookName);
        if (!InfoUtils.cachingBook.contains(bookName)) {
            InfoUtils.cachingBook.add(bookName);
        }
        int bookType = mData.get(position).getBook_type();
        if (!SPHelper.getNeedCharset(getActivity(), bookName) && bookType == 0) {
            getActivity().stopService(new Intent(getActivity(), GetCharsetService.class));
            SPHelper.setNeedCharset(getActivity(), bookName, true);
        }
//        if (SPHelper.getSaveInfo(getActivity())) {
//            ToastUtil.showToast(getActivity(), "正在后台储存分页信息，请稍后", Toast.LENGTH_SHORT);
//            return;
//        }

//        a.equals("dd");
        hasReceiver = false;
        int cache = mData.get(position).getCache();//已经缓存的章节
        Log.e("hascache", cache + "");
        if (bookType == 2 || bookType == 0) {
            //本地书籍,或者内置小说
            Log.e("ziti", infoUtils.pageDetail.containsKey(bookName) + " " + SPHelper.getBookInfoSize(getActivity(), bookName) + " " + SPHelper.getBookTextSize(getActivity()));
            if (infoUtils.pageDetail.containsKey(bookName)) {

                if (SPHelper.getBookTextSize(getActivity()) == SPHelper.getBookInfoSize(getActivity(), bookName)) {
                    gotoRead(position);
                } else {
//                    ChapterBean bean = infoUtils.readChapter(bookName, SPHelper.getCurChapterNO(getActivity(), bookName));
//                    todoDividePage(null, bean, true, true, bookName);
                    SPHelper.setBookInfoSize(getActivity(), bookName, SPHelper.getBookTextSize(getActivity()));
                    infoUtils.pageDetail.remove(bookName);
                    infoUtils.chapterDetail.remove(bookName);
                    infoUtils.indexChapter.remove(bookName);

                    //分页未完成，继续去分页
                    Toast.makeText(getActivity(), getString(R.string.parse_book_tip), Toast.LENGTH_SHORT).show();
                    mReadBean = null;
                    mReadBean = mData.get(position);
                    gotoDivide(bookName, SPHelper.getBookFilePath(getActivity(), bookName), "引言", SPHelper.getBookEnCoding(getActivity(), bookName), true, true, bookType);
                }

            } else if (!SPHelper.getParsePageComplete(getActivity(), bookName) || SPHelper.getBookInfoSize(getActivity(), bookName) == 0) {//页保存完就真的保存完了
//

                //分页未完成，继续去分页
                Toast.makeText(getActivity(), getString(R.string.parse_book_tip), Toast.LENGTH_SHORT).show();
                SPHelper.setBookInfoSize(getActivity(), bookName, SPHelper.getBookTextSize(getActivity()));
                mReadBean = null;
                mReadBean = mData.get(position);
                gotoDivide(bookName, SPHelper.getBookFilePath(getActivity(), bookName), "引言", SPHelper.getBookEnCoding(getActivity(), bookName), true, true, bookType);
            } else {//分页完成了，直接去读取
                gotoRead(position);
            }
        } else if (bookType == 1)

        {//网络书籍

            Log.e("ziti", SPHelper.getBookTextSize(getActivity()) + " " + SPHelper.getBookInfoSize(getActivity(), bookName));
            if (cache > 0) {
                if (SPHelper.getBookTextSize(getActivity()) == SPHelper.getBookInfoSize(getActivity(), bookName)) {
                    gotoRead(position);
                } else {
                    SPHelper.setBookInfoSize(getActivity(), bookName, SPHelper.getBookTextSize(getActivity()));
                    int chapter = SPHelper.getCurChapterNO(getActivity(), bookName);
//                    PageDBHelper pageDb = new PageDBHelper(getActivity(), bookName);
//                    pageDb.delete();
//                    pageDb.closeDB();
                    mReadBean = null;
                    mReadBean = mData.get(position);
                    ChapterDBHelper mChapterDBHelper = new ChapterDBHelper(getActivity(), bookName);
                    ChapterBean bean = mChapterDBHelper.readBookOneChapter(chapter);
                    //对已经存在的进行分页
                    List<ChapterBean> data = mChapterDBHelper.readBookChapterList();
                    Log.e("times", bookName + " " + data.size() + "一共存在的章节");
                    ArrayList<ChapterBean> needdevidedata = new ArrayList<ChapterBean>();
                    todoDividePage(null, bean, true, true, bookName);

                    for (int i = 0; i < data.size(); i++) {
                        //对剩下的其他章节进行分页
                        needdevidedata.add(data.get(i));
                    }
                    todoDividePage(needdevidedata, bean, true, false, bookName);
                }
                Log.e("type", "type1");
            } else if (SPHelper.getHasCache(getActivity(), bookName) != -1) {

//                ChapterDBHelper chapterDBHelper = new ChapterDBHelper(getActivity(), bookName);
//                List<ChapterBean> chapterBeanList = chapterDBHelper.readBookChapterList();
//                for (ChapterBean chapterBean : chapterBeanList) {
//                    Log.e("chapterBean", chapterBean.getChapterName() + " " + chapterBean.getChapterPosition());
//                }    Log.e("type","type1");
//                position = position + 1;
                Log.e("type", "type2");
//                if (SPHelper.getBookTextSize(getActivity()) != SPHelper.getBookInfoSize(getActivity(), bookName)) {
//
//                }
                Log.e("get 123", SPHelper.getBookFilePath(getActivity(), bookName) + position);
//                gotoDivide(bookName, SPHelper.getBookFilePath(getActivity(), bookName) + position, "引言", SPHelper.getBookEnCoding(getActivity(), bookName), true, true, bookType);
                SPHelper.setBookInfoSize(getActivity(), bookName, SPHelper.getBookTextSize(getActivity()));
                gotoDivide(bookName, SPHelper.getBookFilePath(getActivity(), bookName) + 1, "引言", SPHelper.getBookEnCoding(getActivity(), bookName), true, true, bookType);
            } else {//添加了书架未缓存，去缓存
                Log.e("type", "type3");
                mReadBean = null;
                mReadBean = mData.get(position);
                // 缓存一章
                SPHelper.setBookInfoSize(getActivity(), bookName, SPHelper.getBookTextSize(getActivity()));
                int chapter = mReadBean.getCache() == 0 ? 1 : mReadBean.getCache();
                CacheUtils.cacheFile(getActivity(), mReadBean.getBook_name(), mReadBean.getBook_id(), chapter + 1, chapter + 1, false);
                CacheUtils.cacheFile(getActivity(), mReadBean.getBook_name(), mReadBean.getBook_id(), chapter, chapter, true);
            }
        }

    }

    private void backgroundAlpha(float v) {
        if (getActivity() != null) {
            WindowManager.LayoutParams lp = (getActivity()).getWindow().getAttributes();
            lp.alpha = v; //0.0-1.0
            (getActivity()).getWindow().setAttributes(lp);
        }


    }


    //去分页

    private void todoDividePage(ArrayList list, ChapterBean bean, boolean isCacheOne, boolean needopen, String bookname) {
        Intent intent = new Intent(getActivity(), BookIndexService.class);

        intent.putExtra("filePath", bean.getChapterPosition());
        intent.putExtra("bookName", bookname);
        intent.putExtra("cacheOne", isCacheOne);
        intent.putExtra("chapter", bean.getChapterId());
        intent.putExtra("encoding", SPHelper.getBookEnCoding(getActivity(), bookname));

        intent.putExtra("bookType", 1);


        intent.putStringArrayListExtra("needdevide", list);
        intent.putExtra("redevide", true);
        intent.putExtra("needOpen", needopen);
//        intent.putExtra("fenye", true);
        Log.e("fenye", "重新分页");
        getActivity().startService(intent);
    }


    //去阅读界面
    private void gotoRead(int position) {

        //在跳转进页面中
        Intent intent = new Intent(getActivity(), ReadActivity.class);
        intent.putExtra("bookInfo", mData.get(position));
        if (mData.get(position).getBook_type() == 1) {
            intent.putExtra("isNetBook", true);
        } else {
            intent.putExtra("isNetBook", false);
        }
        startActivity(intent);
    }

    //去分页
    private void gotoDivide(String bookName, String filePath, String chapterName, String
            encoding, boolean cacheOne, boolean needOpen, int bookType) {
        Intent intentService = new Intent(getActivity(), BookIndexService.class);
        intentService.putExtra("filePath", filePath);

        intentService.putExtra("bookName", bookName);
        Log.e("tag", bookName + filePath);
        if (!chapterName.equals("")) {
            intentService.putExtra("chapterName", chapterName);
        }
        intentService.putExtra("bookType", bookType);
        intentService.putExtra("cacheOne", cacheOne);
        intentService.putExtra("needOpen", needOpen);
        intentService.putExtra("encoding", encoding);
        getActivity().startService(intentService);

    }

    @Override
    public void onDetailClickListener(int position) {
        BookCaseBean bean = mData.get(position);
        Intent intent = new Intent(getActivity(), BookDetailActivity.class);
        intent.putExtra("bookid", bean.getBook_id());
        startActivity(intent);
    }

    @Override
    public void onCacheClickListener(int position) {
        BookCaseBean bean = mData.get(position);
        Log.e("onCacheClickListener", bean.getLast_chapter_no() + "");
        //缓存整本书
        int chapter = bean.getCache() + 1;
        CacheUtils.cacheFile(getActivity(), bean.getBook_name(), bean.getBook_id(), chapter, -5, false);
    }

    @Override
    public void onTopClickListener(int position) {
        mDBHelper.updateBookCase(mData.get(position), mDBHelper.BOOK_TOP);
        mData.clear();
        mData.addAll(mDBHelper.queryBookCase());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteClickListener(int position) {
        InfoUtils infoUtils = InfoUtils.getInstance(getActivity());
        infoUtils.remove(mData.get(position).getBook_name());
        Map pageDetail = SPHelper.getObject(getActivity(), "pageDetail");
        if (pageDetail != null && pageDetail.containsKey(mData.get(position).getBook_name())) {
            pageDetail.remove(mData.get(position).getBook_name());
            SPHelper.setObject(getActivity(), "pageDetail", pageDetail);
        }
        Map chapterDetail = SPHelper.getObject(getActivity(), "chapterDetail");
        if (chapterDetail != null && chapterDetail.containsKey(mData.get(position).getBook_name())) {
            chapterDetail.remove(mData.get(position).getBook_name());
            SPHelper.setObject(getActivity(), "chapterDetail", chapterDetail);
        }
        //删除章节信息
        ChapterDBHelper bookDBHelper = new ChapterDBHelper(getActivity(), mData.get(position).getBook_name());
        bookDBHelper.delete();
        //删除页信息
        PageDBHelper pageDBHelper = new PageDBHelper(getActivity(), mData.get(position).getBook_name());
        pageDBHelper.delete();
        //删除书架信息
        mDBHelper.deleteBookCase(mData.get(position).getBook_name());
        mData.remove(position);
        mAdapter.notifyDataSetChanged();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (viewReceiver != null) {

            getActivity().unregisterReceiver(viewReceiver);
        }
    }
}
