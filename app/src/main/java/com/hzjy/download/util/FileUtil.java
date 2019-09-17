package com.hzjy.download.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 文件操作工具类
 */
public class FileUtil {
    private static final String TAG = "FileUtil:";
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
    private static final String EXTERNAL_STORAGE_PATH =
            Environment.getExternalStorageDirectory().getPath();

    /**
     * 合并文件
     *
     * @param targetPath 目标文件
     * @param subPaths   碎片文件路径
     * @return {@code true} 合并成功，{@code false}合并失败
     */
    public static boolean mergeFile(String targetPath, List<String> subPaths) {
        File file = new File(targetPath);
        FileOutputStream fos = null;
        FileChannel foc = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(targetPath);
            foc = fos.getChannel();
            List<FileInputStream> streams = new LinkedList<>();
            for (String subPath : subPaths) {
                File f = new File(subPath);
                if (!f.exists()) {
                    L.d(TAG + String.format("合并文件失败，文件【%s】不存在", subPath));
                    for (FileInputStream fis : streams) {
                        fis.close();
                    }
                    streams.clear();

                    return false;
                }
                streams.add(new FileInputStream(subPath));
            }
            Enumeration<FileInputStream> en = Collections.enumeration(streams);
            SequenceInputStream sis = new SequenceInputStream(en);
            ReadableByteChannel fic = Channels.newChannel(sis);
            ByteBuffer bf = ByteBuffer.allocate(8196);
            while (fic.read(bf) != -1) {
                bf.flip();
                foc.write(bf);
                bf.compact();
            }
            fic.close();
            sis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (foc != null) {
                    foc.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 分割文件
     *
     * @param filePath 被分割的文件路径
     * @param num      分割的块数
     */
    public static void splitFile(String filePath, int num) {
        try {
            final File file = new File(filePath);
            long size = file.length();
            FileInputStream fis = new FileInputStream(file);
            FileChannel fic = fis.getChannel();
            long j = 0;
            long block = size / num;
            for (int i = 0; i < num; i++) {
                if (i == num - 1) {
                    block = size - block * (num - 1);
                }
                String subPath = file.getPath() + "." + i + ".part";
                L.d(TAG + String.format("block = %s", block));
                File subFile = new File(subPath);
                if (!subFile.exists()) {
                    subFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(subFile);
                FileChannel sfoc = fos.getChannel();
                ByteBuffer bf = ByteBuffer.allocate(8196);
                int len;
                //fis.skip(block * i);
                while ((len = fic.read(bf)) != -1) {
                    bf.flip();
                    sfoc.write(bf);
                    bf.compact();
                    j += len;
                    if (j >= block * (i + 1)) {
                        break;
                    }
                }
                L.d(TAG +  String.format("len = %s", subFile.length()));
                fos.close();
                sfoc.close();
            }
            fis.close();
            fic.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取SD卡目录列表
     */
    public static List<String> getSDPathList(Context context) {
        List<String> paths;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            paths = getVolumeList(context);
            if (paths == null || paths.isEmpty()) {
                paths = getStorageDirectories();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            paths = getVolumeList(context);
        } else {
            List<String> mounts = readMountsFile();
            List<String> volds = readVoldFile();
            paths = compareMountsWithVold(mounts, volds);
        }
        return paths;
    }

    /**
     * getSDPathList
     */
    private static List<String> getVolumeList(final Context context) {
        List<String> pathList = null;
        try {
            android.os.storage.StorageManager manager =
                    (android.os.storage.StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method method = manager.getClass().getMethod("getVolumePaths");
            String[] paths = (String[]) method.invoke(manager);
            pathList = Arrays.asList(paths);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pathList == null || pathList.isEmpty()) {
            pathList = new ArrayList<>();
            pathList.add(EXTERNAL_STORAGE_PATH);
        }
        LinkedHashMap<Integer, String> paths = new LinkedHashMap<>();
        for (String path : pathList) {
            File root = new File(path);
            if (!root.exists() || !root.isDirectory() || !canWrite(path)) {
                continue;
            }
            //去除mount的相同目录
            int key = (root.getTotalSpace() + "-" + root.getUsableSpace()).hashCode();
            String prevPath = paths.get(key);
            if (!TextUtils.isEmpty(prevPath) && prevPath.length() < path.length()) {
                continue;
            }
            paths.put(key, path);
        }
        List<String> list = new ArrayList<>();
        for (Integer key : paths.keySet()) {
            list.add(paths.get(key));
        }
        return list;
    }

    private static boolean canWrite(String dirPath) {
        File dir = new File(dirPath);
        if (dir.canWrite()) {
            return true;
        }
        boolean canWrite;
        File testWriteFile = null;
        try {
            testWriteFile = new File(dirPath, "tw.txt");
            if (testWriteFile.exists()) {
                testWriteFile.delete();
            }
            testWriteFile.createNewFile();
            FileWriter writer = new FileWriter(testWriteFile);
            writer.write(1);
            writer.close();
            canWrite = true;
        } catch (Exception e) {
            e.printStackTrace();
            canWrite = false;
        } finally {
            try {
                if (testWriteFile != null && testWriteFile.exists()) {
                    testWriteFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return canWrite;
    }

    /**
     * Raturns all available SD-Cards in the system (include emulated) Warning: Hack! Based on Android source code of version 4.3
     * (API 18) Because there is no standard way to get it. TODO: Test on future Android versions 4.2+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static List<String> getStorageDirectories() {
        // Final set of paths
        final List<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;

            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String[] folders = DIR_SEPORATOR.split(path);
            final String lastFolder = folders[folders.length - 1];
            boolean isDigit = false;
            if (!TextUtils.isEmpty(lastFolder) && TextUtils.isDigitsOnly(lastFolder)) {
                isDigit = true;
            }
            rawUserId = isDigit ? lastFolder : "";
            //} else {
            //  rawUserId = "";
            //}
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawExternalStorage);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        // checkout SD-CARDs writable
        for (int i = rv.size() - 1; i >= 0; i--) {
            String path = rv.get(i);
            File root = new File(path);
            if (!root.exists() || !root.isDirectory() || !canWrite(path)) {
                rv.remove(i);
            }
        }
        return rv;
    }

    /**
     * Scan the /proc/mounts file and look for lines like this: /dev/block/vold/179:1 /mnt/sdcard vfat
     * rw,dirsync,nosuid,nodev,noexec ,relatime,uid=1000,gid=1015,fmask=0602,dmask=0602,allow_utime=0020,
     * codepage=cp437,iocharset= iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0 When one is found, split it into its
     * elements and then pull out the path to the that mount point and add it to the arraylist
     */
    private static List<String> readMountsFile() {
        // some mount files don't list the default
        // path first, so we add it here to
        // ensure that it is first in our list
        List<String> mounts = new ArrayList<>();
        mounts.add(EXTERNAL_STORAGE_PATH);
        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/") || line.startsWith("/dev/block//vold/")) {//
                    String[] lineElements = line.split(" ");
                    // String partition = lineElements[0];
                    String element = lineElements[1];
                    // don't add the default mount path
                    // it's already in the list.
                    if (!element.equals(EXTERNAL_STORAGE_PATH)) {
                        mounts.add(element);
                    }
                }
            }
            scanner.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return mounts;
    }

    private static List<String> readVoldFile() {
        // read /etc/vold.conf or /etc/vold.fstab (it depends on version what
        // config file is present)
        List<String> vold = null;
        Scanner scanner = null;
        try {
            try {
                scanner = new Scanner(new File("/system/etc/vold.fstab"));
            } catch (FileNotFoundException e1) {
                // e1.printStackTrace();
                scanner = new Scanner(new File("/system/etc/vold.conf"));
            }
            vold = new ArrayList<String>();
            vold.add(EXTERNAL_STORAGE_PATH);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                line = line.trim();
                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split(" ");
                    if (lineElements.length < 3) {
                        continue;
                    }
                    String element = lineElements[2];
                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }
                    // ignore default path
                    if (!element.equals(EXTERNAL_STORAGE_PATH)) {
                        vold.add(element);
                    }
                } else if (line.startsWith("mount_point")) {
                    String element = line.replaceAll("mount_point", "").trim();
                    if (!element.equals(EXTERNAL_STORAGE_PATH)) {
                        vold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return vold;
    }

    private static List<String> compareMountsWithVold(List<String> mounts, List<String> volds) {
    /*
     * 有时候这两个list中的数据并不相同，我们只需要取两个list的交集部分。
     */
        for (int i = mounts.size() - 1; i >= 0; i--) {
            String mount = mounts.get(i);
            File root = new File(mount);
            // 判断目录是否存在并且可读
            if (!root.exists() || !root.isDirectory() || !root.canWrite()) {
                mounts.remove(i);
                continue;
            }
            if (volds != null && !volds.contains(mount)) {
                mounts.remove(i);
            }
        }
        // 清除无用数据
        if (volds != null) {
            volds.clear();
        }
        return mounts;
    }

    public static long getTotalMemory() {
        String file_path = "/proc/meminfo";// 系统内存信息文件
        String ram_info;
        String[] arrayOfRam;
        long initial_memory = 0L;
        try {
            FileReader fr = new FileReader(file_path);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            // 读取meminfo第一行，系统总内存大小
            ram_info = localBufferedReader.readLine();
            arrayOfRam = ram_info.split("\\s+");// 实现多个空格切割的效果
            initial_memory =
                    Integer.valueOf(arrayOfRam[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return initial_memory;
    }

    public static long getAvailMemory(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }
}
