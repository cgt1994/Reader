package com.syezon.reader.wifiTransfer;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.syezon.reader.activity.WiFiTransferActivity;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.model.BookCaseBean;
import com.syezon.reader.service.GetCharsetService;
import com.syezon.reader.utils.MD5Util;
import com.syezon.reader.utils.SPHelper;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.andserver.AndServerRequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.httpserv.HttpServFileUpload;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * 用户上传文件处理
 * Created by jin on 2016/9/13.
 */
public class UploadHandler implements AndServerRequestHandler {
    private Context context;

    public UploadHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (HttpServFileUpload.isMultipartContent(request)) {//判断是否是带文件上传的表单
            // 这里可以先拿到参数，传文件的时候需要传的话。
            Map<String, String> params = HttpRequestParser.parse(request);//拿到参数

            // 文件保存目录
            final File uploadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/com.syezon.reader/book/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            if (uploadDir.isDirectory()) {
                try {
                    // 保存文件。
                    processFileUpload(request, uploadDir);
                    response(200, "Ok.", response);
                } catch (Exception e) {
                    e.printStackTrace();
                    response(500, "Save the file when the error occurs.", response);
                }
            } else {
                response(400, "The server can not save the file.", response);
            }
        } else {// 如果不是上传文件，告诉客户端禁止访问。
            response(403, "You must upload file, contentType is multipart/form-data.", response);
        }
    }

    /**
     * 解析文件并保存到SD卡。
     *
     * @param request   {@link HttpRequest}.
     * @param uploadDir 文件保存文件夹。
     * @throws Exception 保存文件时可能发生。
     */
    private void processFileUpload(HttpRequest request, File uploadDir) throws Exception {
        FileItemFactory factory = new DiskFileItemFactory(1024 * 1024, uploadDir);
        HttpServFileUpload fileUpload = new HttpServFileUpload(factory);

        // 设置上传进度监听，可以在这个类中，发广播或者handler更新UI。
        fileUpload.setProgressListener(new AndWebProgressListener());
        FileItemIterator itemIterator = fileUpload.getItemIterator(request);
        int i = 0;
        while (itemIterator.hasNext()) {
            FileItemStream item = itemIterator.next();
            if (item.getName().contains(".txt")) {
                String bookName = item.getName().substring(0, item.getName().indexOf("."))+"_wifi";
                MobclickAgent.onEvent(context, "wifi_upload_book", bookName);
                //解析书籍
                InputStream is = item.openStream();
                BufferedInputStream bis = new BufferedInputStream(is);
//                String encoding = getCharset(is);

//                is.close();
////                PushbackInputStream
//                Log.e("encodingnew", " 1");
//                InputStream is2 = item.openStream();
//                Log.e("encodingnew", (is2 == null) + " ");
//                String encoding2 = getCharset(is2);
//                Log.e("encodingnew", encoding + " " + encoding2);
//                InputStreamReader inReader = new InputStreamReader(is, encoding);
//
//
//                BufferedReader reader = new BufferedReader(inReader);
                // BookFactory bookFactory = new BookFactory(context, bookName);
                File parent = new File(uploadDir + "/" + MD5Util.encrypt(bookName));
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                File uploadedFile = new File(parent, MD5Util.encrypt(bookName));
                if (!uploadedFile.exists()) {
                    uploadedFile.createNewFile();
                }


//                is.reset();

                SPHelper.setBookFilePath(context, bookName, uploadedFile.getPath());
//                SPHelper.setBookEnCoding(context, bookName, encoding);


//                Log.e("encodeing set", bookName + "  " + encoding);


                OutputStream os = new FileOutputStream(uploadedFile);
                OutputStreamWriter osWriter = new OutputStreamWriter(os);
//                if (encoding.equals("GBK")) {
//                    osWriter = new OutputStreamWriter(os);
//                } else {
//                    osWriter = new OutputStreamWriter(os, encoding);
//                }
                BufferedWriter writer = new BufferedWriter(osWriter);

                //剔除空行写入文件
                byte buf[] = new byte[1024];
                String lineTxt;
                int len = 0;
//                while ((lineTxt = bis.read()) != null) {
//                    if (!lineTxt.trim().equals("")) {
//                        writer.write(lineTxt + "\n");
//                    }
//                }
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                    os.flush();
                }
                bis.close();
                writer.close();
//                if (is != null) {
//                    try {
//                        is.close();
//                    } catch (IOException e) {
//                    }
//                }
//               开启获取字符集服务
                Intent intent = new Intent(context, GetCharsetService.class);
                intent.putExtra("filePath", uploadedFile.getPath());
                intent.putExtra("bookName", bookName);
//                intent.putExtra("encoding", encoding);
//                intent.putExtra("chapterName", "引言");
//                intent.putExtra("needOpen", false);
                context.startService(intent);

                //向书架中添加书籍
                BookCaseDBHelper helper = new BookCaseDBHelper(context);
                BookCaseBean bean = new BookCaseBean();
                bean.setBook_update_time("");
                bean.setLast_chapter("");
                bean.setBook_author("未知");
                bean.setBook_name(bookName);
                bean.setBook_img("-1");
                bean.setAdd_time(System.currentTimeMillis() + "");
                bean.setCache(-1);
                bean.setBook_id(-1);
                bean.setBook_type(2);
                Log.e("addto", "4");
                helper.addToBookCase(bean);
            }
        }
    }

    private static String getCharset(InputStream in) {
        BufferedInputStream bin;
        int bom = 0;
        String str = " ";
        String str2 = "";
        try {
            bin = new BufferedInputStream(in);
            Log.e("mark2", bin.markSupported() + " ");

            bom = (bin.read() << 8) + bin.read();
            // 获取两个字节内容，如果文件无BOM信息，则通过判断字的字节长度区分编码格式
            byte bs[] = new byte[10];
            int i = 0;
            while (str.matches("\\s+\\w*")) {
                bin.read(bs);
                i++;
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
                code = "US-ASCII";
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

    private String getFileEnCoding(byte[] b) {
        if (b[0] == -17 && b[1] == -69 && b[2] == -65) {
            return "utf-8";
        } else {
            return "GBK";
        }
    }

    public static boolean IsUTF8Bytes(byte[] data) {

        int i = 0;
        int size = data.length;

        while (i < size) {
            int step = 0;
            if ((data[i] & 0x80) == 0x00) {
                step = 1;
            } else if ((data[i] & 0xe0) == 0xc0) {
                if (i + 1 >= size) return false;
                if ((data[i + 1] & 0xc0) != 0x80) return false;

                step = 2;
            } else if ((data[i] & 0xf0) == 0xe0) {
                if (i + 2 >= size) return false;
                if ((data[i + 1] & 0xc0) != 0x80) return false;
                if ((data[i + 2] & 0xc0) != 0x80) return false;

                step = 3;
            } else {
                return false;
            }

            i += step;
        }

        if (i == size) return true;

        return false;
    }


    /**
     * 发送响应消息。
     *
     * @param responseCode 响应码。
     * @param message      响应消息。
     * @param response     响应。
     * @throws Exception 发送数据的时候可能异常。
     */
    private void response(int responseCode, String message, HttpResponse response) throws IOException {
        response.setStatusCode(responseCode);
        response.setEntity(new StringEntity(message, "utf-8"));
    }

    private class AndWebProgressListener implements ProgressListener {
        /**
         * 更新进度.
         *
         * @param pBytesRead     读到现在为止的字节总数。
         * @param pContentLength 内容总大小，如果大小是未知的，那么这个数字是-1。
         * @param pItems         字段的下标，哪一个正在被读。如果是0，没有字段被读取，如果是1，代表第一个正在被读。
         */
        @Override
        public void update(long pBytesRead, long pContentLength, int pItems) {
            if (pContentLength != -1) {
                int progress = (int) (pBytesRead * 100 / pContentLength);
                //发广播更新进度条
                Intent intent = new Intent();
                intent.setAction(WiFiTransferActivity.UPLOAD_PROGRESS);
                intent.putExtra("progress", progress);
                context.sendBroadcast(intent);
            }
        }
    }
}
