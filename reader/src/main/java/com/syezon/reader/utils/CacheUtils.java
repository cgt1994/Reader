package com.syezon.reader.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.http.APIDefine;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.service.BookIndexService;
import com.syezon.reader.service.CacheService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 缓存工具类
 * Created by jin on 2016/10/8.
 */
public class CacheUtils {
    public static void cacheFile(final Context context, final String bookName, int bookId, int start, final int end, final boolean needOpen) {
        Log.e("cache1", "bookname" + bookName + "start" + start + "end" + end + (SPHelper.getCache(context, bookName).contains(start)));
//        -5代表 start之后的全部请求 并且取点 之后可以根据这个断点下载
        if (end == -5) {
            if (SPHelper.getHasCache(context, bookName) != -1) {
                start = SPHelper.getHasCache(context, bookName) + 1;
            }
        }
//        已缓存但未分页 去分页
        ChapterDBHelper chapterDBHelper = new ChapterDBHelper(context, bookName);
        ChapterBean chapterBean = chapterDBHelper.readBookOneChapter(start);
        if (chapterBean != null) {
            Intent intent = new Intent(context, BookIndexService.class);
            intent.putExtra("filePath", chapterBean.getChapterPosition());
            intent.putExtra("bookName", bookName);
            intent.putExtra("cacheOne", true);
            intent.putExtra("chapter", start);//章节id
            intent.putExtra("chapterName", chapterBean.getChapterName());
            intent.putExtra("needOpen", needOpen);
            intent.putExtra("encoding", "utf-8");
            context.startService(intent);
            chapterDBHelper.closeDB();
            return;
        }
        if (start <= 0) {
            return;
        }
        if (SPHelper.getCache(context, bookName).contains(start)) {
            Intent intent = new Intent();
            intent.setAction("cacheComplete");
//            intent.putExtra("cacheError", true);
            context.sendBroadcast(intent);
            return;

        }

        String cid = "";
        if (SConfig.IMSI != null) {
            cid = SConfig.IMSI;
        } else if (SConfig.IMEI != null) {
            cid = SConfig.IMEI;
        } else {
            cid = SConfig.MAC;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("token", SPHelper.getUserToken(context));
        params.put("id", bookId);
        params.put("start", start);
        params.put("end", end);
        //Log.e("TAG", "cache file:" + bookName + ":" + start + ":" + end);
        final int finalStart = start;
        OkHttpUtils.postString().url(APIDefine.GET_NOVEL_INFO)
                .content(Tools.Map2Json(params))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("lianwang", "get book cache error:" + e);
                    }

                    @Override
                    public void onResponse(String response, int id) {

                        Log.e("lianwang", "get book cache response:" + response);
                        try {
                            JSONObject root = new JSONObject(response);
                            int rc = root.getInt("rc");
                            if (rc == 1) {
                                String baseUrl = root.getString("baseUrl");
                                JSONArray data = root.getJSONArray("data");
                                if (data.length() == 0) {
                                    return;
                                }
                                ArrayList<String> cacheUrl = new ArrayList<String>();
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject value = data.getJSONObject(i);
                                    cacheUrl.add(value.getString("name") + "," + baseUrl + value.getString("url"));
//                                    Log.e("url", data.length() + " url " + i + "  " + cacheUrl.get(i));
                                }
                                int startChapter = finalStart;
                                if (cacheUrl.size() > 0 && InfoUtils.cachingBook.contains(bookName)) {
                                    Intent intent = new Intent(context, CacheService.class);
                                    intent.putStringArrayListExtra("data", cacheUrl);
                                    intent.putExtra("startChapter", startChapter);
                                    intent.putExtra("bookName", bookName);
                                    intent.putExtra("needOpen", needOpen);
                                    intent.putExtra("needDevide", end != -5);
                                    context.startService(intent);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("excpiton2", e.toString() + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
    }
}
