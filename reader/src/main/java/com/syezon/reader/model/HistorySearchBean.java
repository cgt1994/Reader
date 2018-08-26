package com.syezon.reader.model;

import java.io.Serializable;

/**
 * Created by jin on 2016/9/26.
 */
public class HistorySearchBean implements Serializable {
    private int id;
    private String searchContent;
    private String searchTime;

    public String getSearchContent() {
        return searchContent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    public String getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(String searchTime) {
        this.searchTime = searchTime;
    }
}
