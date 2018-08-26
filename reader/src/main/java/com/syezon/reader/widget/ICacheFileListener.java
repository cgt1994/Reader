package com.syezon.reader.widget;

/**
 * Created by jin on 2016/10/10.
 */
public interface ICacheFileListener {
    //opt：0表示前一页,1表示下一页
    void onCacheFile(int chapter,int opt);
}
