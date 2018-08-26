package com.syezon.reader.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.syezon.reader.R;
import com.syezon.reader.constant.Constant;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.utils.BrightnessUtils;
import com.syezon.reader.utils.DividePagesUtil;
import com.syezon.reader.utils.FileUtils;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.MD5Util;
import com.syezon.reader.utils.PermissionsChecker;
import com.syezon.reader.utils.SConfig;
import com.syezon.reader.utils.SPHelper;
import com.syezon.reader.utils.Tools;
import com.syezon.reader.view.AutoScrollTextView;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.syezon.reader.constant.Constant.UPDATE_URL;

/**
 * 开屏页
 * Created by jin on 2016/10/20.
 */
public class WelcomeActivity extends Activity {
    private static final int REAUEST_WRITE_PREMISSION = 1;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    private static final int START_COUNT = 3;

    private Animation apphaAnimation;
    private ImageView img_ad;
    private String picLoadUrl;
    private TextView count_tv;
    private TextView time_tv;
    private ImageView mImg_welcome;
    private AutoScrollTextView text_switcher;
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private RelativeLayout rl_bottom;
    private LinearLayout ll_icon, ll_count;
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private static final int REQUEST_CODE = 0; // 请求码
    private String weburl;
    private int i = 5;
    private ArrayList<String> titleList = new ArrayList<String>() {
    };
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mPermissionsChecker = new PermissionsChecker(this);
        initView();
        initad();
        getToken();//获取token
        initAdSwitch();//获取广告开关
        checkNovelUpdate();//检查更新
        for (int i = 5; i > 0; i--) {

            titleList.add(i + " ");
        }

        text_switcher.setTextList(titleList);
//        if (SPHelper.getNativeADIsOpen(this))
//            ADUtils.showNativeAD(this, this);
//        handler.sendEmptyMessage(START_COUNT);
    }

    private void initad() {
        time_tv = (TextView) findViewById(R.id.time_tv);
        img_ad = (ImageView) findViewById(R.id.img_ad);
        count_tv = (TextView) findViewById(R.id.count_tv);
        text_switcher = (AutoScrollTextView) findViewById(R.id.text_switcher);
    }

    private void initView() {
//        rl_bottom = (RelativeLayout)findViewById(R.id.rl_bottom);
        mImg_welcome = (ImageView) findViewById(R.id.img_welcome);
        apphaAnimation = AnimationUtils.loadAnimation(this, R.anim.welcome_alpha);
        apphaAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                //copyAssets();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_enter, R.anim.anim_exit);
                WelcomeActivity.this.finish();
            }
        });
//        Log.e("hasper", hasPermission() + " ");

        readViewConfig();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(WelcomeActivity.this)) {
                    getBaseInfo();
                }
            }
        }
//        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
//            finish();
//        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("WelcomeActivity"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
        text_switcher.startAutoScroll();
        ll_count = (LinearLayout) findViewById(R.id.ll_count);
        ll_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(0);
            }
        });
        handler.sendEmptyMessageDelayed(START_COUNT, 5000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(WelcomeActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + WelcomeActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
                Toast.makeText(WelcomeActivity.this, "请打开所需系统权限", Toast.LENGTH_SHORT).show();
            } else {
                getBaseInfo();
            }
        } else {
            getBaseInfo();
        }

        if (hasPermission()) {
            deleteHistory();
            taskHasPermission();
        }
        readViewConfig();
//        Log.e("permission", mPermissionsChecker.lacksPermission(Manifest.permission.WRITE_SETTINGS) + " ");
//
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }


    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }


    //获取一些手机基本信息,IMSI和手机目前亮度值等
    private void getBaseInfo() {
        SConfig.init(this);
        //获取当前手机屏幕亮度
        if (SPHelper.getReadBrightness(this) == 50) {
            SPHelper.setReadBrightness(this, BrightnessUtils.getScreenBrightness(this));
            Log.e("bright", BrightnessUtils.getScreenBrightness(this) + " ");
        }
    }


    private void deleteHistory() {
        if (!SPHelper.getClearHistory(this)) {
            SPHelper.setClearHistory(this, true);
            String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/com.syezon.reader/book";
            FileUtils.deleteDirectory(dirName);
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission == PackageManager.PERMISSION_GRANTED) {//has permission
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REAUEST_WRITE_PREMISSION);
                return false;
            }
        } else {
            return true;
        }
    }

    private void taskHasPermission() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                copyAssets();
            }
        }).start();
    }

    //计算一页能显示的行数和字数
    private void readViewConfig() {
        DividePagesUtil util = DividePagesUtil.getInstance(this);
        InfoUtils infoUtils = InfoUtils.getInstance(this);
        util.getCurViewInfo(0);
    }

    //将assets文件下内容复制到sd卡中,只执行一次
    private void copyAssets() {

        if (SPHelper.getUploadIndex(this).equals("-1")) {
            String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/com.syezon.reader/";
            //Log.e("TAG", "copy assets file");
            //将服务器地址加入到文件中
            FileUtils.copyAssetsFile(this, "upload", dirName + "upload");
        }
        if (!SPHelper.getFileIsCopy(this)) {
            FileUtils.copyBook2SDCard(this, "book");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        text_switcher.stopAutoScroll();
        handler.removeCallbacksAndMessages(null);
    }

    //获取用户的token值
    private void getToken() {
        String cid = "";
        if (SConfig.IMSI != null) {
            cid = SConfig.IMSI;
        } else if (SConfig.IMEI != null) {
            cid = SConfig.IMEI;
        } else {
            cid = SConfig.MAC;
        }
        long tms = System.currentTimeMillis();
        String secret = "SDHH%aa@xc$cllL244DDkjmwLKd0w8kc";
        String sign = MD5Util.encrypt(cid + tms + secret);
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tms", tms);
        params.put("sign", sign);
        OkHttpUtils.postString().url(APIDefine.GET_TOKEN).
                content(Tools.Map2Json(params)).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e("TAG", "error:" + e);
            }

            @Override
            public void onResponse(String response, int id) {
                try {
                    JSONObject root = new JSONObject(response);
                    int rc = root.getInt("rc");
                    if (1 == rc) {
                        String token = root.getString("token");
                        SPHelper.setUserToken(WelcomeActivity.this, token);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initAdSwitch() {
        InfoUtils.RANDOM = (int) (Math.random() * 10 + 1);
        OkHttpUtils.get().url(APIDefine.AD_SWITCH).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                setAdSwitch(true);
            }

            @Override
            public void onResponse(String response, int id) {
                // Log.e("TAG", "get ad switch response:" + response);
                try {
                    JSONObject root = new JSONObject(response);
                    JSONArray slpashInfo = root.getJSONArray("splash");

                    String showrate = root.optString("showrate");
                    Float rate = Float.parseFloat(showrate);

                    SPHelper.setRate(WelcomeActivity.this, rate);

//                    生成随机数
                    int k = (int) (Math.random() * slpashInfo.length());
//                    for (int i = 0; i < slpashInfo.length(); i++) {
//
//                        JSONObject pic = slpashInfo.getJSONObject(i);
//                        String picurl = pic.getString("pic");
//                        Log.e("pic", picurl);
//                    }
                    JSONArray showJson = root.optJSONArray("show");

                    Log.e("showj", showJson + "");
                    if (showJson != null) {
                        InfoUtils.showPic = showJson;

                    }

                    picLoadUrl = (String) slpashInfo.getJSONObject(k).get("pic");
                    weburl = (String) slpashInfo.getJSONObject(k).get("url");
                    img_ad.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            img_ad.setVisibility(View.GONE);

//                            handler.removeCallbacksAndMessages(null);
                            Intent intent = new Intent(WelcomeActivity.this, WebviewActivity.class);
                            intent.putExtra("weburl", weburl);
                            startActivity(intent);
                            finish();
                        }
                    });
                    Log.e("pic", picLoadUrl);


                    String version = root.getString("ver");
                    SPHelper.setLastVersion(WelcomeActivity.this, version);
                    String curVersion = Tools.getVersion(WelcomeActivity.this);
                    if (Tools.versionCompare(version, curVersion) == 0) {//版本对
                        JSONArray data = root.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject value = data.getJSONObject(i);
                            String channel = value.getString("channel");
//                            String curChannel="baidu";
                            String curChannel = WelcomeActivity.this.getString(R.string.syezon_channel_id);
                            Log.e("channel", channel + "  " + curChannel);
                            if (channel.equals(curChannel)) {//0表示关闭，1表示打开
                                String nativeAd = value.getString("native");

                                String bannerAd = value.getString("banner");

                                String wallAd = value.getString("wall");
                                String splash = value.getString("splash");
                                Log.e("splash1", splash);
                                if (splash.equals("0")) {
                                    mImg_welcome.setVisibility(View.VISIBLE);
                                    img_ad.setVisibility(View.GONE);
                                    count_tv.setVisibility(View.GONE);
                                    time_tv.setVisibility(View.GONE);
                                    text_switcher.setVisibility(View.GONE);

                                } else {
                                    rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
                                    rl_bottom.setVisibility(View.VISIBLE);
                                    Picasso.with(WelcomeActivity.this).load(picLoadUrl).into(img_ad);
                                    img_ad.setVisibility(View.VISIBLE);
                                    mImg_welcome.setVisibility(View.GONE);
                                    text_switcher.setVisibility(View.VISIBLE);

                                }
                                if ("0".equals(nativeAd)) {
                                    SPHelper.setNativeADIsOpen(WelcomeActivity.this, false);
                                } else {
                                    SPHelper.setNativeADIsOpen(WelcomeActivity.this, true);
                                }
                                if ("0".equals(bannerAd)) {
                                    SPHelper.setBannerADIsOpen(WelcomeActivity.this, false);
                                } else {
                                    SPHelper.setBannerADIsOpen(WelcomeActivity.this, true);
                                }
//                                if ("0".equals(videoAd)) {
//                                    SPHelper.setVideoADIsOpen(WelcomeActivity.this, false);
//                                } else {
//                                    SPHelper.setVideoADIsOpen(WelcomeActivity.this, true);
//                                }
//                                if ("0".equals(interstitialAd)) {
//                                    SPHelper.setInterstitialADIsOpen(WelcomeActivity.this, false);
//                                } else {
//                                    SPHelper.setInterstitialADIsOpen(WelcomeActivity.this, true);
//                                }
                                if ("0".equals(wallAd)) {
                                    SPHelper.setWallADIsOpen(WelcomeActivity.this, false);
                                } else {
                                    SPHelper.setWallADIsOpen(WelcomeActivity.this, true);
                                }
                                break;
                            }
                        }
                    } else {
                        if (Tools.versionCompare(version, curVersion) == 1) {
                            SPHelper.setNeedUpdate(WelcomeActivity.this, true);
                        }

                        JSONArray updateArrary = root.optJSONArray("update");
                        if (updateArrary != null) {
                            for (int i = 0; i < updateArrary.length(); i++) {
                                if (Constant.NATIVE_BN.equals(updateArrary.optJSONObject(i).optString("bn"))) {
                                    UPDATE_URL = updateArrary.optJSONObject(i).optString("url");
                                    Log.e("UPDATE_URL", UPDATE_URL);
                                }
                            }
                        }

                        setAdSwitch(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setAdSwitch(true);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //设置开关状态
    private void setAdSwitch(boolean isOpen) {
        SPHelper.setNativeADIsOpen(this, isOpen);
        SPHelper.setWallADIsOpen(this, isOpen);
        SPHelper.setInterstitialADIsOpen(this, isOpen);
        SPHelper.setVideoADIsOpen(this, isOpen);
        SPHelper.setBannerADIsOpen(this, isOpen);
    }

    //检查更新
    private void checkNovelUpdate() {
        String cid = "";
        if (SConfig.IMSI != null) {
            cid = SConfig.IMSI;
        } else if (SConfig.IMEI != null) {
            cid = SConfig.IMEI;
        } else {
            cid = SConfig.MAC;
        }
        StringBuffer ids = new StringBuffer();
        final BookCaseDBHelper helper = new BookCaseDBHelper(this);
        List<BookCaseBean> data = helper.queryBookCase();
        for (int i = 0; i < data.size(); i++) {
            int type = data.get(i).getBook_type();
            if (type != 2) {
                ids.append(data.get(i).getBook_id()).append(",");
            }
        }
        //有网络书籍的时候才去检查
        if (ids.length() > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("cid", cid);
            params.put("token", SPHelper.getUserToken(this));
            params.put("ids", ids.substring(0, ids.length() - 1));
            OkHttpUtils.postString().url(APIDefine.GET_NOVEL_NEW_CHAPTER).content(Tools.Map2Json(params))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Log.e("TAG", "get book last chapter error:" + e);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            // Log.e("TAG", "get book last chapter response:" + response);
                            try {
                                JSONObject root = new JSONObject(response);
                                int rc = root.getInt("rc");
                                if (rc == 1) {
                                    JSONArray data = root.getJSONArray("data");
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject value = data.getJSONObject(i);
                                        int novelId = value.getInt("id");
                                        String chapter = value.getString("lastchapter");
                                        int chapterNO = value.getInt("lastchapterno");
                                        BookCaseBean bean = new BookCaseBean();
                                        bean.setBook_id(novelId);
                                        bean.setLast_chapter_no(chapterNO);
                                        bean.setLast_chapter(chapter);
                                        helper.updateBookCase(bean, helper.BOOK_CHECK_UPDATE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("WelcomeActivity"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
    }

}
