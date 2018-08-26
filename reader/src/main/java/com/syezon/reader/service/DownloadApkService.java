package com.syezon.reader.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.syezon.reader.utils.DownloadNotificationControl;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.ToastUtil;
import com.thin.downloadmanager.DownloadManager;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListener;
import com.thin.downloadmanager.ThinDownloadManager;

import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DownloadApkService extends IntentService {
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 4;// 下载的线程数

    private MyDownloadListener myDownloadStatusListener = new MyDownloadListener();

    private DownloadNotificationControl downloadNotificationControl;

    private Context context;
    private int DOWNLOAD_ID = 0;
    private long lastTime;


    public DownloadApkService() {
        super("DownloadApkService");
    }

    @Override
    public void onCreate() {
        context = this;
        super.onCreate();
    }

    /**
     * 判断应用是否已经被安装
     */
    public static boolean isAppInstalled(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
        for (PackageInfo pkg : packageInfoList) {
            ApplicationInfo info = pkg.applicationInfo;
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                if (TextUtils.equals(pkg.packageName, pkgName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    private void showToastByRunnable(final IntentService context, final CharSequence text, final int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String pkgName = intent.getStringExtra("PKG");
        long currentTime = System.currentTimeMillis();
//        Log.d("=====", ""+isStop(context,currentTime));
        if (isStop(context, currentTime)) {
            return;//防止误点下载按钮
        } else {
            if (isAppInstalled(context, pkgName)) {
                ToastUtil.showToast(getApplicationContext(), "该应用已经存在", Toast.LENGTH_SHORT);
                showToastByRunnable((IntentService) context, "该应用已经存在", Toast.LENGTH_SHORT);
//                Toast.makeText(context, "该应用已经存在", Toast.LENGTH_SHORT).show();
                return;//手机上存在该应用就不下载安装了
            }
            SPHelper.saveLongData(context, currentTime);
        }
        String apkName = intent.getStringExtra("NAME");
        String url = intent.getStringExtra("URL");
        String imgUrl = intent.getStringExtra("ICON");
        Log.e("TAG", pkgName + " " + apkName + " " + url + " " + imgUrl);
        String destinationPath = getDiskCacheDir(context) + "/" + apkName + ".apk";
        downloadNotificationControl = new DownloadNotificationControl(context, imgUrl, destinationPath);/////////注意
        ThinDownloadManager downloadManager = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);
        Uri downloadUri = Uri.parse(url);
        Uri destinationUri = Uri.parse(destinationPath);
        DownloadRequest request = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri)
                .setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadListener(myDownloadStatusListener);
        if (downloadManager.query(DOWNLOAD_ID) == DownloadManager.STATUS_NOT_FOUND) {
            DOWNLOAD_ID = downloadManager.add(request);
        }
        downloadNotificationControl.showProgressNotification();
    }

    /**
     * 判断安装间隔是否合理
     */
    private static boolean isStop(Context context, long currentTime) {
        long lastSavedTime = SPHelper.getLongData(context);
        long distanceTime = currentTime - lastSavedTime;
        return (distanceTime / 1000) < 10;
    }

    private class MyDownloadListener implements DownloadStatusListener {

        @Override
        public void onDownloadComplete(int id) {
//            if (id == DOWNLOAD_ID) {
//                FileUtils.openApk(new File(destinationPath),context);
//                downloadNotificationControl.getmNotificationManager().cancelAll();
//            }
        }

        @Override
        public void onDownloadFailed(int id, int errorCode, String errorMessage) {
            if (id == DOWNLOAD_ID) {
                downloadNotificationControl.getmNotificationManager().cancel(1234);
            }
        }

        @Override
        public void onProgress(int id, long totalBytes, long downloadedBytes, int progress) {
            if (id == DOWNLOAD_ID) {
                downloadNotificationControl.updateProgress(progress);
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("onstartcommand", "service");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
