package com.happy.happyclass.core.executor.impl;

import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.executor.AbstractExecutor;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.util.*;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * 重写方法体，将密文添加到字节码末尾
 * Author huangzhilin
 * Date 2020/1/6
 */
public class MethodBodyOverwriteExecutor extends AbstractExecutor {
    private static final Object locker = new Object();

    @Override
    public byte[] encrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException, IOException {
        //重写方法体
        //初始化上下文
        if (classInfo.getSourceFile() != null) {
            ClassUtil.initPoolClassPathByClassFile(classInfo.getSourceFile());
        } else if (StringUtils.isNotEmpty(classInfo.getClassFileName())) {
            ClassUtil.initPoolClassPathByClassFile(new File(classInfo.getClassFileName()));
        }
        ByteArrayInputStream inputStream = classInfo.getInputStream();
        inputStream.mark(0);
        byte[] buffer = ClassUtil.overWriteMethodBody(inputStream, null);
        inputStream.reset();
        if (buffer == null) {
            throw new HappyException("");
        }
        //修改魔数
        System.arraycopy(H, 0, buffer, 0, H.length);
        //获取需要加密的数据
        //从常量池开始位置进行加密
        //加密的数据要从原字节码中获取
        byte[] origin = ByteUtil.readStream(inputStream, true);
        byte[] needEncrypt = new byte[origin.length - 8];
        System.arraycopy(origin, 8, needEncrypt, 0, needEncrypt.length);
        //获取密文
        byte[] cipher = AESEncryptUtil.encrypt(needEncrypt, getEncryptPassword(contextInfo).trim().toCharArray());
        //记录密文开始位置，解密时使用
        byte[] endBytes = ByteUtil.intToBytes(buffer.length);
        //最终数据:buffer+cipher+endBytes
        byte[] finalData = new byte[buffer.length + cipher.length + endBytes.length];
        int copyPosition = 0;
        System.arraycopy(buffer, 0, finalData, copyPosition, buffer.length);
        copyPosition += buffer.length;
        System.arraycopy(cipher, 0, finalData, copyPosition, cipher.length);
        copyPosition += cipher.length;
        System.arraycopy(endBytes, 0, finalData, copyPosition, endBytes.length);
        return finalData;
    }

    @Override
    public byte[] decrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException {
        synchronized (locker) {
            //System.arraycopy并不是线程安全的，所以这里全局加锁
            LogUtil.print("handle class:" + classInfo.getClassFileName());
            byte[] buffer = classInfo.getClassFileBuffer();
            //首先还原魔数
            System.arraycopy(HH, 0, buffer, 0, HH.length);
            //从后往前读4个字节
            //密文开始位置
            int end = ByteUtil.readInt(buffer, buffer.length - 4);
            //获取密文
            byte[] encrypted = new byte[buffer.length - end - 4];
            System.arraycopy(buffer, end, encrypted, 0, encrypted.length);
            //解密
            byte[] plain = AESEncryptUtil.decrypt(encrypted, getDecryptPassword(contextInfo).toCharArray());
            byte[] finalData = new byte[8 + plain.length];
            System.arraycopy(buffer, 0, finalData, 0, 8);
            System.arraycopy(plain, 0, finalData, 8, plain.length);
            return finalData;
        }
    }
}
