package com.syezon.reader.wifiTransfer;

import android.content.Context;
import android.content.Intent;

import com.syezon.reader.activity.WiFiTransferActivity;
import com.syezon.reader.utils.Tools;
import com.yanzhenjie.andserver.AndServerRequestHandler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * getName接口
 * Created by jin on 2016/9/14.
 */
public class UserInfoHandler implements AndServerRequestHandler {

    private Context context;

    public UserInfoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String phoneInfo = "{\"serverName\" : \"" + Tools.getPhoneInfo() + "\"}";
        response.setEntity(new StringEntity(phoneInfo, "utf-8"));

        //发送用户连接的广播
        Intent intent = new Intent();
        intent.setAction(WiFiTransferActivity.USER_CONNECTED);
        this.context.sendBroadcast(intent);
    }
}
