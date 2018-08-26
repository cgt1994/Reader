package com.syezon.reader.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.syezon.reader.R;
import com.syezon.reader.service.WiFiTransferService;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.Tools;
import com.umeng.analytics.MobclickAgent;

/**
 * wifi传书界面
 * Created by jin on 2016/9/14.
 */
public class WiFiTransferActivity extends Activity implements WiFiTransferService.IServerListener {

    private static final String TAG = WiFiTransferActivity.class.getSimpleName();
    public static final String USER_CONNECTED = "user_connected";
    public static final String UPLOAD_PROGRESS = "upload_progress";
    private static final int WIFI_OPEN = 1;
    private static final int WIFI_CLOSE = 2;

    private TextView mTv_title;
    private ImageView mIv_back;
    private TextView mTv_serverIP;
    private TextView mTv_wifi_status;
    private TextView mTv_wifi_tip;
    private RelativeLayout mRL_not_connect;
    private RelativeLayout mRL_connected;
    private ImageView mIv_transfer;
    private ProgressBar mPb_transfer;

    private UserConnectedReceiver mUserConnectedReceiver;

    //用户连接的广播接收
    public class UserConnectedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mRL_connected.setVisibility(View.VISIBLE);
            mRL_not_connect.setVisibility(View.GONE);
        }
    }

    private UploadProgressReceiver mProgressReceiver;

    //用户上传书籍进度广播
    public class UploadProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIv_transfer.setVisibility(View.INVISIBLE);
            mPb_transfer.setVisibility(View.VISIBLE);
            int progress = intent.getIntExtra("progress", 0);
            mPb_transfer.setProgress(progress);
            if (progress >= 100) {
                mIv_transfer.setVisibility(View.VISIBLE);
                mPb_transfer.setVisibility(View.GONE);
                Toast.makeText(WiFiTransferActivity.this, getString(R.string.transfer_success), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private WiFiTransferService mService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((WiFiTransferService.WiFiBinder) service).getService();
            mService.setServerListener(WiFiTransferActivity.this);
            mService.initServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WIFI_OPEN:
                    mTv_serverIP.setText("http://" + SPHelper.getServerIP(WiFiTransferActivity.this));
                    mTv_wifi_status.setText(getString(R.string.wifi_open));
                    String str = getString(R.string.wifi_tip);
                    mTv_wifi_tip.setText(String.format(str, SPHelper.getWiFiName(WiFiTransferActivity.this)));
                    break;
                case WIFI_CLOSE:
                    mTv_wifi_status.setText(getString(R.string.wifi_close));
                    Toast.makeText(WiFiTransferActivity.this, getString(R.string.wifi_close_tip), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.setStatusBarColor(this, R.color.title_bg);
        setContentView(R.layout.activity_wifi);
        initView();
        initService();
        initRegister();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
    }

    //注册广播
    private void initRegister() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USER_CONNECTED);
        mUserConnectedReceiver = new UserConnectedReceiver();
        registerReceiver(mUserConnectedReceiver, filter);

        IntentFilter progressFilter = new IntentFilter();
        progressFilter.addAction(UPLOAD_PROGRESS);
        mProgressReceiver = new UploadProgressReceiver();
        registerReceiver(mProgressReceiver, progressFilter);
    }

    private void initService() {
        Intent intent = new Intent(this, WiFiTransferService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        mTv_title = (TextView) findViewById(R.id.title_center);
        mIv_back = (ImageView) findViewById(R.id.title_left);
        mTv_serverIP = (TextView) findViewById(R.id.wifi_ip);
        mTv_wifi_status = (TextView) findViewById(R.id.wifi_status);
        mTv_wifi_tip = (TextView) findViewById(R.id.wifi_tip);
        mRL_not_connect = (RelativeLayout) findViewById(R.id.rl_not_connect);
        mRL_connected = (RelativeLayout) findViewById(R.id.rl_connected);
        mIv_transfer = (ImageView) findViewById(R.id.iv_transfer);
        mPb_transfer = (ProgressBar) findViewById(R.id.pb_transfer);

        mIv_back.setVisibility(View.VISIBLE);
        mIv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTv_title.setText(getString(R.string.title_wifi));
    }

    @Override
    public void serverReady(boolean isReady) {
        Message msg = Message.obtain();
        if (isReady) {
            msg.what = WIFI_OPEN;
        } else {
            msg.what = WIFI_CLOSE;
        }
        mHandler.sendMessage(msg);
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("SplashScreen"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("SplashScreen"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        unregisterReceiver(mUserConnectedReceiver);
        unregisterReceiver(mProgressReceiver);
    }

}
