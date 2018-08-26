package com.syezon.reader.model;

import java.io.Serializable;

/**
 * Created by jin on 2016/9/26.
 */
public class NovelBean implements Serializable {
    private int id;//小说id
    private String name;//小说名称
    private String img;//小说封皮
    private String author;//作者
    private String chapter;//最新章节
    private String intro;//简单的介绍

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }
}
