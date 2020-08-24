package com.happy.happyclass.core.util;


import com.happy.happyclass.core.constant.HappyClassConstantPool;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class ByteUtil {

    /**
     * 把int类型的value转为4个byte字节，高位在前
     *
     * @param value
     */
    public static byte[] intToBytes(int value) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) value;
        bytes[2] = (byte) (value >>> 8);
        bytes[1] = (byte) (value >>> 16);
        bytes[0] = (byte) (value >>> 24);
        return bytes;
    }

    /**
     * 把byte数组转为int类型，高位在前
     *
     * @param bytes
     */
    public static int bytesToInt(byte[] bytes) {
        return ((bytes[3] & 0xFF)) + ((bytes[2] & 0xFF) << 8) + ((bytes[1] & 0xFF) << 16) + ((bytes[0]) << 24);
    }

    /**
     * 读取两个字节数值(相当于无符号short类型)
     *
     * @param classBuffer
     * @param offset
     * @return
     */
    public static int readUnsignedShort(byte[] classBuffer, int offset) {
        return ((classBuffer[offset] & 0xFF) << 8) | (classBuffer[offset + 1] & 0xFF);
    }

    /**
     * 从byte数组的指定位置开始读取一个int，高位在前
     *
     * @param bytes
     * @param offset
     * @return
     */
    public static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    /**
     * 从一个{@link InputStream}中读取byte数组
     *
     * @param inputStream
     * @param close
     * @return
     * @throws IOException
     */
    public static byte[] readStream(final InputStream inputStream, final boolean close)
            throws IOException {
        if (inputStream == null) {
            throw new IOException("Class not found");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            try {
                if (close) {
                    inputStream.close();
                } else {
                    inputStream.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 字符数组转字节数组
     *
     * @param chars
     * @return
     */
    public static byte[] toBytes(char[] chars) {
        char[] tmp = new char[chars.length];
        System.arraycopy(chars, 0, tmp, 0, chars.length);
        CharBuffer charBuffer = CharBuffer.wrap(tmp);
        ByteBuffer byteBuffer = Charset.forName(HappyClassConstantPool.DEFAULT_CHARSET).encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000');
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    /**
     * @param chars
     * @return
     */
    public static char[] merger(char[]... chars) {
        int length = 0;
        for (char[] item : chars) {
            length += item.length;
        }
        char[] result = new char[length];
        int index = 0;
        for (char[] b : chars) {
            System.arraycopy(b, 0, result, index, b.length);
            index += b.length;
        }
        return result;
    }
}
