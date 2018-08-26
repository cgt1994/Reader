package com.syezon.reader.model;

import java.io.Serializable;

/**
 * Created by jin on 2016/9/26.
 */
public class NovelClassBean implements Serializable {

    private int id;//分类id
    private String img;//分类的图片
    private String name;//分类名称
    private int count;//该分类下的小说数量

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
