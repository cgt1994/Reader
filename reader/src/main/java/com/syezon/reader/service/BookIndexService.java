package com.syezon.reader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.db.PageDBHelper;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.model.PageBean;
import com.syezon.reader.utils.InfoUtils;
import com.syezon.reader.utils.SPHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 当是通过WiFi传书时使用该类进行建立书本索引的服务
 * Created by jin on 2016/9/21.
 */
public class BookIndexService extends Service {
    private static final int CACHE_COMPLETE = 1;
    public static List<String> mStartTask = new ArrayList<>();//分页服务队列
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CACHE_COMPLETE:
                    boolean cacheOne = msg.arg1 == 1 ? true : false;
                    int chapter = msg.arg2;
                    //发送广播更新数据
                    Intent intent = new Intent();
                    intent.putExtra("cacheOne", cacheOne);
                    intent.putExtra("chapter", chapter);
                    intent.putExtra("cacheError", false);
                    intent.setAction("cacheComplete");
                    BookIndexService.this.sendBroadcast(intent);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private InfoUtils infoUtils;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        infoUtils = InfoUtils.getInstance(this);
        if (intent != null) {
            ArrayList needdevide;
            final int booktype = intent.getIntExtra("bookType", 1);
            final String bookName = intent.getStringExtra("bookName");
            final int taskId = intent.getIntExtra("chapter", 1);
            //判断该书的该章节是否已经在进行分页了
            Boolean redeivde = intent.getBooleanExtra("redevide", false);
            if (redeivde) {
                Log.e("times", "redevide==true");

                needdevide = (ArrayList) intent.getStringArrayListExtra("needdevide");
//                Log.e("times", needdevide.size() + " qqq");
            } else {
                needdevide = new ArrayList();
            }

            Log.e("tag", "重新分页吗" + redeivde);
            // Log.e("TAG", "task is:" + bookName + ":" + taskId);
            if (redeivde) {
                Log.e("renwu", mStartTask.contains(bookName + ":" + taskId) + "");
//                if (mStartTask.contains(bookName + ":" + taskId)) {
                mStartTask.remove((bookName + ":" + taskId));
            }
            if (!mStartTask.contains(bookName + ":" + taskId)) {
                mStartTask.add(bookName + ":" + taskId);

                final String filePath = intent.getStringExtra("filePath");
                final String encoding = intent.getStringExtra("encoding");
                final String chapterName = intent.getStringExtra("chapterName");
                final int chapter = intent.getIntExtra("chapter", 1);

                final boolean isCacheOne = intent.getBooleanExtra("cacheOne", false);

                final boolean needOpen = intent.getBooleanExtra("needOpen", false);//是否需要打开书籍,只有在wifi传书的时候不需要打开，其他时候分页都是要打开书籍的
                Log.e("open", "filePath" + filePath);
                Log.e("open", "chapterName" + chapterName);
                Log.e("open", "chapter" + chapter);
                Log.e("open", "bookName" + bookName);
                Log.e("encodeing get", bookName + " " + encoding);
//                SPHelper.setBookFilePath(this, bookName, filePath);
                final ArrayList finalNeeddevide = needdevide;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("booktype", "task begin run" + "" + booktype);
                        if (booktype == 1) {
                            divideNetPage(bookName, chapterName, chapter, filePath, encoding, taskId, isCacheOne ? 1 : 0, needOpen, finalNeeddevide, booktype);

                        } else {
                            divideNativePage(bookName, chapterName, 1, filePath, encoding, taskId, isCacheOne ? 1 : 0, needOpen, finalNeeddevide, booktype);
                        }
                    }
                }).start();
            } else {
                //for (int i = 0; i < mStartTask.size(); i++) {
                //Log.e("TAG", "task has already exist:"+mStartTask.get(i));
                //}
                Log.e("TAG", "task has already exist");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //    public  int stringNumbers(String str,String t,int counter)
//    {
//        if (str.indexOf(t)==-1)
//        {
//            return 0;
//        }
//        else if(str.indexOf(t) != -1)
//        {
//            counter++;
//            stringNumbers(str.substring(str.indexOf(t)+4),t,counter);
//            return counter;
//        }
//        return 0;
//    }


    /**
     * @param input
     * @return 全角转半角
     */
    public static String ToDBC(String input) {


        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }
        String returnString = new String(c);

        return returnString;
    }

    private synchronized void divideNativePage(String bookName, String chapterName, int chapterNO, String filePath, String encoding, int taskId, int isCacheOne, boolean NeedOpen, ArrayList<ChapterBean> list, int booktype) {
        try {

            SPHelper.setSaveInfo(this, true);
            Log.e("TAG", "dividePage");
            Boolean needOpen;
            int ci = 0;

            List<String> pageInfo = new ArrayList<String>();
            List<String> chapterInfo = new ArrayList<String>();

            int chapter = 1; //章节数,从数据库中读取，因为一章一章下载分页
            File file = new File(filePath);
//                null是直接进来分页 =0是当前只有缓存一章的情况下去换字体

            Log.e("tag", "fileis exists" + file.exists());
            if (!file.exists()) {
                return;
            }
            InputStream in = new FileInputStream(file);
            InputStreamReader inReader = new InputStreamReader(in, encoding);
            BufferedReader reader = new BufferedReader(inReader);

            String lineTxt;
            int page = 0;
            int chapterPage = 0;//章节开始的页号
            String lastChapter = chapterName;//上一章标题的
            int viewLines = SPHelper.getBookLines(this);//- SPHelper.getCutLines(this);//少填充一行作为调整

            int viewNums = SPHelper.getCurTxtNums(this);
            Log.e("fenyeservice", "textsize" + SPHelper.getBookTextSize(this) + "viewnum" + viewNums + " " + SPHelper.getBookLines(this) + "  " + SPHelper.getCutLines(this));

            int nowLine = 0;//当前是文件的第几行
            int startLine = 0; //这页从文件的第几行开始,每个文件是唯一的值
            int startChar = 0; //这页从startLine行的第几个汉字开始，每行是唯一的值
            int endLine = 0; //这页到文件的第几行结束,每个文件是唯一的值
            int endChar = 1; //这页到endLine行的第几个汉字结束，每行是唯一的值
            int pageLine = 0;//当前页到第几行了，pageLine<=viewLines
            int chapterStartLine = 0;//章节开始行数
            int chapterEndLine = 0;//章节结束行数
            int qw = 0;

            long startTime = 0;
            long endTime = 0;
            boolean space = false;
            boolean hasIntroduction = false;//是否有引言
            //还未读完
            startTime = System.currentTimeMillis();
            Log.e("starttime", startTime + " ");
            while ((lineTxt = reader.readLine()) != null) {
                if (nowLine == 0) {
//                    第一页的空白页
                    pageInfo.add(page + "|" + chapter + "|" + nowLine + "|" + (nowLine - 1) + "|" + startChar + "|" + endChar);
//                    判断是否
                    SPHelper.isHasIntroduction(BookIndexService.this, bookName, !judgeChapter(lineTxt));
                }

                nowLine++;
                lineTxt = ToDBC(lineTxt);
//                    if (lineTxt.lastIndexOf(" ")!=-1){
                lineTxt = "  " + lineTxt.trim();
                Log.e("fenyeservice", space + "" + lineTxt.length() + "lin==" + lineTxt);
                if (judgeChapter(lineTxt) && isChapter(chapterEndLine, nowLine)) {
                    if (nowLine > 1) {
                        hasIntroduction = true;

                        page++;
//                        pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + (nowLine) + "|" + startChar + "|" + endChar);
                    } else {
                        hasIntroduction = false;
                    }
                    if (page != 0) {


//                        Log.e("fenyeservice", "这是标题" + lineTxt);
                        Log.e("zxcvb", "标题筛选 " + lineTxt + "page" + (page) + "chapter" + chapter + "startLine" + startLine + "endLine" + (nowLine) + "   " + startChar + "..." + endChar);
                        pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + (nowLine) + "|" + startChar + "|" + endChar);
//                        添加空白页
                        if (nowLine > 1) {
                            page++;
                            pageInfo.add(page + "|" + chapter + "|" + (nowLine) + "|" + (nowLine - 1) + "|" + 0 + "|" + 0);
                        }


                    }

                    //startChar = 0;
                    endChar = 0;
//                    if (nowLine > 1) {
//                        Log.e("pageadd", " 2 " + "  " + page);
//
//
//                        Log.e("zxcvb", "" + lineTxt + "page" + (page) + "chapter" + chapter + "startLine" + startLine + "endLine" + (nowLine) + "   " + startChar + "..." + endChar);
//                        pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//
////                            Constant.indexPage.put(bookName, pageInfo);
//                        Log.e("fenyeservice", "page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
//                    } else {
//
//                    }
                    //没有引言的时候，章标题就是这一章的标题
                    if (!hasIntroduction) {
                        if (nowLine > 1) {//第一次不执行加一操作，因为从1开始
                            chapter++;
                            chapterStartLine = chapterEndLine + 1;
                            chapterPage += page;
                        }
                    }
                    startLine = nowLine + 1;
                    pageLine = 0;
                    startChar = 0;
                    chapterEndLine = nowLine;
                    if (nowLine > 1) {
                        Log.e("chapterInfo ", lineTxt + " " + chapterPage + "|" + chapter + "|" + lastChapter + "|" + chapterStartLine + "|" + chapterEndLine + "|" + 0);
                        chapterInfo.add(chapterPage + "|" + chapter + "|" + lastChapter + "|" + chapterStartLine + "|" + chapterEndLine + "|" + 0 + "|" + lineTxt);
                    }
                    // Log.e("TAG", "chapter info:" + page + "," + chapter + "," + lastChapter + "," + chapterStartLine + "," + chapterEndLine + "," + 0);
                    if (hasIntroduction) {//有引言的时候，章标题是下一章的标题
                        chapter++;
                        chapterStartLine = chapterEndLine + 1;
                        chapterPage += page;
                    }
                    lastChapter = lineTxt;
                    page = 0;
                    continue;
                }
                if (lineTxt.length() == 2) {
                    space = true;
                } else {
                    space = false;
                }
//                if (judgeChapter(lineTxt) && isChapter(chapterEndLine, nowLine)) {
//
//                }

                int lineTxtLen = lineTxt.trim().length() + 2;
                int viewline = lineTxtLen / viewNums; //这行抵屏幕多少行

                if (viewline == 0) {
                    //不够一行当一行
                    viewline = 1;
                } else if (viewline * viewNums < lineTxtLen) {
                    //不能整除的，行数加1
                    viewline++;

                    Log.e("fenyeservice", "lineTxtLen==" + lineTxtLen + ",lineTxt   " + lineTxt + ",viewline==" + viewline + "pageLine" + pageLine);

                }
//                 Log.e("TAG", "need lines:" + viewline+":"+lineTxt);
                //计算当前总行数，可能超出能显示的行数
                int lineTmpCount = viewline + pageLine;
                if (lineTmpCount < viewLines) {
                    //在一页范围内
                    pageLine += viewline;
                    Log.e("fenyeservice", "pageline" + pageLine);

                } else if (lineTmpCount == viewLines) {
                    //正好满一页
                    page++;
                    endLine = nowLine;
                    endChar = lineTxtLen;
                    Log.e("fenyeservice", "满页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "   " + startChar + "..." + endChar);
                    pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//                        Constant.indexPage.put(bookName, pageInfo);
//                      Log.e("TAG", "just complete page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                    //下一页的开始
                    startLine = nowLine + 1;
                    pageLine = 0;
                    startChar = 0;
                    endChar = 0;
                } else {
                    //这行跨页了
                    int pageCount = lineTmpCount / viewLines;//计算当前的行数需要几页
                    //取上整,页数不需要去上整
//                    if (pageCount * viewLines < lineTmpCount) {
//                        pageCount++;
//                    }
                    //Log.e("TAG", "over page:" + pageCount + "," + lineTxt.length() + "," + lineTxt);
                    for (int i = 0; i < pageCount; i++) {
                        if (i == 0) {//还在这一页
                            startChar = endChar;
//

                            endChar = viewNums * (viewLines - pageLine);//这一页上能显示的内容

                            endLine = nowLine;
                            page++;

                            Log.e("fenyeservice", "几页" + pageCount + "viewnum" + viewNums + "viewline==" + viewline + ",pageline" + pageLine);
                            Log.e("fenyeservice", "跨页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "   " + startChar + "..." + endChar);


                            pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + (endChar));
//                                Constant.indexPage.put(bookName, pageInfo);
                            // Log.e("TAG", "over 1 page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                        } else {//下一页

                            startLine = nowLine;
                            startChar = endChar;
                            endChar = startChar + viewNums * viewLines;
                            page++;

                            Log.e("fenyeservice", "下一页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "  " + startChar + "..." + endChar);
                            pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//                                Constant.indexPage.put(bookName, pageInfo);
                            endLine = nowLine;
                            // Log.e("TAG", "over " + (i + 1) + " page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                        }
                    }
                    if (endChar < lineTxt.length()) {
                        //开始下一页,剩下的内容可能占据好几行
                        startLine = nowLine;
                        startChar = endChar;
                        int txtLen = lineTxt.substring(startChar).length();
                        int txtLine = txtLen / viewNums;//剩下的内容所在的行数
                        if (txtLine * viewNums < txtLen) {
                            txtLine++;
                        }
                        pageLine = txtLine;
                    } else {
                        startLine = nowLine + 1;
                        pageLine = 0;
                        startChar = 0;
                        endChar = 0;
                    }
                    // Log.e("TAG", "over page last:" + pageLine);
                }
            }
            endTime = System.currentTimeMillis();
            Log.e("endtime", endTime + " ");
            Log.e("costtime", (endTime - startTime) + "  " + endTime + " " + startTime);
            Log.e("fenyeservice", pageLine + " == pageline");
            //这一章节的最后一些内容不够一页的 这是为了从*网络*读取小说的时候 一章一章读取 要取到最后一页
            if (pageLine > 0) {
                page++;
                Log.e("fenyeservice", "不够一页");
                pageInfo.add(page + "|" + (chapter) + "|" + startLine + "|" + (nowLine + 1) + "|" + startChar + "|" + endChar);
//                if (infoUtils.indexPage.get(bookName) == null) {
//                    infoUtils.indexPage.put(bookName, pageInfo);
//                } else {
//                    for (String a : pageInfo) {
//                        infoUtils.indexPage.get(bookName).add(a);
//                    }
//                }
//                    infoUtils.indexPage.put(bookName, pageInfo);
                Log.e("fenyeservice", "last page info:" + bookName + page + "," + chapter + "," + startLine + "," + (nowLine + 1) + "," + startChar + "," + endChar);
            }

            chapterInfo.add((chapterPage) + "|" + (chapter) + "|" + lastChapter + "|" + chapterStartLine + "|" + chapterEndLine + "|" + 1 + "|" + "终");
            if (infoUtils.indexChapter.get(bookName) == null) {
                infoUtils.indexChapter.put(bookName, chapterInfo);
            } else {
                for (String a : chapterInfo) {
                    infoUtils.indexChapter.get(bookName).add(a);
                }
            }
//                infoUtils.indexChapter.put(bookName, chapterInfo);
//            Log.e("qwer", infoUtils.indexPage.containsKey(bookName) + "");
            Log.e("fenyeservice chatinfo", infoUtils.indexChapter.get(bookName).size() + "chapter info:" + chapterPage + "," + (chapter) + "," + lastChapter + "," + chapterStartLine + "," + chapterEndLine + "," + 1);
            //最后一章的信息
            reader.close();
            //Log.e("TAG", "divide page complete:" + (System.currentTimeMillis() - startTime) + ":needOpen:" + needOpen);


            //内置小说或者wifi传书在这个时候发送分页完成的广播，数据可从内存中读取


            Log.e("save123", "存储本地");
            saveChapterInfosNatetive(bookName, chapterInfo, filePath);
            savePageInfosNative(bookName, pageInfo, taskId, isCacheOne, false);
            Message msg = Message.obtain();
            msg.what = CACHE_COMPLETE;
            msg.arg1 = isCacheOne;
            msg.arg2 = chapter;
            msg.obj = bookName;
            mHandler.sendMessage(msg);
            //保存信息

        } catch (Exception e) {
            Log.e("error", "qwe" + e.getMessage() + '\n' + e.toString());
            e.printStackTrace();
        }
    }

    private synchronized void divideNetPage(String bookName, String chapterName, int chapterNO, String filePath, String encoding, int taskId, int isCacheOne, boolean NeedOpen, ArrayList<ChapterBean> list, int booktype) {
        try {

            SPHelper.setSaveInfo(this, true);
            Log.e("TAG", "dividePage");
            Boolean needOpen;
            int times = 0;
            if (list == null || list.size() == 0) {
                needOpen = true;
                times = 1;
                Log.e("times", times + " 本章");
            } else {
                needOpen = false;
                times = list.size();
                Log.e("times", times + " else");
            }

            long startTime = System.currentTimeMillis();
            List<String> pageInfo = new ArrayList<String>();
            List<String> chapterInfo = new ArrayList<String>();
            for (int chapters = 0; chapters < times; chapters++) {
                int chapter = chapterNO; //章节数,从数据库中读取，因为一章一章下载分页
                File file;
//                null是直接进来分页 =0是当前只有缓存一章的情况下去换字体
                if (list == null || list.size() == 0) {
                    needOpen = NeedOpen;
                    Log.e("filepath", needOpen + "当前 " + filePath);
                    file = new File(filePath);
                } else {
//                    除当前外的其他页做后台分页处理
                    needOpen = NeedOpen;
                    chapter = list.get(chapters).getChapterId();
                    Log.e("filepath", needOpen + "else" + list.get(chapters).getChapterPosition() + "  ");
                    file = new File(list.get(chapters).getChapterPosition());
                }
                Log.e("tag", "fileis exists" + file.exists());
                if (!file.exists()) {
                    return;
                }
                InputStream in = new FileInputStream(file);
                InputStreamReader inReader = new InputStreamReader(in, encoding);
                BufferedReader reader = new BufferedReader(inReader);

                String lineTxt;
                int page = 0;
                int chapterPage = 0;//章节开始的页号
                String lastChapter = chapterName;//上一章标题的
                int viewLines = SPHelper.getBookLines(this);//- SPHelper.getCutLines(this);//少填充一行作为调整

                int viewNums = SPHelper.getCurTxtNums(this);
                Log.e("fenyeservice", "textsize" + SPHelper.getBookTextSize(this) + "viewnum" + viewNums + " " + SPHelper.getBookLines(this) + "  " + SPHelper.getCutLines(this));

                int nowLine = 0;//当前是文件的第几行
                int startLine = 0; //这页从文件的第几行开始,每个文件是唯一的值
                int startChar = 0; //这页从startLine行的第几个汉字开始，每行是唯一的值
                int endLine = 0; //这页到文件的第几行结束,每个文件是唯一的值
                int endChar = 1; //这页到endLine行的第几个汉字结束，每行是唯一的值
                int pageLine = 0;//当前页到第几行了，pageLine<=viewLines
                int chapterStartLine = 0;//章节开始行数
                int chapterEndLine = 0;//章节结束行数
                int qw = 0;
                boolean firstchapter = true;

                boolean hasIntroduction = false;//是否有引言
                //还未读完
                while ((lineTxt = reader.readLine()) != null) {
                    if (nowLine == 0) {
                        pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
                        page++;
                    }
                    nowLine++;
                    lineTxt = ToDBC(lineTxt);
//                    if (lineTxt.lastIndexOf(" ")!=-1){
                    lineTxt = "  " + lineTxt.trim();
//                    if (judgeChapter(lineTxt) && isChapter(chapterEndLine, nowLine) && firstchapter) {
//                        firstchapter = false;
//                        if (nowLine > 1) {
//                            page++;
//                        }
//                        if (page != 0) {
//                            Log.e("fenyeservice", "这是标题" + lineTxt);
//                            Log.e("fenyeservice", "标题筛选 " + lineTxt + "page" + (page - 1) + "chapter" + chapter + "startLine" + startLine + "endLine" + (nowLine) + "   " + startChar + "..." + endChar);
//                            if (page != 0) {
//                            }
//                        }
//                        page++;
//
//
//                        endChar = 0;
//                        if (nowLine > 1) {
//
//                            hasIntroduction = true;
//                            pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//
//
//                            Log.e("fenyeservice", "page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
//                        } else {
//                            hasIntroduction = false;
//                        }
//                        //没有引言的时候，章标题就是这一章的标题
//                        if (!hasIntroduction) {
//                            if (nowLine > 1) {//第一次不执行加一操作，因为从1开始
//                                chapter++;
//                                chapterStartLine = chapterEndLine + 1;
//                                chapterPage += page;
//                            }
//                        }
//                        startLine = nowLine + 1;
//                        pageLine = 0;
//                        startChar = 0;
//                        chapterEndLine = nowLine;
//                        if (nowLine > 1) {
//                            Log.e("chapterinfo", lineTxt + "chapterPage" + (chapterPage = chapterPage - 1 > 0 ? chapterPage : 0) + "chapter" + (chapter) + "lastChapter" + lastChapter + "chapterStartLine" + chapterStartLine + "chapterEndLine" + chapterEndLine);
//                            chapterInfo.add(chapterPage + "|" + chapter + "|" + lastChapter + "|" + chapterStartLine + "|" + chapterEndLine + "|" + 0);
////                            Constant.indexChapter.put(bookName, chapterInfo);
//                        }
//                        // Log.e("TAG", "chapter info:" + page + "," + chapter + "," + lastChapter + "," + chapterStartLine + "," + chapterEndLine + "," + 0);
//                        if (hasIntroduction) {//有引言的时候，章标题是下一章的标题
//                            chapter++;
//                            chapterStartLine = chapterEndLine + 1;
//                            chapterPage += page;
//                        }
//                        lastChapter = lineTxt;
//                        page = 0;
//                        continue;
//                    }
                    int lineTxtLen = lineTxt.trim().length() + 2;
                    int viewline = lineTxtLen / viewNums; //这行抵屏幕多少行

                    if (viewline == 0) {
                        //不够一行当一行
                        viewline = 1;
                    } else if (viewline * viewNums < lineTxtLen) {
                        //不能整除的，行数加1
                        viewline++;
                        Log.e("fenyeservice", "lineTxtLen==" + lineTxtLen + ",lineTxt   " + lineTxt + ",viewline==" + viewline + "pageLine" + pageLine);

                    }
//                 Log.e("TAG", "need lines:" + viewline+":"+lineTxt);
                    //计算当前总行数，可能超出能显示的行数
                    int lineTmpCount = viewline + pageLine;
                    if (lineTmpCount < viewLines) {
                        //在一页范围内
                        pageLine += viewline;
                        Log.e("fenyeservice", "pageline" + pageLine);

                    } else if (lineTmpCount == viewLines) {
                        //正好满一页
                        page++;
                        endLine = nowLine;
                        endChar = lineTxtLen;
                        Log.e("fenyeservice", "满页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "   " + startChar + "..." + endChar);
                        pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//                        Constant.indexPage.put(bookName, pageInfo);
//                      Log.e("TAG", "just complete page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                        //下一页的开始
                        startLine = nowLine + 1;
                        pageLine = 0;
                        startChar = 0;
                        endChar = 0;
                    } else {
                        //这行跨页了
                        int pageCount = lineTmpCount / viewLines;//计算当前的行数需要几页
                        //取上整,页数不需要去上整
//                    if (pageCount * viewLines < lineTmpCount) {
//                        pageCount++;
//                    }
                        //Log.e("TAG", "over page:" + pageCount + "," + lineTxt.length() + "," + lineTxt);
                        for (int i = 0; i < pageCount; i++) {
                            if (i == 0) {//还在这一页
                                startChar = endChar;
//

                                endChar = viewNums * (viewLines - pageLine);//这一页上能显示的内容

                                endLine = nowLine;
                                page++;

                                Log.e("fenyeservice", "几页" + pageCount + "viewnum" + viewNums + "viewline==" + viewline + ",pageline" + pageLine);
                                Log.e("fenyeservice", "跨页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "   " + startChar + "..." + endChar);


                                pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + (endChar));
//                                Constant.indexPage.put(bookName, pageInfo);
                                // Log.e("TAG", "over 1 page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                            } else {//下一页

                                startLine = nowLine;
                                startChar = endChar;
                                endChar = startChar + viewNums * viewLines;
                                page++;

                                Log.e("fenyeservice", "下一页" + "page" + page + "chapter" + chapter + "startLine" + startLine + "endLine" + endLine + "  " + startChar + "..." + endChar);
                                pageInfo.add(page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar);
//                                Constant.indexPage.put(bookName, pageInfo);
                                endLine = nowLine;
                                // Log.e("TAG", "over " + (i + 1) + " page info:" + page + "," + chapter + "," + startLine + "," + endLine + "," + startChar + "," + endChar);
                            }
                        }
                        if (endChar < lineTxt.length()) {
                            //开始下一页,剩下的内容可能占据好几行
                            startLine = nowLine;
                            startChar = endChar;
                            int txtLen = lineTxt.substring(startChar).length();
                            int txtLine = txtLen / viewNums;//剩下的内容所在的行数
                            if (txtLine * viewNums < txtLen) {
                                txtLine++;
                            }
                            pageLine = txtLine;
                        } else {
                            startLine = nowLine + 1;
                            pageLine = 0;
                            startChar = 0;
                            endChar = 0;
                        }
                        // Log.e("TAG", "over page last:" + pageLine);
                    }
                }
                Log.e("fenyeservice", pageLine + " == pageline");
                //这一章节的最后一些内容不够一页的 这是为了从*网络*读取小说的时候 一章一章读取 要取到最后一页
                if (pageLine > 0) {
                    page++;
                    Log.e("fenyeservice", "不够一页");
                    pageInfo.add(page + "|" + (chapter) + "|" + startLine + "|" + (nowLine + 1) + "|" + startChar + "|" + endChar);
//                    if (infoUtils.indexPage.get(bookName) == null) {
//                        infoUtils.indexPage.put(bookName, pageInfo);
//                    } else {
//                        for (String a : pageInfo) {
//                            infoUtils.indexPage.get(bookName).add(a);
//                        }
//                    }
//                    infoUtils.indexPage.put(bookName, pageInfo);
                    Log.e("chapterinfo", "last page info:" + bookName + (chapterPage + 1) + "," + chapter + "," + startLine + "," + (nowLine + 1) + "," + startChar + "," + endChar);
                }

                chapterInfo.add((chapterPage + 1) + "|" + (chapter) + "|" + lastChapter + "|" + chapterStartLine + "|" + chapterEndLine + "|" + 1);
                if (infoUtils.indexChapter.get(bookName) == null) {
                    infoUtils.indexChapter.put(bookName, chapterInfo);
                } else {
                    for (String a : chapterInfo) {
                        infoUtils.indexChapter.get(bookName).add(a);
                    }
                }
//                infoUtils.indexChapter.put(bookName, chapterInfo);
//                Log.e("qwer", infoUtils.indexPage.containsKey(bookName) + "");
                Log.e("fenyeservice chatinfo", infoUtils.indexChapter.get(bookName).size() + "chapter info:" + chapterPage + "," + (chapter) + "," + lastChapter + "," + chapterStartLine + "," + chapterEndLine + "," + 1);
                //最后一章的信息
                reader.close();
                //Log.e("TAG", "divide page complete:" + (System.currentTimeMillis() - startTime) + ":needOpen:" + needOpen);

                Log.e("fenyeservice needopen", needOpen + " ");
                if (pageInfo.size() > 100 && needOpen) {//内置小说或者wifi传书在这个时候发送分页完成的广播，数据可从内存中读取
                    Message msg = Message.obtain();
                    msg.what = CACHE_COMPLETE;
                    msg.arg1 = isCacheOne;
                    msg.arg2 = chapter;
                    msg.obj = bookName;
                    mHandler.sendMessage(msg);
                }
            }

            Log.e("save123", "存储网络");
            saveChapterInfos(bookName, chapterInfo, filePath);
            savePageInfos(bookName, pageInfo, taskId, isCacheOne, needOpen);

            //保存信息

        } catch (Exception e) {
            Log.e("error", "qwe");
            e.printStackTrace();
        }
    }

    /**
     * @param bookname
     * @param chapterInfo
     * @param filePath    用来存储 本地小说和WIFI传书小说的章节信息 存在本地
     */
    private void saveChapterInfosNatetive(String bookname, List<String> chapterInfo, String filePath) {
//        String bookinfo = indexChapter.get(bookname).get(chapter - 1);
        List<ChapterBean> list = new ArrayList<>();
        for (String a : chapterInfo) {
            String[] info = a.split("\\|");
            Log.e("aaq", "info " + Integer.valueOf(info[1]) + "   " + info[6]);
            ChapterBean bean = new ChapterBean();
            //章节的位置
            bean.setChapterPosition(filePath);
            bean.setPage(Integer.valueOf(info[0]));
            bean.setChapterId(Integer.valueOf(info[1]));

            bean.setChapterName(info[2]);
            bean.setChapterStart(Long.parseLong(info[3]));
            bean.setChapterEnd(Long.parseLong(info[4]));
            bean.setIsLastChapter(Integer.valueOf(info[5]));
            bean.setChapterN((info[6]));
            bean.setIsRead(0);
            list.add(bean);
        }
        if (infoUtils.chapterDetail.get(bookname) == null) {
            infoUtils.chapterDetail.put(bookname, list);
        } else {
            for (ChapterBean a : list) {
                infoUtils.chapterDetail.get(bookname).add(a);
            }
        }
        SPHelper.setObject(this, "chapterDetail", infoUtils.chapterDetail);

//        Log.e("setObject 1", infoUtils.chapterDetail.size() + " " + a.size() + " " + a);
        SPHelper.setSaveInfo(this, false);
    }

    /**
     * 用来存储网络小说的数据 存在本地
     */
    //将分页信息保存起来
    //将分页信息保存起来
    private void savePageInfosNative(String bookName, List<String> infos, int taskId, int isCacheOne, Boolean needOpen) {
        String firstInfo = infos.get(0);
        List<PageBean> list = new ArrayList<>();
        String[] firstInfos = firstInfo.split("\\|");
        int lastChapterId = Integer.valueOf(firstInfos[1]);
        for (int i = 0; i < infos.size(); i++) {
//            Log.e("qwe1", "2");
            String info = infos.get(i);
            //Log.e("TAG","page info:"+info);
            String[] pageInfos = info.split("\\|");
            PageBean bean = new PageBean();
            bean.setPage(Integer.valueOf(pageInfos[0]));
            bean.setChapterId(Integer.valueOf(pageInfos[1]));
            bean.setPageStartLine(Integer.valueOf(pageInfos[2]));
            bean.setPageEndLine(Integer.valueOf(pageInfos[3]));

            bean.setPageStartChar(Integer.valueOf(pageInfos[4]));
            bean.setPageEndChar(Integer.valueOf(pageInfos[5]));
            list.add(bean);
//            //删除该章节的多余页数
//            if (lastChapterId != Integer.valueOf(pageInfos[1])) {
//                String lastInfo = infos.get(i - 1);//取拿上一章的最后一页
//                String[] lastInfos = lastInfo.split("\\|");
//                pageDBHelper.deletePage(lastChapterId, Integer.valueOf(lastInfos[0]));
//                lastChapterId = Integer.valueOf(pageInfos[1]);
//            }
        }

        mStartTask.remove(bookName + ":" + taskId);
//        SPHelper.setSaveInfo(this, false);
        if (infoUtils.pageDetail.get(bookName) == null) {
            infoUtils.pageDetail.put(bookName, list);
        }
        SPHelper.setObject(this, "pageDetail", infoUtils.pageDetail);

//        Log.e("setObject", infoUtils.pageDetail + " ");
//        Log.e("setObject", infoUtils.pageDetail.size() + " " + list.size() + "  "
//                + a.size() + " " + a);
        Log.e("sendcast", (infos.size() < 100) + " " + needOpen);
        if (infos.size() < 100 && needOpen) {//网络小说在这个时候发送分页完成的广播，因为网络小说直接从数据库中读取
            Message msg = Message.obtain();
            msg.what = CACHE_COMPLETE;
            msg.arg1 = isCacheOne;
            msg.arg2 = lastChapterId;
            msg.obj = bookName;
            mHandler.sendMessage(msg);
        }

    }

    /**
     * 用来存储网络小说的数据 存在数据库
     */
    private void savePageInfos(final String bookName, final List<String> infos, final int taskId, final int isCacheOne, Boolean needopen) {
        PageDBHelper pageDBHelper = new PageDBHelper(BookIndexService.this, bookName);
        Log.e("qwe1", "1");
        String firstInfo = infos.get(0);
        String[] firstInfos = firstInfo.split("\\|");
        int lastChapterId = Integer.valueOf(firstInfos[1]);
        for (int i = 0; i < infos.size(); i++) {
//            Log.e("qwe1", "2");
            String info = infos.get(i);
            //Log.e("TAG","page info:"+info);
            String[] pageInfos = info.split("\\|");
            PageBean bean = new PageBean();
            bean.setPage(Integer.valueOf(pageInfos[0]));
            bean.setChapterId(Integer.valueOf(pageInfos[1]));
            bean.setPageStartLine(Integer.valueOf(pageInfos[2]));
            bean.setPageEndLine(Integer.valueOf(pageInfos[3]));

            bean.setPageStartChar(Integer.valueOf(pageInfos[4]));
            bean.setPageEndChar(Integer.valueOf(pageInfos[5]));
            pageDBHelper.writePage(bean);
            //删除该章节的多余页数
            if (lastChapterId != Integer.valueOf(pageInfos[1])) {
                String lastInfo = infos.get(i - 1);//取拿上一章的最后一页
                String[] lastInfos = lastInfo.split("\\|");
                pageDBHelper.deletePage(lastChapterId, Integer.valueOf(lastInfos[0]));
                lastChapterId = Integer.valueOf(pageInfos[1]);
            }
        }
        Log.e("qwe1", "3");
        //删除最后一章的多余内容
        String lastInfo = infos.get(infos.size() - 1);
        String[] lastInfos = lastInfo.split("\\|");
        pageDBHelper.deletePage(lastChapterId, Integer.valueOf(lastInfos[0]));
        Log.e("isneedopen", needopen + " ");
        if (infos.size() < 100 && needopen) {//网络小说在这个时候发送分页完成的广播，因为网络小说直接从数据库中读取
            Message msg = Message.obtain();
            msg.what = CACHE_COMPLETE;
            msg.arg1 = isCacheOne;
            msg.arg2 = lastChapterId;
            msg.obj = bookName;
            mHandler.sendMessage(msg);
        }
        //Constant.indexPage.remove(bookName);

        mStartTask.remove(bookName + ":" + taskId);
        pageDBHelper.closeDB();
        SPHelper.setParsePageComplete(BookIndexService.this, bookName, true);
        //Log.e("TAG", "save page complete");
    }

    /**
     * @param bookName
     * @param chapters
     * @param filePath 用来存储 网络小说的章节信息 存在数据库
     */
    //将章节信息保存起来
    private void saveChapterInfos(final String bookName, final List<String> chapters, final String filePath) {
        ChapterDBHelper chapterDBHelper = new ChapterDBHelper(BookIndexService.this, bookName);
        for (int i = 0; i < chapters.size(); i++) {
            String chapter = chapters.get(i);
            String[] info = chapter.split("\\|");
            //Log.e("TAG", "chapter info:" + chapter);
            ChapterBean bean = new ChapterBean();
            //章节的位置
            bean.setChapterPosition(filePath);
            bean.setPage(Integer.valueOf(info[0]));
            bean.setChapterId(Integer.valueOf(info[1]));
            bean.setChapterName(info[2]);
            bean.setChapterStart(Long.parseLong(info[3]));
            bean.setChapterEnd(Long.parseLong(info[4]));
            bean.setIsLastChapter(Integer.valueOf(info[5]));
            bean.setIsRead(0);
            chapterDBHelper.writeBook(bean);
        }
        chapterDBHelper.closeDB();
        //Constant.indexChapter.remove(bookName);
        SPHelper.setParseChapterComplete(BookIndexService.this, bookName, true);
        //Log.e("TAG", "save chapter complete");
    }

    //判断两个章标题之间是否间隔足够
    private boolean isChapter(int lastLine, int curLine) {
        if (lastLine == 0) {
            return true;
        } else {
            return curLine - lastLine > 10;
        }
    }

    int preLength = 0;

    //判断是否是章标题
    private boolean judgeChapter(String str) {
        str = str.trim();
        if (!str.startsWith("第")) {
            return false;
        }
        if (str.startsWith("(") || str.startsWith("（")) {
            return false;
        }
        if (str.length() > 50)
            return false;
        int firstPos = str.indexOf("第");
        if (firstPos < 0) {
            return false;
        }
//        if (firstPos >= 4) {
//            return false;
//        }
        int newLength = 0;
        int endPos = str.indexOf("章");


        if (endPos > 0) {

        } else {
            endPos = str.indexOf("回");
        }
//        if (preLength == 0) {
//            preLength = (str.substring(endPos)).length();
//        } else {
//            newLength = (str.substring(endPos)).length();
//            if (newLength > 12) {
//                return Math.abs(newLength - preLength) < 6;
//            }
//            preLength = newLength;
//        }

        if (endPos < 0) {
            return false;
        }
        Log.e("judge", endPos + "  " + firstPos + "  " + str);
        return endPos - firstPos < 12;

    }

    // 判断是否是章标题
    private boolean judgeChapterName(String str) {
        String regex = ".*(第.*章).*";
        Pattern pattern = Pattern.compile(regex);//每次编译速度很慢
        Matcher matcher = pattern.matcher(str.trim());
        if (str.length() < 50 && matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }
}
