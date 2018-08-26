package com.syezon.reader.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.syezon.reader.utils.SPHelper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jin on 2016/12/8.
 */
public class GetCharsetService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("GetCharsetService", "onStartCommand");
        if (intent != null) {
            String bookName = intent.getStringExtra("bookName");
            String filepath = intent.getStringExtra("filePath");
            Log.e("GetCharsetService", bookName + filepath);
            InputStream inputStream;
            try {
                inputStream = new FileInputStream(filepath);
                String encoding = getCharset(inputStream);
                Log.e("GetCharsetService", bookName + " " + encoding);
                SPHelper.setBookEnCoding(GetCharsetService.this, bookName, encoding);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {

            }

        }
        return super.onStartCommand(intent, flags, startId);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static String getCharset(InputStream in) {
        BufferedInputStream bin;
        int bom = 0;
        String str = " ";
        String str2 = "";
        try {
            bin = new BufferedInputStream(in);
            bom = (bin.read() << 8) + bin.read();
            // 获取两个字节内容，如果文件无BOM信息，则通过判断字的字节长度区分编码格式
            byte bs[] = new byte[10];

            while (str.matches("\\s+\\w*")) {
                bin.read(bs);

                str = new String(bs, "UTF-8");

            }
//            PushbackInputStream inputStream = new PushbackInputStream(in, 19);
//            inputStream.unread(new byte[19]);
            str2 = new String(bs, "GBK");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String code = null;
        // 有BOM
        switch (bom) {
            case 0xefbb:
                code = "utf-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                // 无BOM
                if (str.length() <= str2.length()) {
                    code = "utf-8";
                } else {
                    code = "GBK";
                }
        }


        return code;
    }
}
