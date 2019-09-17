/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hzjy.download.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * Created by lyy on 2016/1/22. 通用工具
 */
public class CommonUtil {
    private static final String TAG = "CommonUtil:";
    public static final String SERVER_CHARSET = "ISO-8859-1";
    private static long lastClickTime;

    /**
     * 是否是快速点击，500ms内快速点击无效
     *
     * @return true 快速点击
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 将字符串转换为Ftp服务器默认的ISO-8859-1编码
     *
     * @param charSet 原字符串编码s
     * @param str     需要转换的字符串
     * @return 转换后的字符串
     */
    public static String convertFtpChar(String charSet, String str)
            throws UnsupportedEncodingException {
        return new String(str.getBytes(charSet), SERVER_CHARSET);
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return {@code true}删除成功、{@code false}删除失败
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            L.e(TAG + "删除文件失败，路径为空");
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
            if (file.renameTo(to)) {
                return to.delete();
            } else {
                return file.delete();
            }
        }
        return false;
    }

    /**
     * 将对象写入文件
     *
     * @param filePath 文件路径
     * @param data     data数据必须实现{@link Serializable}接口
     */
    public static void writeObjToFile(String filePath, Object data) {
        if (!(data instanceof Serializable)) {
            L.e(TAG + "对象写入文件失败，data数据必须实现Serializable接口");
            return;
        }
        FileOutputStream ops = null;
        try {
            if (!createFile(filePath)) {
                return;
            }
            ops = new FileOutputStream(filePath);
            ObjectOutputStream oops = new ObjectOutputStream(ops);
            oops.writeObject(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ops != null) {
                try {
                    ops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从文件中读取对象
     *
     * @param filePath 文件路径
     * @return 如果读取成功，返回相应的Obj对象，读取失败，返回null
     */
    public static Object readObjFromFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            L.e(TAG+ "文件路径为空");
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            L.e(TAG+ String.format("文件【%s】不存在", filePath));
            return null;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            ObjectInputStream oois = new ObjectInputStream(fis);
            return oois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 检查SD内存空间是否充足
     *
     * @param filePath 文件保存路径
     * @param fileSize 文件大小
     * @return {@code false} 内存空间不足，{@code true}内存空间足够
     */
    public static boolean checkSDMemorySpace(String filePath, long fileSize,Context context) {
        List<String> dirs = FileUtil.getSDPathList(context);
        if (dirs == null || dirs.isEmpty()) {
            return true;
        }
        for (String path : dirs) {
            if (filePath.contains(path)) {
                if (fileSize > 0 && fileSize > getAvailableExternalMemorySize(path)) {
                    return false;
                }
            }
        }
        return true;
    }
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }
    /**
     * sdcard 可用大小
     *
     * @param sdcardPath sdcard 根路径
     * @return 单位为：byte
     */
    public static long getAvailableExternalMemorySize(String sdcardPath) {
        StatFs stat = new StatFs(sdcardPath);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * sdcard 总大小
     *
     * @param sdcardPath sdcard 根路径
     * @return 单位为：byte
     */
    public static long getTotalExternalMemorySize(String sdcardPath) {
        StatFs stat = new StatFs(sdcardPath);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取某包下所有类
     *
     * @param className 过滤的类名
     * @return 类的完整名称
     */
    public static List<String> getPkgClassNames(Context context, String className) {
        List<String> classNameList = new ArrayList<>();
        String pPath = context.getPackageCodePath();
        File dir = new File(pPath).getParentFile();
        String[] paths = dir.list();
        if (paths == null) {
            classNameList.addAll(getPkgClassName(pPath, className));
        } else {
            String dPath = dir.getPath();
            for (String path : dir.list()) {
                String fPath = dPath + "/" + path;
                if (!fPath.endsWith(".apk")) {
                    continue;
                }
                classNameList.addAll(getPkgClassName(fPath, className));
            }
        }
        return classNameList;
    }

    /**
     * 获取指定包名下的所有类
     *
     * @param path        dex路径
     * @param filterClass 需要过滤的类
     */
    public static List<String> getPkgClassName(String path, String filterClass) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(path);
            if (!file.exists()) {
                L.w(TAG+ String.format("路径【%s】下的Dex文件不存在", path));
                return list;
            }

            DexFile df = new DexFile(path);//通过DexFile查找当前的APK中可执行文件
            Enumeration<String> enumeration = df.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            while (enumeration.hasMoreElements()) {
                String _className = enumeration.nextElement();
                if (!_className.contains(filterClass)) {
                    continue;
                }
                if (_className.contains(filterClass)) {
                    list.add(_className);
                }
            }
            df.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 拦截window.location.replace数据
     *
     * @return 重定向url
     */
    public static String getWindowReplaceUrl(String text) {
//        if (TextUtils.isEmpty(text)) {
//            L.e(TAG+ "拦截数据为null");
//            return null;
//        }
//        String reg = Regular.REG_WINLOD_REPLACE;
//        Pattern p = Pattern.compile(reg);
//        Matcher m = p.matcher(text);
//        if (m.find()) {
//            String s = m.group();
//            s = s.substring(9, s.length() - 2);
//            return s;
//        }
        return null;
    }

    /**
     * 获取sdcard app的缓存目录
     *
     * @return "/mnt/sdcard/Android/data/{package_name}/files/"
     */
    public static String getAppPath(Context context) {
        //判断是否存在sd卡
        boolean sdExist = Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
        if (!sdExist) {
            return null;
        } else {
            //获取sd卡路径
            File file = context.getExternalFilesDir(null);
            String dir;
            if (file != null) {
                dir = file.getPath() + "/";
            } else {
                dir = Environment.getExternalStorageDirectory().getPath()
                        + "/Android/data/"
                        + context.getPackageName()
                        + "/files/";
            }
            return dir;
        }
    }

    /**
     * 获取map泛型类型
     *
     * @param map list类型字段
     * @return 泛型类型
     */
    public static Class[] getMapParamType(Field map) {
        Class type = map.getType();
        if (!type.isAssignableFrom(Map.class)) {
            L.d(TAG+ "字段类型不是Map");
            return null;
        }

        Type fc = map.getGenericType();

        if (fc == null) {
            L.d(TAG+ "该字段没有泛型参数");
            return null;
        }

        if (fc instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) fc;
            Type[] types = pt.getActualTypeArguments();
            Class[] clazz = new Class[2];
            clazz[0] = (Class) types[0];
            clazz[1] = (Class) types[1];
            return clazz;
        }
        return null;
    }

    /**
     * 获取list泛型类型
     *
     * @param list list类型字段
     * @return 泛型类型
     */

    public static Class getListParamType(Field list) {
        Class type = list.getType();
        if (!type.isAssignableFrom(List.class)) {
            L.d(TAG+ "字段类型不是List");
            return null;
        }

        Type fc = list.getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型

        if (fc == null) {
            L.d(TAG+ "该字段没有泛型参数");
            return null;
        }

        if (fc instanceof ParameterizedType) { //如果是泛型参数的类型
            ParameterizedType pt = (ParameterizedType) fc;
            return (Class) pt.getActualTypeArguments()[0]; //得到泛型里的class类型对象。
        }
        return null;
    }

    /**
     * 创建文件名，如果url链接有后缀名，则使用url中的后缀名
     *
     * @return url 的 hashKey
     */
    public static String createFileName(String url) {
        int end = url.indexOf("?");
        String tempUrl, fileName = "";
        if (end > 0) {
            tempUrl = url.substring(0, end);
            int tempEnd = tempUrl.lastIndexOf("/");
            if (tempEnd > 0) {
                fileName = tempUrl.substring(tempEnd + 1);
            }
        } else {
            int tempEnd = url.lastIndexOf("/");
            if (tempEnd > 0) {
                fileName = url.substring(tempEnd + 1);
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = CommonUtil.keyToHashKey(url);
        }
        return fileName;
    }

//    /**
//     * 分割获取url，协议，ip/域名，端口，内容
//     *
//     * @param url 输入的url{@code String url = "ftp://z:z@dygod18.com:21211/[电影天堂www.dy2018.com]猩球崛起3：终极之战BD国英双语中英双字.mkv";}
//     */
//    public static FtpUrlEntity getFtpUrlInfo(String url) {
//        Uri uri = Uri.parse(url);
//
//        String userInfo = uri.getUserInfo(), remotePath = uri.getPath();
//        L.d(TAG+
//                String.format("scheme = %s, user = %s, host = %s, port = %s, path = %s", uri.getScheme(),
//                        userInfo, uri.getHost(), uri.getPort(), remotePath));
//
//        FtpUrlEntity entity = new FtpUrlEntity();
//        entity.url = url;
//        entity.hostName = uri.getHost();
//        entity.port = uri.getPort() == -1 ? "21" : String.valueOf(uri.getPort());
//        if (!TextUtils.isEmpty(userInfo)) {
//            String[] temp = userInfo.split(":");
//            if (temp.length == 2) {
//                entity.user = temp[0];
//                entity.password = temp[1];
//            } else {
//                entity.user = userInfo;
//            }
//        }
//        entity.scheme = uri.getScheme();
//        entity.remotePath = TextUtils.isEmpty(remotePath) ? "/" : remotePath;
//        return entity;
//    }

    /**
     * 转换Url
     *
     * @param url 原地址
     * @return 转换后的地址
     */
    public static String convertUrl(String url) {
//        Uri uri = Uri.parse(url);
//        url = uri.toString();
//        if (hasDoubleCharacter(url)) {
//            //预先处理空格，URLEncoder只会把空格转换为+
//            url = url.replaceAll(" ", "%20");
//            //匹配双字节字符(包括汉字在内)
//            String regex = Regular.REG_DOUBLE_CHAR_AND_SPACE;
//            Pattern p = Pattern.compile(regex);
//            Matcher m = p.matcher(url);
//            Set<String> strs = new HashSet<>();
//            while (m.find()) {
//                strs.add(m.group());
//            }
//            try {
//                for (String str : strs) {
//                    url = url.replaceAll(str, URLEncoder.encode(str, "UTF-8"));
//                }
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
        return url;
    }

    /**
     * 判断是否有双字节字符(包括汉字在内) 和空格、制表符、回车
     *
     * @param chineseStr 需要进行判断的字符串
     * @return {@code true}有双字节字符，{@code false} 无双字节字符
     */
    public static boolean hasDoubleCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (char aCharArray : charArray) {
            if (((aCharArray >= 0x0391) && (aCharArray <= 0xFFE5)) || (aCharArray == 0x0d) || (aCharArray
                    == 0x0a) || (aCharArray == 0x20)) {
                return true;
            }
        }
        return false;
    }

    /**
     * base64 解密字符串
     *
     * @param str 被加密的字符串
     * @return 解密后的字符串
     */
    public static String decryptBASE64(String str) {
        return new String(Base64.decode(str.getBytes(), Base64.DEFAULT));
    }

    /**
     * base64 加密字符串
     *
     * @param str 需要加密的字符串
     * @return 加密后的字符串
     */
    public static String encryptBASE64(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    /**
     * 字符串编码转换
     */
    public static String strCharSetConvert(String oldStr, String charSet) {
        try {
            return new String(oldStr.getBytes(), charSet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据下载任务组的url创建key
     *
     * @return urls 为 null 或者 size为0，返回""
     */
    public static String getMd5Code(List<String> urls) {
        if (urls == null || urls.size() < 1) return "";
        String md5 = "";
        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            sb.append(url);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sb.toString().getBytes());
            md5 = new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            L.e(TAG+ e.getMessage());
        }
        return md5;
    }

    /**
     * 获取字符串的md5
     *
     * @return 字符串为空或获取md5失败，则返回""
     */
    public static String getStrMd5(String str) {
        if (TextUtils.isEmpty(str)) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            L.e(TAG+ e.getMessage());
        }
        return "";
    }

    /**
     * 获取CPU核心数
     */
    public static int getCoresNum() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            L.d(TAG+ "CPU Count: " + files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Print exception
            L.d(TAG+ "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }

    /**
     * 通过流创建文件
     */
    public static void createFileFormInputStream(InputStream is, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            is.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验文件MD5码
     */
    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            L.e(TAG+ "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = getFileMD5(updateFile);
        if (calculatedDigest == null) {
            L.e(TAG+ "calculatedDigest null");
            return false;
        }
        return calculatedDigest.equalsIgnoreCase(md5);
    }

    /**
     * 校验文件MD5码
     */
    public static boolean checkMD5(String md5, InputStream is) {
        if (TextUtils.isEmpty(md5) || is == null) {
            L.e(TAG+ "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = getFileMD5(is);
        if (calculatedDigest == null) {
            L.e(TAG+ "calculatedDigest null");
            return false;
        }
        return calculatedDigest.equalsIgnoreCase(md5);
    }

    /**
     * 获取文件MD5码
     */
    public static String getFileMD5(File updateFile) {
        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            L.e(TAG+ e);
            return null;
        }

        return getFileMD5(is);
    }

    /**
     * 获取文件MD5码
     */
    public static String getFileMD5(InputStream is) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            L.e(TAG+ e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                L.e(TAG+ e);
            }
        }
    }

//    /**
//     * 创建任务命令
//     *
//     * @param taskType {@link ICmd#TASK_TYPE_DOWNLOAD}、{@link ICmd#TASK_TYPE_DOWNLOAD_GROUP}、{@link
//     *                 ICmd#TASK_TYPE_UPLOAD}
//     */
//    public static <T extends AbsTaskWrapper> AbsNormalCmd createNormalCmd(T entity, int cmd,
//                                                                          int taskType) {
//        return NormalCmdFactory.getInstance().createCmd(entity, cmd, taskType);
//    }

//    /**
//     * 创建任务组命令
//     *
//     * @param childUrl 子任务url
//     */
//    public static <T extends AbsGroupTaskWrapper> AbsGroupCmd createGroupCmd(String target, T entity,
//                                                                             int cmd, String childUrl) {
//        return GroupCmdFactory.getInstance().createCmd(target, entity, cmd, childUrl);
//    }

    /**
     * 创建隐性的Intent
     */
    public static Intent createIntent(String packageName, String action) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(packageName);
        Uri uri = builder.build();
        Intent intent = new Intent(action);
        intent.setData(uri);
        return intent;
    }

    /**
     * 存储字符串到配置文件
     *
     * @param preName 配置文件名
     * @param key     存储的键值
     * @param value   需要存储的字符串
     * @return 成功标志
     */
    public static Boolean putString(String preName, Context context, String key, String value) {
        SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * 从配置文件读取字符串
     *
     * @param preName 配置文件名
     * @param key     字符串键值
     * @return 键值对应的字符串, 默认返回""
     */
    public static String getString(String preName, Context context, String key) {
        SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
        return pre.getString(key, "");
    }

    /**
     * 获取所有字段，包括父类的字段
     */
    public static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        Class personClazz = clazz.getSuperclass();
        if (personClazz != null) {
            Class rootClazz = personClazz.getSuperclass();
            if (rootClazz != null) {
                Collections.addAll(fields, rootClazz.getDeclaredFields());
            }
            Collections.addAll(fields, personClazz.getDeclaredFields());
        }
        Collections.addAll(fields, clazz.getDeclaredFields());
        return fields;
    }

    /**
     * 获取当前类里面的所在字段
     */
    public static Field[] getFields(Class clazz) {
        Field[] fields;
        fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            Class superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                fields = getFields(superClazz);
            }
        }
        return fields;
    }

    /**
     * 获取类里面的指定对象，如果该类没有则从父类查询
     */
    public static Field getField(Class clazz, String name) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(name);
            } catch (NoSuchFieldException e1) {
                if (clazz.getSuperclass() == null) {
                    return field;
                } else {
                    field = getField(clazz.getSuperclass(), name);
                }
            }
        }
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * 字符串转hashcode
     */
    public static int keyToHashCode(String str) {
        int total = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '-') ch = (char) 28; // does not contain the same last 5 bits as any letter
            if (ch == '\'') ch = (char) 29; // nor this
            total = (total * 33) + (ch & 0x1F);
        }
        return total;
    }

    /**
     * 将key转换为16进制码
     *
     * @param key 缓存的key
     * @return 转换后的key的值, 系统便是通过该key来读写缓存
     */
    public static String keyToHashKey(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    /**
     * 将普通字符串转换为16位进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取对象名
     *
     * @param obj 对象
     * @return 对象名
     */
    public static String getClassName(Object obj) {
        String[] arrays = obj.getClass().getName().split("\\.");
        return arrays[arrays.length - 1];
    }

    /**
     * 获取对象名
     *
     * @param clazz clazz
     * @return 对象名
     */
    public static String getClassName(Class clazz) {
        String[] arrays = clazz.getName().split("\\.");
        return arrays[arrays.length - 1];
    }

    /**
     * 格式化文件大小
     *
     * @param size file.length() 获取文件大小
     */
    public static String formatFileSize(double size) {
        if (size < 0) {
            return "0kb";
        }
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            return size + "b";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "kb";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "mb";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "gb";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "tb";
    }

    /**
     * 创建目录 当目录不存在的时候创建文件，否则返回false
     */
    public static boolean createDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                L.d(TAG+ "创建失败，请检查路径和是否配置文件权限！");
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 创建文件 当文件不存在的时候就创建一个文件。 如果文件存在，先删除原文件，然后重新创建一个新文件
     *
     * @return {@code true} 创建成功、{@code false} 创建失败
     */
    public static boolean createFile(String path) {
        if (TextUtils.isEmpty(path)) {
            L.e(TAG+ "文件路径不能为null");
            return false;
        }
        File file = new File(path);
        if (file.getParentFile() == null || !file.getParentFile().exists()) {
            L.d(TAG+ "目标文件所在路径不存在，准备创建……");
            if (!createDir(file.getParent())) {
                L.d(TAG+ "创建目录文件所在的目录失败！文件路径【" + path + "】");
            }
        }
        // 创建目标文件
        if (file.exists()) {
            final File to = new File(file.getAbsolutePath() + System.currentTimeMillis());
            if (file.renameTo(to)) {
                to.delete();
            } else {
                file.delete();
            }
        }
        try {
            if (file.createNewFile()) {
                //L.d(TAG+ "创建文件成功:" + file.getAbsolutePath());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 通过文件名获取下载配置文件路径
     *
     * @param fileName 文件名
     */
//    public static String getFileConfigPath(boolean isDownload, String fileName) {
//        return AriaManager.APP.getFilesDir().getPath() + (isDownload ? AriaManager.DOWNLOAD_TEMP_DIR
//                : AriaManager.UPLOAD_TEMP_DIR) + fileName + ".properties";
//    }

    /**
     * 读取下载配置文件
     */
    public static Properties loadConfig(File file) {
        Properties properties = new Properties();
        FileInputStream fis = null;
        if (!file.exists()) {
            createFile(file.getPath());
        }
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    /**
     * 保存配置文件
     */
    public static void saveConfig(File file, Properties properties) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, false);
            properties.store(fos, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}