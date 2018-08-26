package com.syezon.reader.utils;

import android.content.Context;
import android.util.Log;

import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.model.PageBean;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jin on 2016/12/16.
 */
public class InfoUtils {

    private Context context;

    //    章节对应的图片展现
    public static int RANDOM = 1;

    //    缓存章节数 key是书名
    public static ArrayList<String> cachingBook = new ArrayList<>();
    //    轮播图片数组
    public static JSONArray showPic = new JSONArray();
    //章节信息
    public Map<String, List<String>> indexChapter = new HashMap<>();
    //        书本具体分页后的章信息
    public Map<String, List<ChapterBean>> chapterDetail = new HashMap<>();
    //        书本具体分页后页信息
    public Map<String, List<PageBean>>
            pageDetail = new HashMap<>();

    private static InfoUtils instance = null;
    private Boolean firstGetInfo = false;

    public InfoUtils(Context context) {
        this.context = context;
        if (!firstGetInfo) {
            init(context);
            firstGetInfo = true;
        }
    }

    private void init(Context context) {

        if ((SPHelper.getObject(context, "indexChapter")) != null) {

            indexChapter = SPHelper.getObject(context, "indexChapter");
            Log.e("getpage 1", "true" + indexChapter.size());
        }
        if ((SPHelper.getObject(context, "chapterDetail")) != null) {

            chapterDetail = SPHelper.getObject(context, "chapterDetail");
            Log.e("getpage 2", "true" + chapterDetail.size());
        }
        if ((SPHelper.getObject(context, "pageDetail")) != null) {

            pageDetail = SPHelper.getObject(context, "pageDetail");
            Log.e("getpage 3", "true" + pageDetail.size());

        }

    }

    public static InfoUtils getInstance(Context context) {
        if (instance == null) {
            instance = new InfoUtils(context);
        }
        return instance;
    }

    /**
     * @param bookname 对应书名
     * @param chapter  对应章节
     * @return 返回对应章节信息 信息存储在ChapterBean里
     */
    public ChapterBean readChapter(String bookname, int chapter) {
        List<String> chaptersList = instance.indexChapter.get(bookname);
        if (chaptersList != null) {
            for (String a : chaptersList) {
                String[] info = a.split("\\|");
                ChapterBean bean = new ChapterBean();
                //章节的位置
//        bean.setChapterPosition(filePath);
                bean.setPage(Integer.valueOf(info[0]));
                bean.setChapterId(Integer.valueOf(info[1]));
                if (chapter == Integer.valueOf(info[1])) {
                    bean.setChapterName(info[2]);
                    bean.setChapterStart(Long.parseLong(info[3]));
                    bean.setChapterEnd(Long.parseLong(info[4]));
                    bean.setIsLastChapter(Integer.valueOf(info[5]));
                    bean.setIsRead(0);
                    return bean;
                }
            }
        }
        return null;
//        bean.setChapterPosition(get);

    }

    /**
     * @param bookname 要查找的书本名
     *                 page + "|" + chapter + "|" + startLine + "|" + endLine + "|" + startChar + "|" + endChar String 存储格式
     * @return 对应书本的所有章节信息
     */
    public List<ChapterBean> readChapterList(String bookname) {
        List<ChapterBean> list = new ArrayList<>();
        List<String> bookinfos = indexChapter.get(bookname);
        for (String a : bookinfos) {
            ChapterBean bean = new ChapterBean();
            String[] info = a.split("\\|");
            bean.setChapterId(Integer.valueOf(info[1]));
            bean.setChapterName(info[2]);
            bean.setChapterStart(Long.parseLong(info[3]));
            bean.setChapterEnd(Long.parseLong(info[4]));
            list.add(bean);
        }
        return list;


    }

    /**
     * @param bookname 对应书名
     * @param chapter  对应章节
     * @param page     对应页数
     * @return 返回这一页显示什么内容 具体信息封装在PageBean里
     */
    public PageBean getPageInfo(String bookname, int chapter, int page) {
        List<PageBean> list = pageDetail.get(bookname);
//        List<String> list =indexPage.get(bookname)
        int i = 0;
        for (PageBean a : list) {
            if (a.getPage() == page && a.getChapterId() == chapter) {
                return list.get(i);
            }
            i++;
            Log.e("list11", "need" + chapter + " " + page + " " + a.getChapterId() + " " + a.getPage() + " " + a.getPageStartLine() + " " + a.getPageEndLine());
        }


        return null;

    }

    /**
     * @param bookname 对应书名
     * @param chapter  对应章节
     * @return 返回章节的数目
     */
    public int getTotalPages(String bookname, int chapter) {
        List<PageBean> list = pageDetail.get(bookname);

        if (list == null) {
            ChapterBean chapterbean = readChapterDetail(bookname, chapter);
            ChapterBean chapterBean2 = readChapterDetail(bookname, chapter);
            if (chapterbean != null && chapterBean2 != null) {

                return chapterbean.getPage() - chapterBean2.getPage();
            }
        }
        int count = 0;
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getChapterId() == chapter) {
                count++;

            } else {
                if (count != 0) {
                    return count;
                }
            }
            i++;
        }

        return -1;
    }

    /**
     * @param bookname 移除所有这本书的分页信息 下次再打开需要重新启动分页
     */
    public void remove(String bookname) {

        indexChapter.remove(bookname);
        chapterDetail.remove(bookname);
        pageDetail.remove(bookname);
    }

    public void remove() {
        indexChapter.clear();
        chapterDetail.clear();
        pageDetail.clear();
    }

    public static ChapterBean readChapterDetail(List<ChapterBean> chapterBeanList, String mBookName, int chapter) {

//        Log.e("qwes",chapterBeanList)
        for (ChapterBean a : chapterBeanList) {
            if (a.getChapterId() == chapter) {
                return a;
            }
        }
        return null;


    }

    public ChapterBean readChapterDetail(String mBookName, int chapter) {
        Log.e("hia", chapter + " ");
        List<ChapterBean> chapterBeanList = chapterDetail.get(mBookName);
//        Log.e("qwes",chapterBeanList)
        if (chapterBeanList != null) {


            for (ChapterBean a : chapterBeanList) {
                if (a.getChapterId() == chapter) {
                    return a;
                }
            }
        }
        return null;


    }
}
