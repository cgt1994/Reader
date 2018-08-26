package com.syezon.reader.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.syezon.reader.constant.Constant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Set;

/**
 * SP文件帮助类，每本书的读取进度及索引信息
 * 每本书会有一个sp文件
 * Created by jin on 2016/8/30.
 */
public class SPHelper {

    /**
     * ----------------------------------common-------------------------------------------------
     */
    private static String COMMON_SPNAME = "reader_config";

    public static void setTextFix(Context context, int a) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("textfix", a).commit();


    }

    public static int getTextFix(Context context) {
        SharedPreferences sharedPreference = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreference.getInt("textfix", -1);
    }

    //保存用户token值
    public static void setUserToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("user_token", token).commit();
    }

    public static String getUserToken(Context context) {
        SharedPreferences sharedPreference = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreference.getString("user_token", "-1");
    }

    // 判断是否需要获取字符集
    public static void setNeedCharset(Context context, String bookname, Boolean need) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(bookname, need).commit();
    }

    public static Boolean getNeedCharset(Context context, String bookname) {
        SharedPreferences sharedPreference = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreference.getBoolean(bookname, false);
    }

    public static void setRate(Context context, float rate) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putFloat("RATE", rate).commit();
    }

    public static float getRate(Context context) {
        SharedPreferences sharedPreference = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreference.getFloat("RATE", (float) 0.5);
    }

    //不自动锁屏
    public static void setNotAutoLock(Context context, boolean notAutoLock) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("not_auto_lock", notAutoLock).commit();
    }

    public static boolean getNotAutoLock(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("not_auto_lock", false);
    }

    //    是否有引言
    public static void isHasIntroduction(Context context, String bookname, boolean has) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("Introduction" + bookname, has).commit();
    }

    public static boolean getHasIntroduction(Context context, String bookname) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("Introduction" + bookname, false);
    }

    public static void setUpdataDate(Context context, String time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("UpdataDate", time).commit();
    }

    public static String getUpdataDate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("UpdataDate", "");
    }

    //是否开启过引导界面
    public static void setNeedUpdate(Context context, boolean isneed) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("Need_Update", isneed).commit();
    }

    public static boolean getNeedUpdate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("Need_Update", false);
    }

    //是否开启过引导界面
    public static void setHasGuide(Context context, boolean hasguide) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("hasguide", hasguide).commit();
    }

    public static boolean getHasGuide(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("hasguide", false);
    }

    //阅读风格0表示白色，1表示黄色，2表示粉色，3表示夜间
    public static void setReadStyle(Context context, int style) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("style", style).commit();
    }

    public static int getReaderStyle(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("style", 0);
    }

    public static void saveAdChapter(Context context, Set chapter) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet("AD_EXIST", chapter).commit();
    }

    public static Set getAdChapter(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet("AD_EXIST", new HashSet<String>());
    }

    //阅读是屏幕亮度
    public static void setReadBrightness(Context context, int brightness) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("brightness", brightness).commit();
    }

    public static int getReadBrightness(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("brightness", 50);
    }

    //保存字体大小
    public static void setBookTextSize(Context context, int textSize) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("textSize", textSize).commit();
    }

    public static int getBookTextSize(Context context) {
        SharedPreferences sharedPreference = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreference.getInt("textSize", Constant.DEFAULT_TEXTSIZE);
    }


    //保存当前页面能显示的行数
    public static void setBookLines(Context context, int lines) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("lines", lines).commit();
    }

    public static int getBookLines(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("lines", 0);
    }

    //保存当前模式下能显示的字数
    public static void setCurTxtNums(Context context, int nums) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("nums", nums).commit();
    }

    public static int getCurTxtNums(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("nums", 0);
    }

    public static void setCutLines(Context context, int nums) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("cutLines", nums).commit();
    }

    public static int getCutLines(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("cutLines", 0);
    }

    // 判断后台是否在操作数据库存储分页信息
    public static void setSaveInfo(Context context, boolean issaving) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("saveinfo", issaving).commit();
    }

    public static Boolean getSaveInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("saveinfo", false);
    }

    //保存上传页面index.html的路径
    public static void setUploadIndex(Context context, String path) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("index", path).commit();
    }

    public static String getUploadIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("index", "-1");
    }

    //保存服务器地址
    public static void setServerIP(Context context, String serverIP) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("serverIP", serverIP).commit();
    }

    public static String getServerIP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("serverIP", "-1");
    }

    //保存wifi名称
    public static void setWiFiName(Context context, String wifiName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("wifiName", wifiName).commit();
    }

    public static String getWiFiName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("wifiName", "-1");
    }

    //标记是否清空过历史文件
    public static void setClearHistory(Context context, boolean isClear) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("clearHistory", isClear).commit();
    }

    public static boolean getClearHistory(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("clearHistory", false);
    }

    //标记内置小说是否已近拷贝
    public static void setFileIsCopy(Context context, boolean isCopy) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("fileCopy", isCopy).commit();
    }

    public static boolean getFileIsCopy(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("fileCopy", false);
    }

    //保存广告开关
    public static void setNativeADIsOpen(Context context, boolean isOpen) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("nativeAd", isOpen).commit();
    }

    public static boolean getNativeADIsOpen(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("nativeAd", true);
    }

    public static void setInterstitialADIsOpen(Context context, boolean isOpen) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("interstitialAd", isOpen).commit();
    }

    public static boolean getInterstitialADIsOpen(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("interstitialAd", true);
    }

    public static void setVideoADIsOpen(Context context, boolean isOpen) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("videoAd", isOpen).commit();
    }

    public static boolean getVideoADIsOpen(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("videoAd", true);
    }

    public static void setWallADIsOpen(Context context, boolean isOpen) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("wallAd", isOpen).commit();
    }


    public static void setBannerADIsOpen(Context context, boolean isOpen) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("bannerAd", isOpen).commit();
    }

    public static boolean getBannerADIsOpen(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("bannerAd", true);
    }

    //    存储手机高度
    public static void setPhoneHeight(Context context, int high) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("higth", high).commit();
    }

    public static int getPhoneHeight(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("higth", 1);
    }

    /**
     * ------------------------------------book------------------------------------------------
     */
    //存储当前页数
    public static void setCurrentPage(Context context, String bookName, int page) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("currentPage", page).commit();
    }

    public static void setCache(Context context, String bookname, Set<String> cache) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookname, Context.MODE_PRIVATE);
        sharedPreferences.edit().putStringSet(bookname, cache).commit();
    }

    public static Set<String> getCache(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(bookName, new HashSet<String>());
    }

    public static int getCurrentPage(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("currentPage", 1);
    }


    //保存当前章节数
    public static void setCurChapterNO(Context context, String bookName, int chapterNO) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("chapterNO", chapterNO).commit();
    }

    public static int getCurChapterNO(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("chapterNO", 1);
    }

    //当前章节名称
    public static void setCurChapterName(Context context, String bookName, String chapterName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("chapterName", chapterName).commit();
    }

    public static String getCurChapterName(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getString("chapterName", " ");
    }

    //保存当前文件读取的位置
    public static void setCurrentSeek(Context context, String bookName, long seek) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong("seek", seek).commit();
    }

    public static long getCurrentSeek(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getLong("seek", 0);
    }

    //书本的编码格式
    public static void setBookEnCoding(Context context, String bookName, String encoding) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("encoding", encoding).commit();
    }

    public static String getBookEnCoding(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getString("encoding", "utf-8");
    }

    //退出时是否在上下章节之间,0表示上一章最后一页,1表示下一章第一页,在上下章之间来回切换的时候产生
    public static void setSpecialPos(Context context, String bookName, int pos) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("special", pos).commit();
    }

    public static int getSpecialPos(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("special", -1);
    }


    //当前书籍文件位置
    public static void setBookFilePath(Context context, String bookName, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        Log.e("setpath", bookName + "   " + filePath);
        sharedPreferences.edit().putString("filePath", filePath).commit();
    }

    public static String getBookFilePath(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getString("filePath", "-1");
    }

    //保存该书本是否分页完成
    public static void setParsePageComplete(Context context, String bookName, boolean isComplete) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isPageComplete", isComplete).commit();
    }

    public static boolean getParsePageComplete(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isPageComplete", false);
    }

    //保存该书本是否分章完成
    public static void setParseChapterComplete(Context context, String bookName, boolean isComplete) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isChapterComplete", isComplete).commit();
    }


    //    保存章节总数
    public static void setBookLength(Context context, String bookName, int chaptertotal) {
        Log.e("length", " " + bookName + "  " + chaptertotal);
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("chaptertotal", chaptertotal).commit();
    }

    public static int getBookLength(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("chaptertotal", 0);
    }

    //清除之前的索引信息
    public static void clearSP(Context context, String bookName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }


    //          保存缓存的章节数，用于显示下载的进度
    public static void setHasCache(Context context, String bookname, int cache) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(bookname + "cache", cache).commit();
    }

    public static int getHasCache(Context context, String bookname) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(bookname + "cache", -1);
    }

    public static void setLastVersion(Context context, String version) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("Version", version).commit();
    }

    public static String getLastVersion(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("Version", "");
    }

    //保存当前书的字体
    public static void setBookInfoSize(Context context, String bookname, int size) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(bookname + "textsize", size).commit();
    }

    public static int getBookInfoSize(Context context, String bookname) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(bookname + "textsize", 0);
    }

    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear();
    }

    public static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(key).commit();
    }

    public static void saveLongData(Context context, long time) {
        SharedPreferences sp = context.getSharedPreferences(COMMON_SPNAME, 0);
        sp.edit().putLong("install", time).commit();
    }

    public static long getLongData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(COMMON_SPNAME, 0);
        return sp.getLong("install", 0);
    }

    public static <T> T getObject(Context context, String key) {
        Log.e("info get", key);
        SharedPreferences sp = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);
        if (sp.contains(key)) {
            String objectVal = sp.getString(key, null);
            byte[] buffer = Base64.decode(objectVal, Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
                T t = (T) ois.readObject();
                return t;
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    //保存对象
    public static void setObject(Context context, String key, Object object) {
        Log.e("setObject set", key + (object == null));
        SharedPreferences sp = context.getSharedPreferences(COMMON_SPNAME, Context.MODE_PRIVATE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {

            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            String objectVal = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objectVal);
            editor.commit();

        } catch (IOException e) {
            Log.e("savaerror", e.getMessage() + " " + e.getCause() + e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
