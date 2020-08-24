package com.happy.happyclass.core.util;

import com.happy.happyclass.core.constant.HappyClassConstantPool;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class AESEncryptUtil {
    public static final char[] SALT = {'a', 'b', 'e', 'q', 's', 'u', 'm', '&', 'h', 'o', 'p', 'w', '3', 'y', '#', '$', 'z', 'n', '@', 'm'};
    private static int KEY_LENGTH = 1024;

    /**
     * 加密
     *
     * @param msg 内容
     * @param key 密钥
     * @return 密文
     */
    public static byte[] encrypt(byte[] msg, char[] key) {
        return enAES(msg, md5(ByteUtil.merger(key, SALT), true));
    }

    /**
     * 解密
     *
     * @param msg 密文
     * @param key 密钥
     * @return 明文
     */
    public static byte[] decrypt(byte[] msg, char[] key) {
        return deAES(msg, md5(ByteUtil.merger(key, SALT), true));
    }

    /**
     * md5加密
     *
     * @param str 字符串
     * @return md5字串
     */
    public static byte[] md5byte(char[] str) {
        byte[] b = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = ByteUtil.toBytes(str);
            md.update(buffer);
            b = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * md5
     *
     * @param str   字串
     * @param sh0rt 是否16位
     * @return 32位/16位md5
     */
    public static char[] md5(char[] str, boolean sh0rt) {
        byte s[] = md5byte(str);
        if (s == null) {
            return null;
        }
        int begin = 0;
        int end = s.length;
        if (sh0rt) {
            begin = 8;
            end = 16;
        }
        char[] result = new char[0];
        for (int i = begin; i < end; i++) {
            result = ByteUtil.merger(result, Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6).toCharArray());
        }
        return result;
    }


    /**
     * 加密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 加密后的字节
     */
    public static byte[] enSimple(byte[] msg, int start, int end, char[] key) {
        byte[] keys = merger(md5byte(ByteUtil.merger(key, SALT)), md5byte(ByteUtil.merger(SALT, key)));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 解密
     *
     * @param msg   加密报文
     * @param start 开始位置
     * @param end   结束位置
     * @param key   密钥
     * @return 解密后的字节
     */
    public static byte[] deSimple(byte[] msg, int start, int end, char[] key) {
        byte[] keys = merger(md5byte(ByteUtil.merger(key, SALT)), md5byte(ByteUtil.merger(SALT, key)));
        for (int i = start; i <= end; i++) {
            msg[i] = (byte) (msg[i] ^ keys[i % keys.length]);
        }
        return msg;
    }

    /**
     * 加密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 加密后的字节
     */
    public static byte[] enSimple(byte[] msg, char[] key) {
        return enSimple(msg, 0, msg.length - 1, key);
    }

    /**
     * 解密
     *
     * @param msg 加密报文
     * @param key 密钥
     * @return 解密后的字节
     */
    public static byte[] deSimple(byte[] msg, char[] key) {
        return deSimple(msg, 0, msg.length - 1, key);
    }

    /**
     * 调用加密解密
     *
     * @param cipher Cipher
     * @param msg    要加密的字节
     * @param mode   解密/解密
     * @return 结果
     * @throws Exception Exception
     */
    private static byte[] cipherDoFinal(Cipher cipher, byte[] msg, int mode) throws Exception {
        int in_length = 0;
        if (mode == Cipher.ENCRYPT_MODE) {
            in_length = KEY_LENGTH / 8 - 11;
        } else if (mode == Cipher.DECRYPT_MODE) {
            in_length = KEY_LENGTH / 8;
        }

        byte[] in = new byte[in_length];
        byte[] out = new byte[0];

        for (int i = 0; i < msg.length; i++) {
            if (msg.length - i < in_length && i % in_length == 0) {
                in = new byte[msg.length - i];
            }
            in[i % in_length] = msg[i];
            if (i == (msg.length - 1) || (i % in_length + 1 == in_length)) {
                out = merger(out, cipher.doFinal(in));
            }
        }
        return out;
    }

    /**
     * 合并byte[]
     *
     * @param bts 字节数组
     * @return 合并后的字节
     */
    public static byte[] merger(byte[]... bts) {
        int lenght = 0;
        for (byte[] b : bts) {
            lenght += b.length;
        }

        byte[] bt = new byte[lenght];
        int lastLength = 0;
        for (byte[] b : bts) {
            System.arraycopy(b, 0, bt, lastLength, b.length);
            lastLength += b.length;
        }
        return bt;
    }

    /**
     * AES加密字节
     *
     * @param msg 字节数组
     * @param key 密钥
     * @return 加密后的字节
     */
    public static byte[] enAES(byte[] msg, char[] key) {
        byte[] encrypted = null;
        try {
            byte[] raw = ByteUtil.toBytes(key);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    /**
     * AES解密
     *
     * @param str 密文字串
     * @param key 密钥
     * @return 明文字串
     */
    public static String deAES(String str, char[] key) {
        String originalString = null;
        byte[] msg = Base64.getDecoder().decode(str);
        byte[] original = deAES(msg, key);
        try {
            originalString = new String(original, HappyClassConstantPool.DEFAULT_CHARSET);
        } catch (Exception e) {

        }
        return originalString;
    }

    /**
     * AES解密
     *
     * @param msg 要解密的字节
     * @param key 密钥
     * @return 明文字节
     */
    public static byte[] deAES(byte[] msg, char[] key) {
        byte[] decrypted = null;
        try {
            byte[] raw = ByteUtil.toBytes(key);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }
}
