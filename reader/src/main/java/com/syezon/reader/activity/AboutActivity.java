package com.syezon.reader.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.syezon.reader.R;
import com.syezon.reader.utils.Tools;
import com.umeng.analytics.MobclickAgent;

/**
 * 关于界面
 * Created by jin on 2016/9/18.
 */
public class AboutActivity extends Activity implements View.OnClickListener {


    private TextView mTv_title;
    private ImageView mIv_back;
    private ImageView mIv_about;
    private TextView mTv_version;
    private int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_about);
        mTv_version = (TextView) findViewById(R.id.tv_version);
        mIv_about = (ImageView) findViewById(R.id.iv_about);
        mTv_title = (TextView) findViewById(R.id.title_center);
        mIv_back = (ImageView) findViewById(R.id.title_left);
        mIv_back.setVisibility(View.VISIBLE);


        mTv_title.setText(getString(R.string.title_about));
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            mTv_version.setText("V" + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mIv_back.setOnClickListener(this);
        mIv_about.setOnClickListener(this);
        mCount = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_left:
                finish();
                break;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("AboutActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("AboutActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }
}
