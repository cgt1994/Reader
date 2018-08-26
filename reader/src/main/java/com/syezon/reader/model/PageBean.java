package com.syezon.reader.model;

import java.io.Serializable;

/**
 * Created by jin on 2016/9/30.
 */
public class PageBean implements Serializable {
    private int page;
    private int chapterId;
    private int pageStartLine;
    private int pageEndLine;
    private int pageStartChar;
    private int pageEndChar;
    private int isEnd;
    private int cutLength;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public int getPageStartLine() {
        return pageStartLine;
    }

    public void setPageStartLine(int pageStartLine) {
        this.pageStartLine = pageStartLine;
    }

    public int getPageEndLine() {
        return pageEndLine;
    }

    public void setPageEndLine(int pageEndLine) {
        this.pageEndLine = pageEndLine;
    }

    public int getPageStartChar() {
        return pageStartChar;
    }

    public void setPageStartChar(int pageStartChar) {
        this.pageStartChar = pageStartChar;
    }

    public int getPageEndChar() {
        return pageEndChar;
    }

    public void setPageEndChar(int pageEndChar) {
        this.pageEndChar = pageEndChar;
    }

    public int getIsEnd() {
        return isEnd;
    }

    public void setIsEnd(int isEnd) {
        this.isEnd = isEnd;
    }

    public int getCutLength() {
        return cutLength;
    }

    public void setCutLength(int cutLength) {
        this.cutLength = cutLength;
    }
}
