package com.happy.happyclass.core.util;

import com.happy.happyclass.core.interceptor.Interceptor;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class FileUtil {

    /**
     * 组合路径
     *
     * @param first
     * @param second
     * @return
     */
    public static String composeFilePath(String first, String second) {
        if (StringUtils.isEmpty(second)) {
            return first;
        }
        if (StringUtils.isEmpty(first)) {
            return second;
        }
        first = first.endsWith(File.separator) ? first : first + File.separator;
        second = second.startsWith(File.separator) ? second.substring(1) : second;
        return first + second;
    }

    /**
     * 创建一个文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            //确保目录创建
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    /**
     * 判断两个目录是否相同
     *
     * @param var1
     * @param var2
     * @return
     */
    public static boolean isSameDirectory(String var1, String var2) {
        if (StringUtils.isEmpty(var1) || StringUtils.isEmpty(var2)) {
            return false;
        }
        var1 = var1.endsWith(File.separator) ? var1.substring(0, var1.length() - 1) : var1;
        var2 = var2.endsWith(File.separator) ? var2.substring(0, var2.length() - 1) : var2;
        return var1.equals(var2);
    }

    /**
     * @param source
     * @param target
     * @throws IOException
     */
    public static void copyFile(File source, String target) throws IOException {
        File targetFile = createFile(target);
        try (FileChannel inputChannel = new FileInputStream(source).getChannel();
             FileChannel outputChannel = new FileOutputStream(targetFile).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    /**
     * @param file
     * @param parentPath
     * @throws IOException
     */
    public static void unpack(File file, String parentPath) throws IOException {
        unpack(file, parentPath, null);
    }

    /**
     * 解压jar包到指定的目录
     * 忽略空文件夹
     *
     * @param file
     * @param parentPath
     * @param interceptor
     * @throws IOException
     */
    public static void unpack(File file, String parentPath, Interceptor<byte[], byte[]> interceptor) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String filePath = FileUtil.composeFilePath(parentPath, entry.getName());
                        File newFile = FileUtil.createFile(filePath);
                        InputStream fio = zipFile.getInputStream(entry);
                        byte[] buffer = ByteUtil.readStream(fio, true);
                        if (interceptor != null) {
                            buffer = interceptor.doIntercept(filePath, buffer);
                        }
                        try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                            fileOutputStream.write(buffer, 0, buffer.length);
                            fileOutputStream.flush();
                        }
                    }
                }
            }
        }
    }

    /**
     * @param input  原文件(夹)，比如 d:\test
     * @param output 目标文件，比如 e:\test.jar
     */
    public static void pack(String input, String output) {
        pack(new File(input), new File(output));
    }

    /**
     * 压缩文件
     *
     * @param input
     * @param output
     */
    public static void pack(File input, File output) {
        if (output.exists()) {
            output.delete();
        }
        try (FileOutputStream outputStream = new FileOutputStream(output);
             ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            if (zos != null) {
                pack(input, zos, null);
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }
    }

    /**
     * @param input
     * @param zipOutputStream
     * @param path            文件在压缩包内的路径，为空表示根路径
     */
    public static void pack(File input, ZipOutputStream zipOutputStream, String path) throws IOException {
        if (input.isDirectory()) {
            //压缩文件夹
            if (path != null) {
                ZipEntry ze = new ZipEntry(path + "/");
                ze.setTime(System.currentTimeMillis());
                zipOutputStream.putNextEntry(ze);
                zipOutputStream.closeEntry();
            }
            String relativePath;
            for (File file : input.listFiles()) {
                if (StringUtils.isEmpty(path)) {
                    relativePath = file.getName();
                } else {
                    relativePath = path + "/" + file.getName();
                }
                pack(file, zipOutputStream, relativePath);
            }
        } else {
            //压缩一个文件
            InputStream in = new FileInputStream(input);
            ZipEntry entry = new ZipEntry(path);
            entry.setTime(System.currentTimeMillis());
            zipOutputStream.putNextEntry(entry);
            byte[] buffer = ByteUtil.readStream(in, true);
            zipOutputStream.write(buffer);
            zipOutputStream.closeEntry();
        }
    }

    /**
     * 递归删除文件(夹)
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            for (File item : files) {
                deleteFile(item);
            }
        }
        file.delete();
    }

    /**
     * 递归查找文件
     *
     * @param fileList
     * @param dir
     * @param endWith
     */
    public static void listFile(List<File> fileList, File dir, String endWith) {
        if (!dir.exists()) {
            throw new IllegalArgumentException("目录[" + dir.getAbsolutePath() + "]不存在");
        }
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                listFile(fileList, f, endWith);
            } else if (f.isFile() && f.getName().endsWith(endWith)) {
                fileList.add(f);
            }
        }
    }
}
