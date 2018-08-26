package com.syezon.reader.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.syezon.reader.constant.Constant;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.db.ChapterDBHelper;
import com.syezon.reader.db.PageDBHelper;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.model.ChapterBean;
import com.syezon.reader.model.PageBean;
import com.syezon.reader.widget.ICacheFileListener;

import java.io.File;
import java.util.List;

/**
 * 书籍解析，
 * 当内置小说和WiFi传书分页完成之后处理和网络小说一致
 * Created by jin on 2016/8/25.
 */
public class BookFactory {

    private ChapterDBHelper mChapterDBHelper;
    private PageDBHelper mPageDBHelper;
    private String mBookName;
    private Context mContext;

    private BookCaseBean mBookCaseBean;
    private ChapterBean mCurChapter;//当前章节信息
    private ICacheFileListener mListener;
    private List<ChapterBean> chapterBeanList;
    private InfoUtils infoUtils;

    public BookFactory(Context context, String bookName, ICacheFileListener listener) {
        mChapterDBHelper = new ChapterDBHelper(context, bookName);
        mPageDBHelper = new PageDBHelper(context, bookName);
        BookCaseDBHelper caseDBHelper = new BookCaseDBHelper(context);
        mBookCaseBean = caseDBHelper.selectBookCaseByName(bookName);
        mBookName = bookName;
        mContext = context;
        infoUtils = InfoUtils.getInstance(mContext);
        mListener = listener;
    }

    //设置当前章节
    public void setCurChapter(int chapter) {
        mCurChapter = mChapterDBHelper.readBookOneChapter(chapter);
    }

    public String getNextChapterName(int chater) {
        ChapterBean chapterBean = infoUtils.readChapterDetail(mBookName, chater);
        if (chapterBean != null) {
            String chaptername = chapterBean.getChapterName();
            return chaptername;
        } else {
            return "引言";
        }

    }

    //根据页数索引来读取内容
    public String readBookByPage(boolean isNetBook, int chapter, int page, String encoding) {
        if (isNetBook) {
            page = page <= 0 ? 1 : page;
            Log.e("get8 read", "要读的章节" + chapter + "要读的页数" + page);
            PageBean bean = mPageDBHelper.getPageInfo(chapter, page);
            mCurChapter = mChapterDBHelper.readBookOneChapter(chapter);
            Log.e("bugcause", (mCurChapter == null) + "  " + (bean == null));
//            if (mCurChapter == null && bean == null) {
//                chapter = chapter - 1;
//                page = page + 1;
//                bean = mPageDBHelper.getPageInfo(chapter, page);
//                mCurChapter = mChapterDBHelper.readBookOneChapter(chapter);
//            }
            Log.e("bugcause", chapter + "  " + (mBookCaseBean.getLast_chapter_no()) + " ");
            if (mCurChapter != null && bean != null) {
                Log.e("readBookByPage", mCurChapter.getChapterPosition() + "  " + bean.getPageStartLine() + "  " + bean.getPageEndLine());
                return FileUtils.readFile(mCurChapter.getChapterPosition(), bean.getPageStartLine(), bean.getPageEndLine(), bean.getPageStartChar(), bean.getPageEndChar(), encoding);

            } else if (chapter == mBookCaseBean.getLast_chapter_no()) {
                return Constant.NO_MOREINFOMATION;
            } else {
                Log.e("bug1", "chapter" + chapter + "page" + page);
                return Constant.ERROR_INFOMATION;
            }
        } else {
            // Log.e("TAG", "chapter:" + chapter +", page:"+page );

            //如果内存中存在分页信息的话,通过WiFi传书
//            Log.e("bugp", mBookName + infoUtils.indexPage.containsKey(mBookName));
//            Iterator it = Constant.indexPage.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry entry = (Map.Entry) it.next();
//                Object key = entry.getKey();
//                Object value = entry.getValue();
//
//                Log.e("bugp", "key=" + key + " value=" + value);
//            }
            if (infoUtils.pageDetail.containsKey(mBookName)) {
//                Log.e("get9", "要读的章节" + chapter + "要读的页数" + page);
                Log.e("get8 ", chapter + "  " + page + "   " + getTotalPages(false, chapter));
                if (page > getTotalPages(false, chapter)) {
                    page = 1;
                    chapter = chapter + 1;
                }
                page = page > 1 ? page - 1 : 0;
                //去拿这一章节的开始页数


                String chapterInfo = infoUtils.indexChapter.get(mBookName).get(chapter - 1);
                String[] chapterInfos = chapterInfo.split("\\|");
                int startPage = Integer.parseInt(chapterInfos[0]);
                //Log.e("TAG","chapter info:"+chapterInfo+","+(page + startPage));
                Log.e("chat11", chapter + " " + page + "  " + startPage);
                PageBean pageBean = infoUtils.pageDetail.get(mBookName).get(page + startPage);
//                String info = infoUtils.indexPage.get(mBookName).get(page + startPage);
//
//                String[] infos = info.split("\\|");

                FileUtils utils = new FileUtils(SPHelper.getBookFilePath(mContext, mBookName), encoding);
                return utils.readFile(pageBean.getPageStartLine(), pageBean.getPageEndLine(), pageBean.getPageStartChar(), pageBean.getPageEndChar());
            } else {

                page = page <= 0 ? 1 : page;
                PageBean bean = infoUtils.getPageInfo(mBookName, chapter, page);
                mCurChapter = infoUtils.readChapter(mBookName, chapter);
                if (mCurChapter != null && bean != null) {
                    FileUtils utils = new FileUtils(mCurChapter.getChapterPosition(), encoding);
                    return utils.readFile(bean.getPageStartLine(), bean.getPageEndLine(), bean.getPageStartChar(), bean.getPageEndChar());
                } else if (infoUtils.readChapterList(mBookName).size() == chapter) {
                    return "作者正在努力码字中,\n先去看看其他书吧!";
                } else {
                    Log.e("bug2", "chapter" + chapter + "page" + page);
                    return "正在缓存请稍等...2";
                }
            }
        }
    }


    //根据当前页来获取章节名称
    public String getChapterInfo(boolean isNetBook, int chapter, int page) {
//        List<ChapterBean> chapterBeanList = mChapterDBHelper.readBookChapterList();
//        for (ChapterBean a : chapterBeanList) {
//            Log.e("qqqp", a.getChapterName());
//        }
        if (isNetBook) {
            ChapterBean bean = mChapterDBHelper.readBookOneChapter(chapter);

            if (bean != null) {

                return chapter + ":" + bean.getChapterName();
            } else {

//                this.chapterBeanList.get(chapter).getChapterName();
//                ChapterBean bean1 = Constant.readChapterDetail(Constant.readChapterList(mBookName), mBookName, chapter);
//                Log.e("ppp", " bean1" + (bean1 == null));
//                Log.e("ppp", "bean无效 " + chapter + ":" + bean.getChapterName());
                return chapter + ":" + " ";

            }
        } else {
            //从内存中读取
            if (infoUtils.pageDetail.containsKey(mBookName)) {
                Log.e("ppp", "chapter" + chapter);
                if (chapter == infoUtils.indexChapter.get(mBookName).size() + 1) {
                    return "完结";
                }
                Log.e("ppp 3", chapter + " " + page);
                if (page > getTotalPages(false, chapter)) {

                    chapter = chapter + 1;
                }
                String chapterInfo = infoUtils.indexChapter.get(mBookName).get(chapter - 1 >= 0 ? chapter - 1 : 0);
                String[] chapterInfos = chapterInfo.split("\\|");
                String chapterName = chapterInfos[2];
                Log.e("ppp 1", chapter + " " + chapterName);
                return chapter + ":" + chapterName;
            } else {
                ChapterBean bean = infoUtils.readChapter(mBookName, chapter);
                if (bean != null) {
                    Log.e("ppp 2", chapter + " " + bean.getChapterName());
                    return chapter + ":" + bean.getChapterName();
                } else {
                    return "1: ";
                }
            }
        }
    }

    //根据章节号来返回页数
    public int readBookByChapterNo(boolean isNetBook, int chapterNO) {
//        if (isNetBook) {
//            return 1;//网络书籍每章开头都是第一页
//        } else {
//            if (Constant.indexChapter.containsKey(mBookName)) {
//                String chapterInfo = Constant.indexChapter.get(mBookName).get(chapterNO - 1);//从0开始
//                String[] chapterInfos = chapterInfo.split("\\|");
//                return Integer.valueOf(chapterInfos[0]);
//            } else {
////                ChapterBean bean = mChapterDBHelper.readBookOneChapter(chapterNO);
//                return 1;//内置和wifi，不从内存中读取的时候和网络书籍一样
//            }
//        }
        return 1;
    }


    //判断是否还有后续页数
    public boolean hasNext(boolean isNetBook, int page, int chapter, String chapterName) {
        //Log.e("TAG", "hasNext:" + isNetBook + ":" + page + ":" + chapter + ":" + mPageDBHelper.getTotalPagesByChapter(chapter));
        if (isNetBook) {
            if (page < mPageDBHelper.getTotalPagesByChapter(chapter)) {//先读取已缓存的章节信息
                return true;
            }
            if (chapterName.equals(mBookCaseBean.getLast_chapter())) {//缓存到最新章节
                return false;
            } else {
                //检查本地是否缓存
                // chapter = mCurChapter != null ? mCurChapter.getChapterId() + 1 : chapter;
                if (checkFileExist(chapter + 1)) {//本地存在
                    return true;
                } else {//去缓存
                    Log.e("huancun4", "缓存" + (chapter + 1) + "  ");

                    mListener.onCacheFile(chapter + 1, 1);
//                        mListener.onCacheFile(chapter + 2, 1);


//                        if (SPHelper.getCache(mContext, mBookName, chapter + 2) == -1) {
//                            SPHelper.setCache(mContext, mBookName, chapter + 2);
//                            mListener.onCacheFile(chapter + 2, 1);
//                        }
                    //去缓存下一章节
                    return true;
                }
            }
        } else {
            if (infoUtils.pageDetail.containsKey(mBookName)) {//从内存中读取
                if (chapter != infoUtils.indexChapter.get(mBookName).size()) {
                    return true;
                } else {
                    String before = infoUtils.indexChapter.get(mBookName).get(infoUtils.indexChapter.get(mBookName).size() - 1);
                    String[] before2 = before.split("\\|");
                    int beforepage = Integer.parseInt(before2[0]);
                    beforepage = beforepage + page;
//                    Log.e("eee", beforepage + "# " + infoUtils.indexPage.get(mBookName).size());
                    return beforepage < infoUtils.pageDetail.get(mBookName).size();
                }
            } else if (page < infoUtils.getTotalPages(mBookName, chapter)) {//该章节的页数还没读取完
                Log.e("eee", page + " @" + infoUtils.getTotalPages(mBookName, chapter));
                return true;
            } else {//判断是否还有下一章节
                int totalChapter;
                //获取总章节数
                if (infoUtils.indexChapter.containsKey(mBookName)) {
                    totalChapter = infoUtils.indexChapter.get(mBookName).size();
                } else {
                    if (isNetBook) {
                        totalChapter = mChapterDBHelper.readBookChapterList().size();
                    } else {
                        totalChapter = infoUtils.readChapterList(mBookName).size();
                    }

                    if (totalChapter > chapter) {
                        Log.e("eee", totalChapter + " !" + chapter);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private Boolean sss = false;

    //是否还有前面章节,0表示当前章还有页，1表示要读取上一章节,-1表示没有了

    public boolean hasPre(boolean isNetBook, int page, int chapter) {
        // Log.e("TAG", "hasPre:" + isNetBook + ":" + page + ":" + chapter + ":" + mPageDBHelper.getTotalPagesByChapter(chapter));
        if (isNetBook) {//网络书籍
            if (page > 1) {//当前页数大于一
                return true;
            }
            if (chapter == 1) {//第一章
                return false;
            } else {
                //检查本地是否缓存
                //chapter = mCurChapter != null ? mCurChapter.getChapterId() - 1 : chapter;
                // Log.e("TAG","chapter:"+chapter);
                if (checkFileExist(chapter - 1)) {//本地存在

                    return true;
                } else {//去缓存
//                    Log.e("huancun", SPHelper.getCache(mContext, mBookName+(chapter-1))+"chapter" + chapter + "page" + page);

//                    if (SPHelper.getCache(mContext, mBookName+(chapter-1)) == -1) {
//                        SPHelper.setCache(mContext, mBookName+(chapter-1),0);

                    mListener.onCacheFile(chapter - 1, 0);

//                    Log.e("huancun123", "ttt" + chapter);
//                    mListener.onCacheFile(chapter - 1, 0);
//                        mListener.onCacheFile(chapter + 1, 0);

                    //去缓存上一章节
                    return true;
                }
            }
        } else {//本地书籍
            if (page > 1) {
                return true;//第一章的页数还未达到第一页
            } else {
                if (chapter > 1) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    //检查文件是否存在
    public boolean checkFileExist(int chapter) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/com.syezon.reader/book/" + MD5Util.encrypt(mBookName) + "/" + chapter);
        return file.exists();
    }

    //查询总页数
    public int getTotalPages(boolean isNetBook, int chapter) {
        if (isNetBook) {//网络书籍
            return mPageDBHelper.getTotalPagesByChapter(chapter) >= 1 ? mPageDBHelper.getTotalPagesByChapter(chapter) : 1;
        } else {//通WiFi传书
            if (infoUtils.pageDetail.containsKey(mBookName)) {

                String startChapterInfo = infoUtils.indexChapter.get(mBookName).get(chapter - 1 >= 0 ? chapter - 1 : 0);
                String[] startChapterInfos = startChapterInfo.split("\\|");
                int startPage = Integer.parseInt(startChapterInfos[0]);

                if (infoUtils.indexChapter.get(mBookName).size() == chapter) {
//                    String before = infoUtils.indexPage.get(mBookName).get(infoUtils.indexPage.get(mBookName).size() - 1);
                    PageBean pageBean = infoUtils.pageDetail.get(mBookName).get(infoUtils.pageDetail.get(mBookName).size() - 1);
//                    String[] before2 = before.split("\\|");
//                    int beforepage = Integer.parseInt(before2[0]);
//                    int befroechart = Integer.parseInt(before2[1]);
//                    Log.e("chapterpage", "chapter= " + chapter + " " + startPage + " " + beforepage + " " + befroechart);
                    return pageBean.getPage();

                }
                String endChapterInfo = infoUtils.indexChapter.get(mBookName).get(chapter);
                String[] endChapterInfos = endChapterInfo.split("\\|");
                int endPage = Integer.parseInt(endChapterInfos[0]);
                Log.e("chapterpage", "chapter==" + chapter + " " + endPage + " " + startPage);
                return endPage - startPage;//一章的总页数
            } else {
                return infoUtils.getTotalPages(mBookName, chapter) - 1 >= 1 ? infoUtils.getTotalPages(mBookName, chapter) - 1 : 1;
            }
        }
    }

    public void closeDB() {
        if (mChapterDBHelper != null) {
            mChapterDBHelper.closeDB();
        }
        if (mPageDBHelper != null) {
            mPageDBHelper.closeDB();
        }
    }
}
