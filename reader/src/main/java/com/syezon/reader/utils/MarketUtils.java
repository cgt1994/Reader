package com.syezon.reader.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.umeng.analytics.AnalyticsConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用市场工具类
 */
public class MarketUtils {


    /**
     * 根据友盟渠道号自动跳转相应的应用市场详情页
     */
    public static void launchAppDetail2(Context context) {
//        Log.e("qwe", "23213" + getMarketPkg(context));
//        Toast.makeText(context, getMarketPkg(context), Toast.LENGTH_SHORT).show();
        try {

            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(getMarketPkg(context));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Couldn't launch the market !", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据渠道号获取各渠道对应应用市场的包名
     */
    private static String getMarketPkg(Context context) {
        String channel = "";
        switch (AnalyticsConfig.getChannel(context)) {
            case "tencent":
                channel = "com.tencent.android.qqdownloader";
                break;
            case "baidu":
                channel = "com.baidu.appsearch";
                break;
            case "qihu":
                channel = "com.qihoo.appstore";
                break;
            case "xiaomi":
                channel = "com.xiaomi.market";
                break;
            case "huawei":
                channel = "com.huawei.appmarket";
                break;
            case "sogou":
                channel = "com.sogou.appmall";
                break;
            case "lenovo":
                channel = "com.lenovo.leos.appstore";
                break;
            case "anzhi":
                channel = "cn.goapk.market";
                break;
            case "wandoujia":
                channel = "com.wandoujia.phoenix2";
                break;
            case "oppo":
                channel = "com.oppo.market";
                break;
            case "vivo":
                channel = "com.bbk.appstore";
                break;
            case "offical":
                channel = chooseChannelAutomatically(context);
                break;
            default:
                channel = chooseChannelAutomatically(context);
                break;
        }
        return channel;
    }

    /**
     * 自动选取评论的平台市场
     */
    private static String chooseChannelAutomatically(Context context) {
        String brand = android.os.Build.BRAND;// 手机品牌
        if (TextUtils.equals(brand, "Huawei")) {
            return "com.huawei.appmarket";
        }

        if (TextUtils.equals(brand, "Xiaomi")) {
            return "com.xiaomi.market";
        }

        ArrayList<String> installedMarketPkgs = queryInstalledMarketPkgs(context);
        if (installedMarketPkgs.contains("com.tencent.android.qqdownloader")) {
            return "com.tencent.android.qqdownloader";
        }
        if (installedMarketPkgs.contains("com.qihoo.appstore")) {
            return "com.qihoo.appstore";
        }
        if (installedMarketPkgs.contains("com.baidu.appsearch")) {
            return "com.baidu.appsearch";
        }
        if (installedMarketPkgs.contains("com.wandoujia.phoenix2")) {
            return "com.wandoujia.phoenix2";
        }

        return null;
    }

    /**
     * 获取已安装应用商店的包名列表
     */
    public static ArrayList<String> queryInstalledMarketPkgs(Context context) {
        ArrayList<String> pkgs = new ArrayList<>();
        if (context == null)
            return pkgs;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.APP_MARKET");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        if (infos == null || infos.size() == 0)
            return pkgs;
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            String pkgName = "";
            try {
                ActivityInfo activityInfo = infos.get(i).activityInfo;
                pkgName = activityInfo.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(pkgName))
                pkgs.add(pkgName);

        }
        return pkgs;
    }


}
