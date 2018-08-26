package com.syezon.reader.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * 全局配置
 *
 * @author YL
 */
public class SConfig {

    private static String TAG = SConfig.class.getSimpleName();

    // 设备信息
    public static int SDK_VERSION_CODE; // 设备系统版本号
    public static String SDK_VERSION_NAME; // 设备系统版本名
    public static String DEVICE_MODEL; // 手机型号
    public static String MANUFACTURER; // 手机厂商
    public static String IMEI; // 国际移动设备身份码(手机硬件号)
    public static String IMSI; // 国际移动用户识别码(手机卡中的串号)
    public static String OPERATOR; // 运营商
    public static String MAC; // 设备MAC地址

    // 屏幕信息
    public static int SCREEN_WIDTH; // 屏幕宽度（像数px）
    public static int SCREEN_HEIGHT; // 屏幕高度（像数px）
    public static int SCREEN_STATUS_HEIGHT; // 状态栏高度（像数px）
    public static double SCREEN_ASPECT_RATIO; // 高宽比
    public static int SCREEN_DENSITY_DPI; // 屏幕密度
    public static double SCREEN_SCALE; // 缩放比例（dp * 缩放比例 = px）

    // 应用信息
    public static int VERSION_CODE; // APP版本号
    public static String VERSION_NAME; // APP版本名
    public static String PACKAGE_NAME; // 包名
    public static long FIRST_INSTALL_TIME; // 初次安装时间

    /**
     * 获取全局配置
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static synchronized void init(Context context) {
        // 判断是否需要重新获取
        if (SCREEN_WIDTH > 0 && SCREEN_HEIGHT > 0) {
            return;
        }

        // 获取设备信息
        SDK_VERSION_CODE = Build.VERSION.SDK_INT;
        SDK_VERSION_NAME = Build.VERSION.RELEASE;
        DEVICE_MODEL = Build.MODEL;
        MANUFACTURER = Build.MANUFACTURER;
        Log.e("phoneinfo", DEVICE_MODEL + "//" + MANUFACTURER);
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = tm.getDeviceId();
        if (IMEI == null) {
            IMEI = "novel" + Tools.getRandom(1, 9998) + Tools.getRandom(1, 9998) + Tools.getRandom(1, 9998);
        }
        IMSI = tm.getSubscriberId();
        if (IMSI == null) {
            IMSI = "novel" + Tools.getRandom(1, 9998) + Tools.getRandom(1, 9998) + Tools.getRandom(1, 9998);
        }
        OPERATOR = "";
        String simOperator = tm.getSimOperator();
        if (simOperator != null) {
            if (simOperator.equals("46000") || simOperator.equals("46002")) {
                OPERATOR = "中国移动";
            } else if (simOperator.equals("46001")) {
                OPERATOR = "中国联通";
            } else if (simOperator.equals("46003")) {
                OPERATOR = "中国电信";
            }
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        MAC = wifi.getConnectionInfo().getMacAddress();

        // 获取屏幕信息
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;
        SCREEN_DENSITY_DPI = dm.densityDpi;

        SCREEN_SCALE = dm.scaledDensity;
        Log.e("dpi",SCREEN_SCALE+""+SCREEN_DENSITY_DPI);
        double dH = (double) dm.heightPixels;
        double dW = (double) dm.widthPixels;
        SCREEN_ASPECT_RATIO = dH / dW;
        SCREEN_STATUS_HEIGHT = getStatusHeight(context);

        // 获取应用信息
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        PACKAGE_NAME = context.getPackageName();
        try {
            pi = pm.getPackageInfo(PACKAGE_NAME, 0);
            VERSION_CODE = pi.versionCode;
            VERSION_NAME = pi.versionName;
            FIRST_INSTALL_TIME = pi.firstInstallTime;
            long useTime = System.currentTimeMillis() - FIRST_INSTALL_TIME;
            int USE_H = (int) (useTime / 1000 / 60 / 60);
            int USE_M = (int) (useTime / 1000 / 60 % 60);
            int USE_S = (int) (useTime / 1000 % 60);
        } catch (Exception e) {
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return > 0 success; <= 0 fail
     */
    public static int getStatusHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
        }
        return statusHeight;
    }
}
