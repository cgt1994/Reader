package com.syezon.reader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.MD5Util;
import com.syezon.reader.utils.SPHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 缓存下载服务
 * Created by jin on 2016/9/27.
 */
public class CacheService extends Service {

    private static final int CACHE_START = 1;
    private static final int CACHE_ERROR = 2;
    private static final int CACHE_COMPLETE = 3;
    public static List<String> mCacheTask = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CacheBean bean = (CacheBean) msg.obj;
            switch (msg.what) {
                case CACHE_START:
//                    Toast.makeText(CacheService.this, "开始缓存 " + bean.startName, Toast.LENGTH_SHORT).show();
                    break;
                case CACHE_COMPLETE:
//                    SPHelper.setBookHasCache();
                    Toast.makeText(CacheService.this, bean.startName + "缓存完成", Toast.LENGTH_SHORT).show();
                    break;
                case CACHE_ERROR:

                    Intent intent = new Intent();
                    intent.setAction("cacheComplete");
                    intent.putExtra("cacheError", true);
                    CacheService.this.sendBroadcast(intent);
//                    Toast.makeText(CacheService.this, bean.startName + " 缓存出现错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //缓存线程
    class CacheThread extends Thread {
        private CacheBean bean;
        private List<String> data;
        private Boolean needDivied;
        private BookCaseDBHelper bookCaseDBHelper;

        public CacheThread(CacheBean bean, List<String> data, Boolean needDivied) {
            this.bean = bean;
            this.data = data;
            this.needDivied = needDivied;
            if (data.size() > 1) {
                bean.cacheOne = false;
            } else {
                bean.cacheOne = true;
            }
            bookCaseDBHelper = new BookCaseDBHelper(CacheService.this);
        }

        @Override
        public void run() {
            super.run();
            BookCaseBean bookCaseBean = new BookCaseBean();
            bookCaseBean.setBook_name(bean.startName);
            //提示开始缓存
            Message msg = Message.obtain();
            msg.what = CACHE_START;
            msg.obj = bean;
            mHandler.sendMessage(msg);
            String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/com.syezon.reader/book/" + MD5Util.encrypt(bean.startName) + "/";
            SPHelper.setBookFilePath(CacheService.this, bean.startName, dirName);
            while (data.size() > 0 && InfoUtils.cachingBook.contains(bean.startName)) {//下载队列中还有数据就一直循环

                HttpURLConnection conn = null;
                InputStream is = null;
                try {

                    String info = data.get(0);

                    int index = info.indexOf("http");
                    if (index <= 0) {
                        data.remove(0);
                        bean.startChapter++;
                        continue;
                    }
                    String urlHttp = info.substring(index);
                    String chapterName = info.substring(0, index - 1);
//                    Log.e("excpiton url", urlHttp + " ");
//                    String[] infos = info.split(",");


                    URL url = new URL(urlHttp);
                    //Log.e("TAG", "cache file:" + bean.startChapter + ":" + infos[0]);
                    conn = (HttpURLConnection) url.openConnection();


                    File dirFile = new File(dirName);//一本书一个目录
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }

                    String fileName = bean.startChapter + "";//每一章节一个文件,文件名称就是章节号
                    File file = new File(dirName + fileName);

                    if (conn != null) {
                        file.createNewFile();
                        //得到所下载文章的输入流，将其以文件的形式写入到存储卡上面
                        is = conn.getInputStream();
                        InputStreamReader isReader = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(isReader);

                        OutputStream os = new FileOutputStream(file);
                        OutputStreamWriter osWriter = new OutputStreamWriter(os);
                        BufferedWriter writer = new BufferedWriter(osWriter);

                        String lineTxt;
                        String s = (data.get(0).split(","))[0];
                        writer.write(s + "\n");
                        while ((lineTxt = reader.readLine()) != null) {
//                            Log.e("chap","lineTxt"+lineTxt);
                            writer.write(lineTxt + "\n");
                        }
                        writer.close();
                        reader.close();
//                        Log.e("needDivied", needDivied + "");
                        if (needDivied || bean.startChapter == 1) {
                            //开服务去分页
                            Intent intent = new Intent(CacheService.this, BookIndexService.class);
                            intent.putExtra("filePath", file.getPath());
                            intent.putExtra("bookName", bean.startName);
                            intent.putExtra("cacheOne", bean.cacheOne);
                            intent.putExtra("chapter", bean.startChapter);//章节id
                            intent.putExtra("chapterName", chapterName);
                            intent.putExtra("needOpen", bean.needOpen);
                            intent.putExtra("encoding", "utf-8");
                            startService(intent);
                        }
                        //更新缓存进度,先查询缓存的最后章节，可能出现先缓存了后一章，在去缓存前一章的情况，这个时候不需要去修改缓存进度
                        int cache = bookCaseDBHelper.getLastBookCache(bean.startName);
                        if (cache < bean.startChapter) {
                            bookCaseBean.setCache(bean.startChapter);
                            bookCaseDBHelper.updateBookCase(bookCaseBean, bookCaseDBHelper.BOOK_CACHE);
                        }
                        Log.e("zxc", needDivied + " " + bean.startName + "   " + bean.startChapter);
                        HashSet hashSet = new HashSet<>();
                        Log.e("lianadd", bean.startChapter + " ");
                        hashSet = (HashSet) SPHelper.getCache(CacheService.this, bean.startName);
                        hashSet.add(bean.startChapter);
                        SPHelper.setCache(CacheService.this, bean.startName, hashSet);
                        if (bean.startChapter > SPHelper.getHasCache(CacheService.this, bean.startName)) {
                            SPHelper.setHasCache(CacheService.this, bean.startName, bean.startChapter);
                        }

                        //写入章节存储位置等信息
                        ChapterDBHelper chapterDBHelper = new ChapterDBHelper(CacheService.this, bean.startName);
                        ChapterBean chapterBean = new ChapterBean();
                        chapterBean.setChapterId(bean.startChapter);
                        chapterBean.setChapterName(chapterName);
                        chapterBean.setChapterPosition(file.getPath());
                        chapterDBHelper.writeBook(chapterBean);
                        chapterDBHelper.closeDB();
                    }
                } catch (Exception e) {
                    Log.e("excpiton", e.getMessage() + e.toString());
                    e.printStackTrace();
                    Message msg2 = Message.obtain();
                    msg2.what = CACHE_ERROR;
                    msg2.obj = bean;
                    mHandler.sendMessage(msg2);
                    break;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

                data.remove(0);
                bean.startChapter++;
//                InfoUtils.savaedChapters.put(bean.startName, bean.startChapter);
            }
            //缓存整本书的时候提示缓存完成
            if (!bean.cacheOne) {
                Message msg3 = Message.obtain();
                msg3.what = CACHE_COMPLETE;
                msg3.obj = bean;
                mHandler.sendMessage(msg3);
            }
            //移除缓存队列
            if (mCacheTask.contains(bean.startName + ":" + (bean.startChapter - 1)))
                mCacheTask.remove(bean.startName + ":" + (bean.startChapter - 1));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String startName = intent.getStringExtra("bookName");
        List<String> data = intent.getStringArrayListExtra("data");
        Boolean needDevide = intent.getBooleanExtra("needDevide", true);
//        Log.e("data","data"+data.get(0)+"  "+data.get);
        for (String a : data) {
            Log.e("data", a);
        }
        CacheBean bean = new CacheBean();
        bean.startId = startId;
        bean.startName = startName;
        bean.startChapter = intent.getIntExtra("startChapter", 0);
        bean.needOpen = intent.getBooleanExtra("needOpen", false);
        //如果已经在缓存队列了，就不在缓存
        // Log.e("TAG", "cache task:" + bean.startName + ":" + bean.startChapter);
        if (!checkCacheTask(bean.startName + ":" + bean.startChapter)) {
            mCacheTask.add(bean.startName + ":" + bean.startChapter);
            new CacheThread(bean, data, needDevide).start();
        } else {
            Log.e("TAG", "task exist");
        }
        return START_REDELIVER_INTENT;
    }

    //判断是否存在缓存队列中
    private boolean checkCacheTask(String task) {
        for (int i = 0; i < mCacheTask.size(); i++) {
            if (task.equals(mCacheTask.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static class CacheBean {
        public int startId;//服务id
        public boolean cacheOne;//是否只缓存一章
        public String startName;//书本名称
        public int startChapter;//开始缓存的章节id
        public boolean needOpen;//是否需要在分完页之后打开书籍
    }
}
