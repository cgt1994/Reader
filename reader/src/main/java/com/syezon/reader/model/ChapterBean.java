package com.syezon.reader.model;

import java.io.Serializable;

/**
 * 书本实体类
 * Created by jin on 2016/8/25.
 */
public class ChapterBean implements Serializable {
    private String bookName;//书本名称
    private String chapterName;//章节名称
    private int chapterId;//章节号
    private int isRead;//是否已经阅过
    private String chapterPosition;//章节内容所在位置
    private long chapterStart;//章节开始位置
    private long chapterEnd;//章节结束位置
    public int page;//该章节对应的页数
    private int isLastChapter;//是否是最后一章

    public String getChapterN() {
        return chapterN;
    }

    public void setChapterN(String chapterN) {
        this.chapterN = chapterN;
    }

    private String chapterN;
    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public String getChapterPosition() {
        return chapterPosition;
    }

    public void setChapterPosition(String chapterPosition) {
        this.chapterPosition = chapterPosition;
    }

    public long getChapterStart() {
        return chapterStart;
    }

    public void setChapterStart(long chapterStart) {
        this.chapterStart = chapterStart;
    }

    public long getChapterEnd() {
        return chapterEnd;
    }

    public void setChapterEnd(long chapterEnd) {
        this.chapterEnd = chapterEnd;
    }

    public int getIsLastChapter() {
        return isLastChapter;
    }

    public void setIsLastChapter(int isLastChapter) {
        this.isLastChapter = isLastChapter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
