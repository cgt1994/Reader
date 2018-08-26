package com.syezon.reader.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.syezon.reader.R;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * 常用工具类
 * Created by Fang on 2016/7/26.
 */
public class Tools {
    public static void setStatusBarColor(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(activity, true);
        }
        //为状态栏着色
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setStatusBarTintResource(colorId);
    }

    @TargetApi(19)
    private static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }
    //格式化日期 yyyy-MM-dd
    public static String formatDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(time));
    }

    //显示加载框
    public static ProgressDialog showProgressDialog(Context context) {
        Log.e("diss", "show");

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
        dialog.setContentView(R.layout.view_loading);
        return dialog;
    }

    //关闭加载框
    public static void closeProgressDialog(ProgressDialog dialog) {
        dialog.dismiss();
    }

    //获取版本号
    public static String getVersion(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 版本号比较
     *
     * @param str1 版本号1.
     * @param str2 版本号2
     * @return 0=相等，-1=版本号1小于版本号2，1=版本号1大于版本号2
     */
    public static int versionCompare(String str1, String str2) {
        if (str1 == null || str1.isEmpty()) {
            //版本号为空，可能是版本比较老，没送上来，所以认为str1比较老
            return -1;
        }
        if (str2 == null || str2.isEmpty()) {
            //版本号为空，可能是版本比较老，没送上来，所以认为str2比较老
            return 1;
        }

        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int v1 = Integer.valueOf(vals1[i]);
            int v2 = Integer.valueOf(vals2[i]);
            if (v1 < v2) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (vals1.length == vals2.length) {
                return 0;
            } else if (vals1.length > vals2.length) {
                boolean allZero = true;
                for (int j = vals2.length; j < vals1.length; j++) {
                    int v1 = Integer.valueOf(vals1[j]);
                    if (v1 != 0) {
                        allZero = false;
                        break;
                    }
                }
                return allZero ? 0 : 1;
            } else {
                boolean allZero = true;
                for (int j = vals1.length; j < vals2.length; j++) {
                    int v2 = Integer.valueOf(vals2[j]);
                    if (v2 != 0) {
                        allZero = false;
                        break;
                    }
                }
                return allZero ? 0 : -1;
            }
        }
    }


    //将字符半角化,遇到标点符号不会换行
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    //dp转为px
    public static int dp2px(Context context, float dpValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    //sp转为px
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    //获取手机型号信息
    public static String getPhoneInfo() {
        String phoneType = Build.MODEL;
        return phoneType;
    }

    /**
     * 获取两个值范围内的随机值
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandom(int min, int max) {
        if (max > min) {
            return min + new Random().nextInt(max - min + 1);
        } else if (max < min) {
            return max + new Random().nextInt(min - max + 1);
        } else {
            return min;
        }
    }

    //构建json字符串上传参数
    public static String Map2Json(Map<String, Object> map) {
        StringBuffer params = new StringBuffer();
        params.append("{ \"");
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry e = (Map.Entry) iterator.next();
            params.append(e.getKey()).append("\":");
            Object object = e.getValue();
            if (object instanceof String) {
                params.append("\"").append(e.getValue()).append("\",\"");
            } else {
                params.append(e.getValue()).append(",\"");
            }
        }
        params.delete(params.lastIndexOf(","), params.length());
        params.append("}");
        return params.toString();
    }

    //判断小说类型
    public static String judgeNovelType(int type) {
        String result = "";
        switch (type) {
            case 1:
                result = "玄幻";
                break;
            case 2:
                result = "修真";
                break;
            case 3:
                result = "都市";
                break;
            case 4:
                result = "历史";
                break;
            case 5:
                result = "侦探";
                break;
            case 6:
                result = "网游";
                break;
            case 7:
                result = "科幻";
                break;
            case 8:
                result = "恐怖";
                break;
            case 9:
                result = "散文";
                break;
            case 10:
                result = "其他";
                break;
        }
        return result;
    }
}
