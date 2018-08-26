package com.syezon.reader.wifiTransfer;

import android.content.Context;

import com.syezon.reader.db.BookCaseDBHelper;
import com.yanzhenjie.andserver.AndServerRequestHandler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * check接口
 * Created by jin on 2016/9/14.
 */
public class CheckHandler implements AndServerRequestHandler {

    private Context context;

    public CheckHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        BookCaseDBHelper helper = new BookCaseDBHelper(this.context);
        int count = helper.queryBookCase().size();
        String phoneInfo = "{\"count\" : \"" + count + "\"}";
        response.setEntity(new StringEntity(phoneInfo, "utf-8"));
    }
}
