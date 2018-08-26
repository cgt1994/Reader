package com.syezon.reader.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.voiceads.IFLYInterstitialAd;
import com.syezon.reader.R;
import com.syezon.reader.constant.Constant;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.fragment.BookCaseFragment;
import com.syezon.reader.fragment.BookStoreFragment;
import com.syezon.reader.fragment.MineFragment;
import com.syezon.reader.utils.ADUtils;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.ToastUtil;
import com.syezon.reader.utils.Tools;
import com.syezon.reader.view.QuitDialog;
import com.syezon.reader.view.UpdataDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.Calendar;

import okhttp3.Call;

import static com.syezon.reader.utils.MarketUtils.launchAppDetail2;

//import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ADUtils.InterstitialADListener {

    private TextView mTv_title, mTv_search, mTv_bookCase, mTv_bookStore, mTv_mine, mTv_bookCase_tip, mTv_bookStore_tip, mTv_mine_tip;
    private LinearLayout mLinear_bookCase, mLinear_bookStore, mLinear_mine;
    private FragmentManager mManager;
    private FragmentTransaction mTransaction;

    private BookCaseFragment mBookCaseFragment;
    private BookStoreFragment mBookStoreFragment;
    private MineFragment mMineFragment;
    private InfoUtils infoUtils;
    private BookCaseDBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.setStatusBarColor(this, R.color.title_bg);
        infoUtils = InfoUtils.getInstance(this);
        initView();
        mDBHelper = new BookCaseDBHelper(this);
//        initFragment();
//        connection=new MyConnection(){}
        Log.e("inpro", SPHelper.getNeedUpdate(MainActivity.this) + " ");
        if (SPHelper.getNeedUpdate(MainActivity.this)) {
            if (FristOpen()) {
                showUpdataDialog();
            } else {
                initFragment();
            }
        } else {
            initFragment();
        }


    }

    private boolean FristOpen() {
        String lastTime = SPHelper.getUpdataDate(MainActivity.this);
        Calendar c = Calendar.getInstance();
//        取得系统日期
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        String time = year + "" + month + "" + day;
        Log.e("time", year + " " + month + " " + day);
        if (TextUtils.isEmpty(lastTime)) {
            SPHelper.setUpdataDate(MainActivity.this, time);
            return true;
        } else {
            if (lastTime.equals(time)) {
                return false;
            } else {
                SPHelper.setUpdataDate(MainActivity.this, time);
                return true;
            }

        }
    }

    private RequestCall requestCall;

    private void showUpdataDialog() {

        final UpdataDialog updataDialog = new UpdataDialog(MainActivity.this, R.style.dialog_orders);
        updataDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                              @Override
                                              public void onDismiss(DialogInterface dialog) {
                                                  initFragment();
                                              }
                                          }
        );
        updataDialog.setUpDialogListener(new UpdataDialog.UpDialogListener() {
            @Override
            public void update() {
                if (!Constant.UPDATE_URL.equals("-1")) {
                    requestCall = OkHttpUtils.get().url(Constant.UPDATE_URL).build();
                    String dirName = getFilesDir().getPath();
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
                            openApk(response, MainActivity.this);
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


    public static void openApk(File file, Context context) {
        if (file == null) {
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

//            if ((System.currentTimeMillis() - mExitTime) > 2000) {
            final QuitDialog quitDialog = new QuitDialog(MainActivity.this, R.style.dialog_orders);
            quitDialog.setDialogClickListener(new QuitDialog.DialogClickListener() {
                @Override
                public void sure() {
                    launchAppDetail2(MainActivity.this);
                }

                @Override
                public void cancel() {
                    quitDialog.dismiss();
                    finish();

                }
            });
            quitDialog.show();
//                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
//                mExitTime = System.currentTimeMillis();
//            } else {
//                finish();
//            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

//        getToken();
    //显示插屏广告


    private void initView() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "iconfont.ttf");
        mLinear_bookCase = (LinearLayout) findViewById(R.id.linear_bookcase);
        mLinear_bookStore = (LinearLayout) findViewById(R.id.linear_bookstore);
        mLinear_mine = (LinearLayout) findViewById(R.id.linear_mine);
        mTv_title = (TextView) findViewById(R.id.title_center);
        mTv_bookCase = (TextView) findViewById(R.id.tv_bookcase);
        mTv_bookCase_tip = (TextView) findViewById(R.id.tv_bookcase_tip);
        mTv_bookStore = (TextView) findViewById(R.id.tv_bookstore);
        mTv_bookStore_tip = (TextView) findViewById(R.id.tv_bookstore_tip);
        mTv_mine = (TextView) findViewById(R.id.tv_mine);
        mTv_mine_tip = (TextView) findViewById(R.id.tv_mine_tip);
        mTv_search = (TextView) findViewById(R.id.title_right);

        mTv_title.setText(getString(R.string.title_bookcase));
        mTv_bookCase.setTypeface(typeface);
        mTv_bookStore.setTypeface(typeface);
        mTv_mine.setTypeface(typeface);
        mTv_search.setTypeface(typeface);
        mTv_search.setVisibility(View.VISIBLE);
        mLinear_bookCase.setOnClickListener(this);
        mLinear_bookStore.setOnClickListener(this);
        mLinear_mine.setOnClickListener(this);
        mTv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initFragment() {
        mManager = getSupportFragmentManager();
        mBookCaseFragment = new BookCaseFragment();
        mBookStoreFragment = new BookStoreFragment();
        mMineFragment = new MineFragment();
        //添加第一个
        mManager.beginTransaction().add(R.id.content, mBookCaseFragment).commit();
        if (mDBHelper.queryBookCase().size() == 0) {
//            FragmentTransaction transaction = mManager.beginTransaction();
//            transaction.hide(mBookCaseFragment);
//            transaction.add(R.id.content, mBookStoreFragment);
            onClick(mLinear_bookStore);

//            transaction.commit();
        }
    }

    @Override
    protected void onStop() {
        Log.e("tag", "onStop");

        super.onStop();
    }

    //
    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        Log.e("tag", "onPause");
        save();
        super.onPause();
    }

    private void save() {

//        SPHelper.setObject(this, "indexPage", infoUtils.indexPage);
        SPHelper.setObject(this, "indexChapter", infoUtils.indexChapter);
//        SPHelper.setObject(this, "chapterDetail", infoUtils.chapterDetail);
//        SPHelper.setObject(this, "pageDetail", infoUtils.pageDetail);
    }

    private String getAppInfo() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            int versionCode = this.getPackageManager()
                    .getPackageInfo(pkName, 0).versionCode;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        resetSelect();
        mTransaction = mManager.beginTransaction();
        switch (v.getId()) {
            case R.id.linear_bookcase:
                mTv_search.setVisibility(View.VISIBLE);
                mTv_bookCase.setText(getString(R.string.icon_bookcase_checked));
                mTv_bookCase.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_bookCase_tip.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_title.setText(getString(R.string.title_bookcase));
                mTv_title.setClickable(false);
                mTransaction.replace(R.id.content, mBookCaseFragment);
                mBookCaseFragment.onFreshData();
                break;
            case R.id.linear_bookstore:
                mTv_bookStore.setText(getString(R.string.icon_bookstore_checked));
                mTv_bookStore.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_bookStore_tip.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_title.setText(getString(R.string.title_free_novel));
                mTv_title.setClickable(false);
                mTv_search.setVisibility(View.VISIBLE);
                mTransaction.replace(R.id.content, mBookStoreFragment);

                break;
            case R.id.linear_mine:

                mTv_mine.setText(getString(R.string.icon_mine_checked));
                mTv_mine.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_mine_tip.setTextColor(getResources().getColor(R.color.title_bg));
                mTv_title.setText(getString(R.string.title_mine));
                mTv_title.setOnClickListener(new View.OnClickListener() {
                    Long i;
                    int times = 0;

                    @Override
                    public void onClick(View v) {
                        Log.e("titleclick", "我的 被点击");
                        if (times == 3) {
                            ToastUtil.showToast(MainActivity.this, "" + getAppInfo() + "  " + MainActivity.this.getString(R.string.syezon_channel_id) + "  " + SConfig.DEVICE_MODEL, Toast.LENGTH_SHORT);
                            times = 0;
                            i = System.currentTimeMillis();
                        }
                        if (times == 0) {
                            times++;
                            i = System.currentTimeMillis();
                        } else {
                            if (System.currentTimeMillis() - i < 1500) {

                                times++;
                            } else {
                                times = 0;
                            }
                            i = System.currentTimeMillis();
                        }
                    }
                });
                mTv_search.setVisibility(View.GONE);
                mTransaction.replace(R.id.content, mMineFragment);
                break;
        }
        mTransaction.commit();
    }

    //重置选中状态
    private void resetSelect() {
        mTv_search.setVisibility(View.GONE);
        mTv_bookCase.setText(getString(R.string.icon_bookcase_unchecked));
        mTv_bookCase.setTextColor(getResources().getColor(R.color.foot_unchecked));
        mTv_bookCase_tip.setTextColor(getResources().getColor(R.color.foot_unchecked));
        mTv_bookStore.setText(getString(R.string.icon_bookstore_unchecked));
        mTv_bookStore.setTextColor(getResources().getColor(R.color.foot_unchecked));
        mTv_bookStore_tip.setTextColor(getResources().getColor(R.color.foot_unchecked));
        mTv_mine.setText(getString(R.string.icon_mine_unchecked));
        mTv_mine.setTextColor(getResources().getColor(R.color.foot_unchecked));
        mTv_mine_tip.setTextColor(getResources().getColor(R.color.foot_unchecked));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        //刷新数据
        if (mBookCaseFragment != null) {

            mBookCaseFragment.onFreshData();
        }
    }

    private long mExitTime;


    @Override
    public void onLoadInterstitialAD(IFLYInterstitialAd interstitialAd) {
        interstitialAd.showAd();//显示广告
    }

    @Override
    public void onCloseInterstitialAD() {

    }

    @Override
    public void onClickInterstitialAD() {

    }

}
