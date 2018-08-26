package com.example.downloadutil;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;


/**
 * 控制下载apk时的进度通知
 */
public class DownloadNotificationControl {
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private String iconUrl;
    private String destinationPath;
    private final int NOTIFICATION_ID = 1234;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());

    public NotificationManager getmNotificationManager() {
        return mNotificationManager;
    }

    public DownloadNotificationControl(Context context, String iconUrl, String destinationPath) {
        this.iconUrl = iconUrl;
        this.destinationPath = destinationPath;
        this.context = context;
        initNotification();
    }


    private void initNotification() {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context)
                        .load(iconUrl)
                        .asBitmap()
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                mBuilder.setLargeIcon(resource);
                            }
                        });
            }
        });
    }


    public void showProgressNotification() {
        mBuilder.setContentTitle("等待下载")
                .setWhen(System.currentTimeMillis())
                .setTicker("开始下载")// 通知首次出现在通知栏，带上升动画效果的
                .setProgress(100, 0, false); // 这个方法是显示进度条 设置为true就是不确定的那种进度条效果
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
    public static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
      /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
        /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        }
//      else if(end.equals("pptx")||end.equals("ppt")){
//        type = "application/vnd.ms-powerpoint";
//      }else if(end.equals("docx")||end.equals("doc")){
//        type = "application/vnd.ms-word";
//      }else if(end.equals("xlsx")||end.equals("xls")){
//        type = "application/vnd.ms-excel";
//      }
        else {
//        /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }
    public static Intent getFileIntent(File file) {
//       Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type=" + type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }
    public static void openApk(File file, Context context) {
        if (file == null) {
            return;
        }
        Intent intent = getFileIntent(file);


        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 设置下载进度
     */
    public void updateProgress(int progress) {
        if (progress % 10 != 0) {
            return;
        }
        mBuilder
//                .setWhen(System.currentTimeMillis())
                .setProgress(100, progress, false) // 这个方法是显示进度条
                .setContentTitle("   下载中...");
        if (progress >= 100) {
            mBuilder.setTicker("下载完成")
                    .setContentTitle("   完成...");
            Notification mNotification = mBuilder.build();
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
            mNotificationManager.cancel(NOTIFICATION_ID);

            openApk(new File(destinationPath), context);
            return;
        }
        Notification mNotification = mBuilder.build();
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

}
