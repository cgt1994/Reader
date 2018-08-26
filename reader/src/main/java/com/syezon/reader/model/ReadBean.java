package com.syezon.reader.model;

import java.io.Serializable;

/**
 * 真正读取的时候的缓存bean
 * Created by jin on 2016/10/8.
 */
public class ReadBean implements Serializable {
    private int chapterID;
    private String chapterName;
    private int start;
    private int end;
    private ReadBean preBean;
    private ReadBean nextBean;

    public int getChapterID() {
        return chapterID;
    }

    public void setChapterID(int chapterID) {
        this.chapterID = chapterID;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public ReadBean getPreBean() {
        return preBean;
    }

    public void setPreBean(ReadBean preBean) {
        this.preBean = preBean;
    }

    public ReadBean getNextBean() {
        return nextBean;
    }

    public void setNextBean(ReadBean nextBean) {
        this.nextBean = nextBean;
    }
}
