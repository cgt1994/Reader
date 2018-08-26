package com.syezon.reader.wifiTransfer;

import android.util.Log;

import com.syezon.reader.application.ReaderApplication;
import com.syezon.reader.utils.SPHelper;
import com.yanzhenjie.andserver.AndServerRequestHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 服务连接处理
 * Created by jin on 2016/9/13.
 */
public class ServerConnHandler implements AndServerRequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        File file = new File(SPHelper.getUploadIndex(ReaderApplication.getInstance().getApplicationContext()));
        Log.e("file",SPHelper.getUploadIndex(ReaderApplication.getInstance().getApplicationContext()));
        if(file.exists()) {
            response.setStatusCode(200);// 文件存在，返回成功。
            long contentLength = file.length();
            response.setHeader("ContentLength", Long.toString(contentLength));
            InputStream inputStream = new FileInputStream(file);
//            InputStreamReader inputStreamReader=new InputStreamReader(inputStream,"utf-8");
            HttpEntity httpEntity = new InputStreamEntity(inputStream, contentLength);

            response.setEntity(httpEntity);
        } else {
            // 文件不存在。
            response.setStatusCode(404);
        }
    }
}