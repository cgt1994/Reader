package com.syezon.reader.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYAdListener;
import com.iflytek.voiceads.IFLYAdSize;
import com.iflytek.voiceads.IFLYInterstitialAd;
import com.iflytek.voiceads.IFLYNativeAd;
import com.iflytek.voiceads.IFLYNativeListener;
import com.iflytek.voiceads.IFLYVideoAd;
import com.iflytek.voiceads.IFLYVideoAdListener;
import com.iflytek.voiceads.NativeADDataRef;

import java.util.List;

/**
 * 广告工具类
 * Created by jin on 2016/10/19.
 */
public class ADUtils {

    private static final String InterstitialAdID = "F7AF17BF3AFE0CD1149D5469470833EC";//插屏广告位id
    private static final String VideoAdID = "AB5D5898C12960250B170957F57B9747";//视屏广告位id
    public static String NativeAdID = "173A9E463737EA7942BAEF72509536D8";//原生广告位信息流id

    //插屏广告的监听回调
    public interface InterstitialADListener {
        //插屏加载
        void onLoadInterstitialAD(IFLYInterstitialAd interstitialAd);

        //关闭插屏
        void onCloseInterstitialAD();

        //点击插屏
        void onClickInterstitialAD();
    }


    //原生广告监听回调
    public interface INativeADListener {
        //原生加载
        void onLoadNativeAD(NativeADDataRef adItem);

        //返回原生广告对象，用于点击上传位置的时候使用
        void getNativeAdObj(IFLYNativeAd nativeAd);
    }

    //视屏广告监听回调
    public interface IVideoADListener {
        //返回视屏广告对象
        void getVideoAdObj(IFLYVideoAd videoAD);

        //加载视屏广告成功
        void onLoadVideoAd();

        //关闭视屏广告
        void onCloseVideoAD();

        //点击视屏广告
        void onClickVideoAD();
    }
//    @SuppressLint("NewApi")
//    private void setFullScreen(boolean enable) {
//        if (enable) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        } else {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//    }


    //显示插屏广告
    public static void showInterstitialAD(Context context, final InterstitialADListener listener) {
        //创建插屏广告
        final IFLYInterstitialAd interstitialAd = IFLYInterstitialAd.createInterstitialAd(context, InterstitialAdID);
        //设置广告尺寸
//        interstitialAd.setAdSize(IFLYAdSize.FULLSCREEN);
        interstitialAd.setAdSize(IFLYAdSize.INTERSTITIAL);
//        Log.e("size",SConfig.SCREEN_WIDTH+" "+SConfig.SCREEN_HEIGHT);
//        interstitialAd.setAdSize(new IFLYAdSize(SConfig.SCREEN_WIDTH,SConfig.SCREEN_HEIGHT));
        //设置下载广告前，弹窗提示
        interstitialAd.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
        //请求广告，添加监听器
        interstitialAd.loadAd(new IFLYAdListener() {
            @Override
            public void onAdReceive() {
                Log.e("TAG", "get interstitial ad success");
                listener.onLoadInterstitialAD(interstitialAd);
            }

            @Override
            public void onAdFailed(AdError adError) {
                // Log.e("TAG", "get interstitial ad failed");
            }

            @Override
            public void onAdClick() {
                listener.onClickInterstitialAD();
            }

            @Override
            public void onAdClose() {
                listener.onCloseInterstitialAD();
            }

            @Override
            public void onAdExposure() {

            }

            @Override
            public void onConfirm() {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    //显示原生广告
    public static void showNativeAD(Context context, final INativeADListener listener) {
//        if (context instanceof WelcomeActivity){
//            Log.e("tag","来自welcom");
//            NativeAdID="30C07FC437ED93DD203EDCA0A8FAD082";
//        }else {
        NativeAdID = "173A9E463737EA7942BAEF72509536D8";
//        }

        IFLYNativeAd nativeAd = new IFLYNativeAd(context, NativeAdID, new IFLYNativeListener() {
            @Override
            public void onADLoaded(List<NativeADDataRef> list) {
                if (list.size() > 0) {
                    Log.e("zxc", "get native ad success");
                    listener.onLoadNativeAD(list.get(0));
                }
            }

            @Override
            public void onAdFailed(AdError adError) {
                Log.e("zxc", "get native ad failed"+adError.getErrorCode());
            }

            @Override
            public void onConfirm() {

            }

            @Override
            public void onCancel() {

            }
        });

        listener.getNativeAdObj(nativeAd);
        nativeAd.loadAd(1);//加载一条
    }

    //显示视屏广告
    public static void showVideoAD(Context context, RelativeLayout adContainer, View progress, final IVideoADListener listener) {
        IFLYVideoAd videoAd = IFLYVideoAd.createVideoAd(context, VideoAdID, new IFLYVideoAdListener() {
            @Override
            public void onAdStartPlay() {

            }

            @Override
            public void onAdPlayComplete() {

            }

            @Override
            public void onAdPlayProgress(int i, int i1) {

            }

            @Override
            public void onAdReceive() {
                //Log.e("TAG","get video ad success");
                listener.onLoadVideoAd();
            }

            @Override
            public void onAdFailed(AdError adError) {
                // Log.e("TAG","get video ad failed:"+adError.getErrorCode());
            }

            @Override
            public void onAdClick() {
                listener.onClickVideoAD();
            }

            @Override
            public void onAdClose() {
                listener.onCloseVideoAD();
            }

            @Override
            public void onAdExposure() {

            }

            @Override
            public void onConfirm() {

            }

            @Override
            public void onCancel() {

            }
        }, true);
        videoAd.addProgressBar(progress);
        videoAd.setParameter(AdKeys.DOWNLOAD_ALERT, "true");
        adContainer.removeAllViews();
        adContainer.addView(videoAd);
        videoAd.setAdSize(1920, 1080);
        videoAd.loadAd();
        listener.getVideoAdObj(videoAd);
    }

}
