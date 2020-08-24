package com.happy.happyclass.core.executor.impl;

import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.executor.AbstractExecutor;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.util.AESEncryptUtil;
import com.happy.happyclass.core.util.ByteUtil;
import com.happy.happyclass.core.util.ClassUtil;
import com.happy.happyclass.core.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * 虚构长度为0的字段表和方法表
 * 注意：此算法可能会导致依赖字段、方法的一些注解使用异常，需要配合修改代码
 * Author huangzhilin
 * Date 2020/1/3
 */
public class DefaultHappyClassExecutor extends AbstractExecutor {
    private static final Object locker = new Object();

    @Override
    public byte[] encrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException {
        if (classInfo == null
                || classInfo.getInputStream() == null
                || StringUtils.isEmpty(classInfo.getClassFileName())
                || StringUtils.isEmpty(getEncryptPassword(contextInfo))) {
            throw new HappyException("unSupported classInfo or contextInfo,classInfo:" + classInfo.toString() + ",contextInfo:" + contextInfo.toString());
        }
        byte[] buffer;
        try {
            buffer = ByteUtil.readStream(classInfo.getInputStream(), true);
        } catch (IOException e) {
            throw new HappyException(e);
        }
        //首先跳过常量池
        int flagStartPosition = ClassUtil.seekToFlagStartPosition(buffer);
        //创建一个字节码文件
        //1.魔数+版本
        //魔数
        byte[] sub1 = new byte[8];
        System.arraycopy(H, 0, sub1, 0, 4);
        //拷贝版本号
        System.arraycopy(buffer, 4, sub1, 4, 4);
        //2.拷贝常量池
        byte[] sub2 = new byte[flagStartPosition - 8];
        System.arraycopy(buffer, 8, sub2, 0, sub2.length);
        //3.拷贝一个访问标记、类索引、父类索引、接口索引
        int interfaceNum = ByteUtil.readUnsignedShort(buffer, flagStartPosition + 6);
        //每个接口两个字节
        int interfaceWeight = interfaceNum * 2;
        byte[] sub3 = new byte[8 + interfaceWeight];
        System.arraycopy(buffer, flagStartPosition, sub3, 0, sub3.length);
        //4.模拟字段表、方法表
        byte[] sub4 = new byte[]{0x00, 0x00, 0x00, 0x00};
        //5.属性表及其之后的数据
        int firstAttributeOffset = ClassUtil.seekToAttributePosition(flagStartPosition, buffer);
        byte[] sub5 = new byte[buffer.length - firstAttributeOffset];
        System.arraycopy(buffer, firstAttributeOffset, sub5, 0, sub5.length);
        //6.整个加密的数据为：接口索引到属性表之间的数据
        int encryptStart = flagStartPosition + 8 + interfaceWeight;
        int encryptEnd = firstAttributeOffset;
        byte[] needEncrypt = new byte[encryptEnd - encryptStart];
        System.arraycopy(buffer, encryptStart, needEncrypt, 0, needEncrypt.length);
        //获取密文
        byte[] cipher = AESEncryptUtil.encrypt(needEncrypt, getEncryptPassword(contextInfo).trim().toCharArray());
        //记录原文起始位置，解密时需要把原文从这个位置开始还原，即接口索引后的位置
        byte[] start1 = ByteUtil.intToBytes(encryptStart);
        //sub1:魔数+版本号
        // +  sub2:常量池
        // +  sub3:访问标记+类索引+父类索引+接口索引
        // +  sub4:空的字段表+方法表
        // +  sub5:属性表及其之后的数据
        // +  cipher:密文
        // +  两个int(8字节)的位移记录数据
        byte[] finalData = new byte[sub1.length + sub2.length + sub3.length + sub4.length + sub5.length + cipher.length + 8];
        int position = 0;
        System.arraycopy(sub1, 0, finalData, position, sub1.length);
        position += sub1.length;
        System.arraycopy(sub2, 0, finalData, position, sub2.length);
        position += sub2.length;
        System.arraycopy(sub3, 0, finalData, position, sub3.length);
        position += sub3.length;
        System.arraycopy(sub4, 0, finalData, position, sub4.length);
        position += sub4.length;
        System.arraycopy(sub5, 0, finalData, position, sub5.length);
        position += sub5.length;
        //记录密文的开始位置，从这个位置开始到最后除去8个字节，都是密文
        byte[] start2 = ByteUtil.intToBytes(position);
        System.arraycopy(cipher, 0, finalData, position, cipher.length);
        position += cipher.length;
        System.arraycopy(start1, 0, finalData, position, start1.length);
        position += start1.length;
        System.arraycopy(start2, 0, finalData, position, start2.length);
        return finalData;
    }

    @Override
    public byte[] decrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException {
        synchronized (locker) {
            LogUtil.print("handle class:" + classInfo.getClassFileName());
            byte[] cipher = classInfo.getClassFileBuffer();
            //首先还原魔数
            System.arraycopy(HH, 0, cipher, 0, 4);
            //从后往前读两个int
            int start1 = ByteUtil.readInt(cipher, cipher.length - 8);
            int start2 = ByteUtil.readInt(cipher, cipher.length - 4);
            //首先解密密文
            byte[] encrypted = new byte[cipher.length - 8 - start2];
            System.arraycopy(cipher, start2, encrypted, 0, encrypted.length);
            byte[] plain = AESEncryptUtil.decrypt(encrypted, getDecryptPassword(contextInfo).toCharArray());
            byte[] finalData = new byte[plain.length + start2 - 4];
            System.arraycopy(cipher, 0, finalData, 0, start1);
            System.arraycopy(plain, 0, finalData, start1, plain.length);
            System.arraycopy(cipher, start1 + 4, finalData, start1 + plain.length, start2 - start1 - 4);
            return finalData;
        }
    }
}
