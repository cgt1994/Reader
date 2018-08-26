package com.syezon.reader.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.wifiTransfer.CheckHandler;
import com.syezon.reader.wifiTransfer.GetCssHandler;
import com.syezon.reader.wifiTransfer.GetImgHandler;
import com.syezon.reader.wifiTransfer.GetJsHandler;
import com.syezon.reader.wifiTransfer.ServerConnHandler;
import com.syezon.reader.wifiTransfer.UploadHandler;
import com.syezon.reader.wifiTransfer.UserInfoHandler;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.AndServerBuild;

/**
 * Created by jin on 2016/9/13.
 */
public class WiFiTransferService extends Service {

    private IBinder mWiFiBinder;
    private AndServer andServer;

    public interface IServerListener {
        void serverReady(boolean isReady);
    }

    private IServerListener mListener;

    public void setServerListener(IServerListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mWiFiBinder = new WiFiBinder();

        return mWiFiBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void initServer() {
        String serverIP = getServerIP() + ":4477";
//        Log.e("TAG", "serverIP:" + serverIP);
        if (serverIP == null || "-1:4477".equals(serverIP)) {
            mListener.serverReady(false);
            return;
        }
        if (!serverIP.equals(SPHelper.getServerIP(this))) {
            SPHelper.setServerIP(this, serverIP);
        }

        AndServerBuild andServerBuild = AndServerBuild.create();

        andServerBuild.setPort(4477);// 指定http端口号。
        // 注册接口。
        andServerBuild.add("", new ServerConnHandler());
        andServerBuild.add("index.css", new GetCssHandler());
        andServerBuild.add("all-min.js", new GetJsHandler());
        andServerBuild.add("bg.jpg", new GetImgHandler("bg.jpg"));
        andServerBuild.add("head_bg.jpg", new GetImgHandler("head_bg.jpg"));
        andServerBuild.add("logo_2.png", new GetImgHandler("logo_2.png"));
        andServerBuild.add("logo_1.png", new GetImgHandler("logo_1.png"));
        andServerBuild.add("btn.png", new GetImgHandler("btn.png"));
        andServerBuild.add("bg_2.jpg", new GetImgHandler("bg_2.jpg"));
        andServerBuild.add("getName", new UserInfoHandler(this));
        andServerBuild.add("upload", new UploadHandler(this.getApplicationContext()));
        andServerBuild.add("check", new CheckHandler(this));

        // 启动服务器。
        andServer = andServerBuild.build();
        andServer.launch();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (SPHelper.getUploadIndex(WiFiTransferService.this).equals("-1")) {

                }
                mListener.serverReady(true);
            }
        }).start();
    }

    //获取本机的ip地址
    private String getServerIP() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo.State state = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (NetworkInfo.State.DISCONNECTING == state || NetworkInfo.State.DISCONNECTED == state) {//WiFi未连接
            return "-1";
        }

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = formatIpAddress(ipAddress);
        SPHelper.setWiFiName(this, wifiInfo.getSSID());
        return ip;
    }

    private static String formatIpAddress(int ipAdress) {
        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (andServer != null && andServer.isRunning()) {
            andServer.close();
        }
    }

    public class WiFiBinder extends Binder {
        public WiFiTransferService getService() {
            return WiFiTransferService.this;
        }
    }
}
