package com.syezon.reader.http;

/**
 * 接口定义
 * Created by jin on 2016/9/20.
 */
public class APIDefine {
    //请求根路径
    public static final String BASE_URL = "http://novel.qclx.com/doc/";
    //获取token值
    public static final String GET_TOKEN = BASE_URL + "token.htm";
    //热门搜索
    public static final String HOT_SEARCH = BASE_URL + "hotSearch.htm";
    //分类列表
    public static final String GET_CLASS_LIST = BASE_URL + "typeList.htm";
    //某个分类数据
    public static final String GET_ASSIGN_CLASS = BASE_URL + "typeNovels.htm";
    //获取精选列表
    public static final String GET_SIFT_LIST = BASE_URL + "choiceNovels.htm";
    //获取排行列表
    public static final String GET_SORT_LIST = BASE_URL + "orderNovels.htm";
    //获取小说详情
    public static final String GET_NOVEL_DETAILS = BASE_URL + "novelInfo.htm";
    //获取小说目录
    public static final String GET_NOVEL_DIR = BASE_URL + "novelTable.htm";
    //获取小说内容
    public static final String GET_NOVEL_INFO = BASE_URL + "novelContent.htm";
    //获取小说最新章节序号
    public static final String GET_NOVEL_NEW_CHAPTER = BASE_URL + "chaptersNum.htm";
    //搜索小说
    public static final String SEARCH_NOVEL = BASE_URL + "search.htm";
    //广告开关接口
    public static final String AD_SWITCH = "http://res.ipingke.com/adsw/reader_adsw.html";
}
