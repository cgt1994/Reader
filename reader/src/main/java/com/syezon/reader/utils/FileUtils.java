package com.syezon.reader.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.syezon.reader.constant.Constant;
import com.syezon.reader.db.BookCaseDBHelper;
import com.syezon.reader.model.BookCaseBean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

/**
 * 文件读读写工具类
 * Created by fang on 16-9-8.
 */
public class FileUtils {

    //坚检查sd卡是否可用
    public static boolean checkSDCard() {
        return Environment.isExternalStorageEmulated();
    }

    //将asset文件夹下文件复制到sd卡中
    public static void copyAssetsFile(Context context, String assetDir, String SDDir) {
        try {
            String[] files = context.getAssets().list(assetDir);
            File dirFile = new File(SDDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            for (int i = 0; i < files.length; i++) {
                if (!files[i].contains(".")) {
                    if (0 == assetDir.length()) {
                        copyAssetsFile(context, files[i], SDDir + "/" + files[i] + "/");
                    } else {
                        copyAssetsFile(context, assetDir + "/" + files[i], SDDir + "/" + files[i] + "/");
                    }
                    continue;
                }
                File outFile = new File(dirFile, files[i]);
                if (outFile.exists())
                    outFile.delete();
                InputStream in = null;
                if (0 != assetDir.length())
                    in = context.getAssets().open(assetDir + "/" + files[i]);
                else
                    in = context.getAssets().open(files[i]);
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                if (files[i].contains("index.html")) {
                    String path = dirFile + "/" + files[i];
                    SPHelper.setUploadIndex(context, path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent getFileIntent(File file) {
//       Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
        Uri uri = Uri.fromFile(file);
        String type = getMIMEType(file);
        Log.i("tag", "type=" + type);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, type);
        return intent;
    }

    public static String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
      /* 取得扩展名 */
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();

      /* 依扩展名的类型决定MimeType */
        if (end.equals("pdf")) {
            type = "application/pdf";//
        } else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") ||
                end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio/*";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video/*";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") ||
                end.equals("jpeg") || end.equals("bmp")) {
            type = "image/*";
        } else if (end.equals("apk")) {
        /* android.permission.INSTALL_PACKAGES */
            type = "application/vnd.android.package-archive";
        }
//      else if(end.equals("pptx")||end.equals("ppt")){
//        type = "application/vnd.ms-powerpoint";
//      }else if(end.equals("docx")||end.equals("doc")){
//        type = "application/vnd.ms-word";
//      }else if(end.equals("xlsx")||end.equals("xls")){
//        type = "application/vnd.ms-excel";
//      }
        else {
//        /*如果无法直接打开，就跳出软件列表给用户选择 */
            type = "*/*";
        }
        return type;
    }


    //复制内置小说
    public static void copyBook2SDCard(Context context, String assetDir) {
        try {
            String[] files = context.getAssets().list(assetDir);
            if (files.length < 2) {//没有内置小说的时候，也标记为已经拷贝
                SPHelper.setFileIsCopy(context, true);
                return;
            }
            String bookName = "";
            for (int i = 0; i < files.length; i++) {
                Log.e("filesize", files.length + " ");
                if (files[i].contains("bookinfo")) {
                    //写小说介绍内容
                    bookName = writeBookInfo(context, assetDir + "/bookinfo");
                } else {
                    //复制小说
                    copyBook(context, assetDir + "/" + files[i], bookName);
                }
            }
            SPHelper.setFileIsCopy(context, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存小说介绍信息
    private static String writeBookInfo(Context context, String assetFilePath) throws Exception {
        InputStream is = context.getAssets().open(assetFilePath);//读取小说介绍信息
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isReader);
        String bookInfo = reader.readLine();//bookinfo中的内容为一行，提高速度

        String[] infos = bookInfo.split(",");
        BookCaseDBHelper helper = new BookCaseDBHelper(context);
        BookCaseBean bean = new BookCaseBean();

        for (int i = 0; i < infos.length; i++) {
            String tag = infos[i];
            String info = infos[i].substring(infos[i].indexOf("=") + 1);
            if (tag.contains("bookId")) {
                bean.setBook_id(Integer.valueOf(info));
            } else if (tag.contains("bookName")) {
                bean.setBook_name(info);
            } else if (tag.contains("bookAuthor")) {
                bean.setBook_author(info);
            } else if (tag.contains("bookImg")) {
                bean.setBook_img(info);
            } else if (tag.contains("bookLastChapter")) {
                bean.setLast_chapter(info);
            } else if (tag.contains("encoding")) {
                //判断文件编码格式
                SPHelper.setBookEnCoding(context, bean.getBook_name(), info);
            } else if (tag.contains("bn")) {
                Constant.NATIVE_BN = info;
            }
        }
        bean.setAdd_time(System.currentTimeMillis() + "");
        bean.setBook_type(0);
        Log.e("addto", "1");
        helper.addToBookCase(bean);
        reader.close();
        return bean.getBook_name();
    }

    //复制内置小说进sd卡中,该小说是经过压缩后的压缩文件
    private static void copyBook(Context context, String assetFilePath, String bookName) throws Exception {
        if (!checkSDCard()) {
            return;//sd卡不可用
        }
        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/com.syezon.reader/book/" + MD5Util.encrypt(bookName);
        File dirFile = new File(dirName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        InputStream is = context.getAssets().open(assetFilePath);//获取输入流
        ZipInputStream zipInput = new ZipInputStream(is);

        while (zipInput.getNextEntry() != null) {
            File file = new File(dirName + "/" + MD5Util.encrypt(bookName));
            if (!file.exists()) {
                file.createNewFile();
            }

            SPHelper.setBookFilePath(context, bookName, file.getAbsolutePath());

            InputStreamReader isReader = new InputStreamReader(zipInput, SPHelper.getBookEnCoding(context, bookName));
            BufferedReader reader = new BufferedReader(isReader);

            OutputStream os = new FileOutputStream(file);
            OutputStreamWriter osWriter = new OutputStreamWriter(os,
                    SPHelper.getBookEnCoding(context, bookName));
            BufferedWriter writer = new BufferedWriter(osWriter);

            String lineTxt;
            while ((lineTxt = reader.readLine()) != null) {
                writer.write(lineTxt + "\n");
            }
            writer.close();
        }
        zipInput.close();
        is.close();
    }

    //写文件并返回文件位置
    public static String writeFile(String bookName, String content) {
        String path = null;
        if (!checkSDCard()) {
            return "-1";//sd卡不可用
        }

        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/com.syezon.reader/book/" + MD5Util.encrypt(bookName);
        String fileName = MD5Util.encrypt(content);
        File dirFile = new File(dirName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dirName + "/" + fileName);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osWriter = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osWriter);
            writer.write(content);
            writer.flush();
            path = file.getPath().toString();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    //读取章节内容
    public static String readFile(String filePath, Long start, Long end, int cutLength, String encoding) {
        StringBuffer content = new StringBuffer();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "-1";
            }
            String lineTxt;
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            accessFile.seek(start);
            while ((lineTxt = accessFile.readLine()) != null) {
                lineTxt = new String(lineTxt.getBytes("8859_1"), encoding);
                if (!lineTxt.trim().equals("")) {
                    if (!judgeChapterName(lineTxt)) {
                        content.append(lineTxt).append("\n");
                    }

                    if (accessFile.getFilePointer() >= end) {
                        break;
                    }
                }
            }
            accessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.substring(0, content.length() - cutLength - 1);
    }

    //    这个会随应用卸载而卸载
    public static String getDiskCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    //读取章节内容
    public static String readFile(Context context, String bookName, String filePath, long start, String encoding) {
        int viewLines = SPHelper.getBookLines(context);
        int viewNums = SPHelper.getCurTxtNums(context);
        int curLines = 0;
        int cutLength = 0;
        StringBuffer cutStr = new StringBuffer();
        start = start < 0 ? 0 : start;
        long endSeek = 0l;
        StringBuffer content = new StringBuffer();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "-1";
            }
            String lineTxt;
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            accessFile.seek(start);
            while ((lineTxt = accessFile.readLine()) != null) {
                lineTxt = new String(lineTxt.getBytes("8859_1"), encoding);
                if (!lineTxt.trim().equals("")) {

                    //不是章标题，且当前页还能显示下，当是章标题的时候停止读取
                    if ((!judgeChapterName(lineTxt)) && curLines < viewLines) {
                        if (lineTxt.length() < viewNums) {
                            curLines++;
                        } else {
                            int needLines = (int) Math.ceil((lineTxt.length()) / Double.valueOf(viewNums));//计算当前内容的行数
                            if ((curLines + needLines) > viewLines) {
                                cutStr.append(lineTxt.substring((viewLines - curLines) * viewNums)).append("\n");//被剪掉的字符
                                cutLength = lineTxt.length() - ((viewLines - curLines) * viewNums);
                                curLines = viewLines;
                            } else {
                                curLines += needLines;
                            }
                            //这个时候行数已经大于或等于最大行数了，所以这个时候的指针位置才是当前页的指针位置
                            endSeek = accessFile.getFilePointer();
                            //Log.e("TAG",curLines+":"+needLines+"\n"+lineTxt);
                        }
                    } else {
                        if (judgeChapterName(lineTxt)) {
                            if (endSeek < 200) {//开始的第一行就是章标题了
                                endSeek = start + (lineTxt + "\n").getBytes(encoding).length;
                            } else {
                                endSeek = endSeek + (lineTxt + "\n").getBytes(encoding).length;
                            }
                        } else {
                            endSeek = endSeek - cutStr.toString().getBytes(encoding).length;
                        }
                        //往后一个字符
                        if (encoding.equals("utf-8")) {
                            endSeek += 3;
                        } else {
                            endSeek += 2;
                        }
                        //Log.e("TAG","endSeek:"+endSeek+":"+cutStr.toString().getBytes(encoding).length+":cutStr:"+cutStr);
                        //做索引
//                        SPHelper.setBookIndex(context, bookName, page, 0, start, endSeek, cutLength);
                        SPHelper.setCurrentSeek(context, bookName, endSeek);
                        break;
                    }
                    content.append(lineTxt).append("\n");
                }
            }
            accessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cutLength == 0) {
            return content.toString();
        } else {
            return content.substring(0, content.length() - cutLength);
        }
    }

    // 判断是否是章标题
    private static boolean judgeChapterName(String str) {
        String regex = ".*(第.*章).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str.trim());
        if (str.length() < 50 && matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    //读取章节内容
    public static String readFile(String filePath, int start, int nums, int lines, String encoding) {
        start = start < 0 ? 0 : start;
        int curLines = 0;
        int length = nums * lines;
        StringBuffer content = new StringBuffer();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "-1";
            }
            InputStream in = new FileInputStream(file);
            InputStreamReader inReader = new InputStreamReader(in, encoding);
            BufferedReader reader = new BufferedReader(inReader);
            reader.skip(start);//只读取一部分
            String lineTxt;
            while (curLines < lines && content.length() < length && (lineTxt = reader.readLine()) != null) {
                if (!lineTxt.trim().equals("")) {
                    content.append(lineTxt).append("\n");
                    if (lineTxt.length() < nums) {
                        curLines++;
                    } else {
                        curLines += (int) Math.ceil((lineTxt.length()) / Double.valueOf(nums));//计算当前内容的行数,加上开头的两个空格
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.e("TAG", "content:" + content.toString());
        if (content.length() < length) {
            return content.toString();
        } else {
            return content.toString().substring(0, length);
        }
    }

    //读取章节内容,通过行数,网络书籍的时候使用
    public static String readFile(String filePath, int startLine, int endLine, int startChar, int endChar, String encoding) {
        //Log.e("TAG","start to read file");

        StringBuffer content = new StringBuffer();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "-1";
            }
            InputStream in = new FileInputStream(file);
            InputStreamReader inReader = new InputStreamReader(in, encoding);
            BufferedReader reader = new BufferedReader(inReader);
            String lineTxt;
            int curLines = 0;

            while ((lineTxt = reader.readLine()) != null) {
                Log.e("readnet", startLine + "  " + endLine + " " + startChar + " " + endChar + "我来自网络  " + lineTxt);
//                mLines.add("  "+lineTxt.trim());
                lineTxt = ToDBC(lineTxt);
                lineTxt = "  " + lineTxt.trim();
                curLines++;
                //在这个范围内
                if (curLines >= startLine && curLines <= endLine) {
                    if (curLines == startLine) {
                        if (startLine == endLine) {
                            lineTxt = half2Full(lineTxt.substring(startChar, endChar));
                        } else {
                            lineTxt = half2Full(lineTxt.substring(startChar));
                        }
//                        lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");
                        Log.e("readnet", "startLine" + lineTxt);
                        content.append(lineTxt).append("\n");
                    } else if (curLines == endLine) {
                        lineTxt = half2Full(lineTxt.substring(0, endChar));
//                        lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");
                        Log.e("readnet", "endLine" + lineTxt);
                        content.append(lineTxt).append("\n");
                    } else {
                        lineTxt = half2Full(lineTxt);
//                        lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");

                        Log.e("readnet", "else" + lineTxt);
                        content.append(lineTxt).append("\n");
                    }
                }
            }
            reader.close();
            Log.e("readnet", "read file end");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.e("TAG", "content:" + content.toString());
        return content.toString();
    }

    public static boolean deleteFile1(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("delete", "删除单个文件" + fileName + "成功！");
                return true;
            } else {
                Log.e("delete", "删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            Log.e("delete", "删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Log.e("delete", "删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                Log.e("delefile", files[i].getName());
                flag = deleteFile1(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Log.e("delete", "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.e("delete", "删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    private static String nowFile = "";//当前文件
    private static BufferedReader reader;
    private static List<String> mLines;

    public FileUtils(String filePath, String encoding) {
        try {
            if (!nowFile.equals(filePath)) {
                File file = new File(filePath);
                if (!file.exists()) {
                    return;
                }
                InputStream in = new FileInputStream(file);
                Log.e("file encoding", encoding);
                InputStreamReader inReader = new InputStreamReader(in, encoding);

                reader = new BufferedReader(inReader);
                nowFile = filePath;
                mLines = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String ToDBC(String input) {


        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }
        String returnString = new String(c);

        return returnString;
    }

    //读取章节内容,通过行数
    public String readFile(int startLine, int endLine, int startChar, int endChar) {
        StringBuffer content = new StringBuffer();
        try {
            String lineTxt;
            if (mLines.size() < 50) {//将内容读取到内存中加快速度
                while ((lineTxt = reader.readLine()) != null) {
//                   lineTxt= lineTxt.trim();
//                    if (lineTxt.indexOf("“")!=-1){
//                        Log.e("tag",lineTxt.indexOf("“")+"“");
//                        lineTxt=lineTxt.replaceAll("“","“ ");
//                    }
//                    if (lineTxt.indexOf("”")!=-1){
//                        lineTxt=lineTxt.replaceAll("”","” ");
//                    }
                    lineTxt = ToDBC(lineTxt);
                    Log.e("readFile", "lineTxt: " + lineTxt);
                    mLines.add("  " + lineTxt.trim());
                }
                reader.close();
            }
            // Log.e("TAG", "startLine:" + startLine + "," + endLine + "," + startChar + "," + endChar+":"+mLines.size());
            startLine = startLine > 0 ? startLine - 1 : startLine;
            for (int i = startLine; i < endLine; i++) {
                lineTxt = mLines.get(i);
                Log.e("readFile", "startline" + (i + 1) + "  " + lineTxt);
                if (i == startLine) {
//                    处理直接一个readline直接超出一页
                    if (startLine == (endLine - 1)) {
                        Log.e("readFile", "在同一行" + lineTxt.length() + " " + startChar + "  " + endChar);
//                        跨页的下一页直接占了两页
//                        while ((endChar - startChar) > lineTxt.length()) {
//                            lineTxt = mLines.get(i - 1);
//                            Log.e("finally", lineTxt);
                        if (endChar > lineTxt.length()) {

                            lineTxt = half2Full(lineTxt.substring(startChar));
//                            lineTxt = "dfddfsd";

                        } else {
                            lineTxt = half2Full(lineTxt.substring(startChar, endChar));
                        }
                        Log.e("readFile", "截取后" + lineTxt);
                    } else {
                        lineTxt = half2Full(lineTxt.substring(startChar));
                    }
//                    lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");

                    Log.e("read111", "startLine" + startLine + "  " + lineTxt);
                    content.append(lineTxt).append("\n");

                } else if (i == (endLine - 1)) {
                    lineTxt = half2Full(lineTxt.substring(0, endChar));
//                    lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");
                    Log.e("read111", "end" + startLine + "  " + lineTxt);
                    content.append(lineTxt).append("\n");
                } else {
                    lineTxt = half2Full(lineTxt);
//                    Log.e("read111", "else" + startLine + "  " + lineTxt);
//                    lineTxt = lineTxt.replaceAll("“", " “ ").replaceAll("”", " ” ");
                    content.append(lineTxt).append("\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
//    public static String StringFilter(String str) throws PatternSyntaxException {
//        str=str.replaceAll("【","[").replaceAll("】","]").replaceAll("！","!").replace("，",",");//替换中文标号
//        String regEx="[『』]"; // 清除掉特殊字符
//        Pattern p = Pattern.compile(regEx);
//        Matcher m = p.matcher(str);
//        return m.replaceAll("");
//    }


    public static String half2Full(String value) {
        if (isEmpty(value)) {
            return "";
        }
        char[] cha = value.toCharArray();

        /**
         * full blank space is 12288, half blank space is 32
         * others :full is 65281-65374,and half is 33-126.
         */
        for (int i = 0; i < cha.length; i++) {
            if (cha[i] == 32) {
                cha[i] = (char) 12288;
            } else if (cha[i] < 127) {
                cha[i] = (char) (cha[i] + 65248);
            }
        }
        return new String(cha);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    //删除书本
    public static void deleteFile(String bookName) {
        if (!checkSDCard()) {
            return;//sd卡不可用
        }
        String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/com.syezon.reader/book/" + MD5Util.encrypt(bookName.substring(bookName.indexOf("_") + 1));
        File file = new File(dirName);
        if (file.exists()) {
            deleteDir(file);
        }
    }

    //递归删除书本
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();//递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        final File to = new File(dir.getAbsolutePath() + System.currentTimeMillis());
        dir.renameTo(to);
        return to.delete();
        // 目录此时为空，可以删除
//        return dir.delete();
    }
}
