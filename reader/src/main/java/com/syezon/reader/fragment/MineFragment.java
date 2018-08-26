package com.syezon.reader.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.IUnreadCountCallback;
import com.syezon.reader.R;
import com.syezon.reader.activity.MainActivity;
import com.syezon.reader.activity.WiFiTransferActivity;
import com.syezon.reader.application.ReaderApplication;
import com.syezon.reader.constant.Constant;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.db.PageDBHelper;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.service.BookIndexService;
import com.syezon.reader.service.CacheService;
import com.syezon.reader.utils.CleanCacheUtils;
import com.syezon.reader.utils.FileUtils;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.ToastUtil;
import com.syezon.reader.view.UpdataDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;

/**
 * 我的界面
 * Created by jin on 2016/9/13.
 */
public class MineFragment extends Fragment implements View.OnClickListener, CleanCacheUtils.IDialogClickListener {

    private static final int CLEAR_CACHE_COMPLETE = 1;
    private static final int STORAGE_AND_CAMERA_PERMISSIONS = 2;
    private View mRootView;
    private RelativeLayout mRL_not_auto_lock;
    private RelativeLayout mRL_wifi;
    private RelativeLayout mRL_clear_cache;
    private RelativeLayout mRL_feedback;
    private RelativeLayout mRL_about;
    private ImageView mIv_not_auto_lock;
    private InfoUtils infoUtils;
    private boolean isNotAutoLock = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLEAR_CACHE_COMPLETE:
                    Toast.makeText(ReaderApplication.getInstance().getApplicationContext(), ReaderApplication.getInstance().getApplicationContext().getString(R.string.clear_cache_complete), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_mine, container, false);
        initView(mRootView);
        return mRootView;
    }

    private void initView(View view) {
        mRL_not_auto_lock = (RelativeLayout) view.findViewById(R.id.rl_not_auto_lock);
        mRL_wifi = (RelativeLayout) view.findViewById(R.id.rl_wifi);
        mRL_clear_cache = (RelativeLayout) view.findViewById(R.id.rl_clear_cache);
        mRL_feedback = (RelativeLayout) view.findViewById(R.id.rl_feedback);
        mRL_about = (RelativeLayout) view.findViewById(R.id.rl_about);
        mIv_not_auto_lock = (ImageView) view.findViewById(R.id.iv_not_auto_lock);

        if (infoUtils == null) {
            infoUtils = InfoUtils.getInstance(getActivity());
        }
        mRL_not_auto_lock.setOnClickListener(this);
        mRL_wifi.setOnClickListener(this);
        mRL_clear_cache.setOnClickListener(this);
        mRL_feedback.setOnClickListener(this);
        mRL_about.setOnClickListener(this);

        isNotAutoLock = SPHelper.getNotAutoLock(getActivity());
        changeSelectStatus(mIv_not_auto_lock, isNotAutoLock);
    }

    private Boolean isGetting = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_not_auto_lock:
                isNotAutoLock = !isNotAutoLock;
                SPHelper.setNotAutoLock(getActivity(), isNotAutoLock);
                changeSelectStatus(mIv_not_auto_lock, isNotAutoLock);
                break;
            case R.id.rl_wifi:
                Intent intent = new Intent(getActivity(), WiFiTransferActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_clear_cache:
                CleanCacheUtils cleanCache = new CleanCacheUtils();
                cleanCache.showCleanDialog(mRootView, getActivity(), getString(R.string.clear_cache_title), getString(R.string.clear_cache_msg), this);
                break;
            case R.id.rl_feedback:

//                Intent intent1 = new Intent(getActivity(), FeedBackActivity.class);
//                startActivity(intent1);
//                FeedbackAPI.openFeedbackActivity();
//                Fragment fragment = FeedbackAPI.getFeedbackFragment();
                if (isGetting) {
                    return;
                }
                checkForOpenOrGet(true);
                break;
            case R.id.rl_about:

                String nowVersion = SPHelper.getLastVersion(getActivity());
                Log.e("ttt", nowVersion + "// " + SConfig.VERSION_NAME);
                if (nowVersion.equals(SConfig.VERSION_NAME)) {
                    showUpdataDialog();
                    ToastUtil.showToast(getActivity(), "恭喜您，已经是最新版本", Toast.LENGTH_SHORT);
                } else {
                    ToastUtil.showToast(getActivity(), "检测到有新版本", Toast.LENGTH_SHORT);
                    showUpdataDialog();
                }
//                Intent intent2 = new Intent(getActivity(), AboutActivity.class);
//                startActivity(intent2);
                break;
        }
    }

    private RequestCall requestCall;

    private void showUpdataDialog() {

        final UpdataDialog updataDialog = new UpdataDialog(getActivity(), R.style.dialog_orders);
        updataDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                              @Override
                                              public void onDismiss(DialogInterface dialog) {

                                              }
                                          }
        );
        updataDialog.setUpDialogListener(new UpdataDialog.UpDialogListener() {
            @Override
            public void update() {
                if (!Constant.UPDATE_URL.equals("-1")) {
                    requestCall = OkHttpUtils.get().url(Constant.UPDATE_URL).build();

                    String dirName = getContext().getFilesDir().getPath();
                    requestCall.execute(new FileCallBack(dirName, "update.apk") {
                        @Override
                        public void inProgress(float progress, long total, int id) {
                            super.inProgress(progress, total, id);
                            Log.e("inpro", progress + " ");

                            String pro = String.valueOf(progress * 100);
                            String mpro = pro.substring(0, pro.indexOf("."));
                            updataDialog.setProgress(mpro);

                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(File response, int id) {
                            Log.e("file", response.getName() + " " + response.getPath());
                            updataDialog.dismiss();
                            MainActivity.openApk(response, getActivity());
                        }

                    });

                }
            }

            @Override
            public void cancel() {
                updataDialog.dismiss();
                if (requestCall != null) {

                    requestCall.cancel();
                }
            }
        });
        updataDialog.show();
    }

    private String getAppInfo() {
        try {
            String pkName = getActivity().getPackageName();
            String versionName = getActivity().getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            int versionCode = getActivity().getPackageManager()
                    .getPackageInfo(pkName, 0).versionCode;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }

    private void checkForOpenOrGet(boolean isOpenFeedback) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , STORAGE_AND_CAMERA_PERMISSIONS);
        } else {
            openOrGet(isOpenFeedback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        FeedbackAPI.cleanFeedbackFragment();
//        FeedbackAPI.cleanActivity();
    }

    /**
     * @param isOpenFeedback 打开网页or获取未读数
     */
    private void openOrGet(final boolean isOpenFeedback) {
        //接入方不需要这样调用, 因为扫码预览, 同时为了服务器发布后能做到实时预览效果, 所有每次都init.
        //业务方默认只需要init一次, 然后直接openFeedbackActivity/getFeedbackUnreadCount即可

        final Activity context = getActivity();
        //如果500ms内init未完成, openFeedbackActivity会失败, 可以延长时间>500ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOpenFeedback) {
                    FeedbackAPI.openFeedbackActivity();
                } else {
                    FeedbackAPI.getFeedbackUnreadCount(new IUnreadCountCallback() {
                        @Override
                        public void onSuccess(final int unreadCount) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getActivity(), "未读数：" + unreadCount, Toast.LENGTH_SHORT);
                                    toast.show();
                                    isGetting = false;
                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {

                        }
                    });
                }
                isGetting = false;
            }
        }, 500);
    }

    //改变选中状态
    private void changeSelectStatus(ImageView view, boolean isSelect) {
        if (isSelect) {
            view.setImageResource(R.mipmap.switch_on);
        } else {
            view.setImageResource(R.mipmap.switch_off);
        }
    }

    //清理缓存时对话框，取消
    @Override
    public void onCancelClick() {

    }

    //清理缓存时对话框，确定
    @Override
    public void onSureClick() {

        //开线程清理数据
        new Thread(new Runnable() {
            @Override
            public void run() {

                //遍历书架清空数据
                BookCaseDBHelper helper = new BookCaseDBHelper(getActivity());
                List<BookCaseBean> list = helper.queryBookCase();
                try {


//                Log.e("delete1", list.size() + "" + list.get(0).getBook_name() + " " + list.get(0).getBook_type());
                    for (int i = 0; i < list.size(); i++) {
//                    if (list.get(i).getBook_type() == 1) {//wifi传输的内容不清空
                        String bookName = list.get(i).getBook_name();
                        if (SPHelper.getCache(getActivity(), bookName).size() != 0) {
                            SPHelper.setCache(getActivity(), bookName, new HashSet<String>());
                        }

                        //将书籍相关内容全部删除
                        Log.e("TAG", "delete cache:" + bookName);
                        ChapterDBHelper bookDBHelper = new ChapterDBHelper(getActivity(), bookName);
                        PageDBHelper pageDBHelper = new PageDBHelper(getActivity(), bookName);

                        bookDBHelper.delete();
                        pageDBHelper.delete();

                        bookDBHelper.closeDB();
                        pageDBHelper.closeDB();
                        //更新缓存信息
                        BookCaseBean bean = list.get(i);
                        bean.setCache(0);
                        Log.e("delete", list.get(i).getBook_name() + " " + list.get(i).getBook_type());
//                    清除数据库中的book
                        helper.deleteBookCase(list.get(i).getBook_name());
                        try {
                            SPHelper.setHasCache(getActivity(), list.get(i).getBook_name(), 0);
                        } catch (Exception e) {
                        }

                        helper.updateBookCase(bean, BookCaseDBHelper.BOOK_CACHE);
//                    }
                    }
                } catch (Exception e) {

                }
                //清除缓存队列
                CacheService.mCacheTask.clear();
                BookIndexService.mStartTask.clear();
                String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/com.syezon.reader/book";
                FileUtils.deleteDirectory(dirName);
                SPHelper.remove(getActivity(), "indexPage");
                SPHelper.remove(getActivity(), "indexChapter");
                SPHelper.remove(getActivity(), "chapterDetail");
                SPHelper.remove(getActivity(), "pageDetail");
                infoUtils.remove();
//                BookCaseFragment.mData.clear();
                Message msg = Message.obtain();
                msg.what = CLEAR_CACHE_COMPLETE;
                mHandler.sendMessage(msg);
            }
        }).start();
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
