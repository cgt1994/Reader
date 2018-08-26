package com.syezon.reader.model;

import java.io.Serializable;

/**
 * Created by jin on 2016/9/14.
 */
public class BookCaseBean implements Serializable {
    private int book_id;
    private String book_img;
    private String book_name;
    private String book_author;
    private String book_update_time;
    private String last_chapter;
    private int last_chapter_no;
    private String add_time;
    private int cache;//缓存章节
    private int is_full;
    private int book_type;

    public int getBook_id() {
        return book_id;
    }

    public void setBook_id(int book_id) {
        this.book_id = book_id;
    }

    public String getBook_img() {
        return book_img;
    }

    public void setBook_img(String book_img) {
        this.book_img = book_img;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_author() {
        return book_author;
    }

    public void setBook_author(String book_author) {
        this.book_author = book_author;
    }

    public String getBook_update_time() {
        return book_update_time;
    }

    public void setBook_update_time(String book_update_time) {
        this.book_update_time = book_update_time;
    }

    public String getLast_chapter() {
        return last_chapter;
    }

    public void setLast_chapter(String last_chapter) {
        this.last_chapter = last_chapter;
    }

    public int getLast_chapter_no() {
        return last_chapter_no;
    }

    public void setLast_chapter_no(int last_chapter_no) {
        this.last_chapter_no = last_chapter_no;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public int getCache() {
        return cache;
    }

    public void setCache(int cache) {
        this.cache = cache;
    }

    public int getIs_full() {
        return is_full;
    }

    public void setIs_full(int is_full) {
        this.is_full = is_full;
    }

    public int getBook_type() {
        return book_type;
    }

    public void setBook_type(int book_type) {
        this.book_type = book_type;
    }
}
