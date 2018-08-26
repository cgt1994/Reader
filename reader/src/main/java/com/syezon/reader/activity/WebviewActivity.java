package com.syezon.reader.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.syezon.reader.R;
import com.syezon.reader.service.DownloadApkService;
import com.umeng.analytics.MobclickAgent;

public class WebviewActivity extends Activity {
    private WebView wel_web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        wel_web = (WebView) findViewById(R.id.wel_web);

        setListener();
        init();
    }

    private void init() {
        String url = getIntent().getStringExtra("weburl");
        Log.e("print", url);
        wel_web.setDownloadListener(new MyWebViewDownLoadListener());

        wel_web.getSettings().setJavaScriptEnabled(true);
        wel_web.loadUrl(url);
//        wel_web.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
//                view.loadUrl(url);
//
//                return true;
//            }
//        });

    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
//            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                Toast t = Toast.makeText(WebviewActivity.this, "需要SD卡。", Toast.LENGTH_LONG);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
//                return;
//            }
//            DownloaderTask task = new DownloaderTask();
//            task.execute(url);
            Intent intent = new Intent(WebviewActivity.this, DownloadApkService.class);
            intent.putExtra("URL", url);
            Log.e("downloadurl", url);
            startService(intent);
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;

            Intent intent = new Intent(WebviewActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

//            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setListener() {
        wel_web.setWebViewClient(new WebViewClient() {
            //网页加载开始时调用，显示加载提示旋转进度条

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("shouldOverrideUrlLoading", url);
                // 处理自定义scheme
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                }
                return true;
//                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);

            }

            //网页加载完成时调用，隐藏加载提示旋转进度条
            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
//                progressBar.setVisibility(View.GONE);
            }

            //网页加载失败时调用，隐藏加载提示旋转进度条
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
//                progressBar.setVisibility(View.GONE);
            }

        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("WebviewActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("WebviewActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }
}
