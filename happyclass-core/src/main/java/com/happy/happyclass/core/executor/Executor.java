package com.happy.happyclass.core.executor;

import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;

import java.io.IOException;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public interface Executor {
    /**
     * 负责对{@link ClassInfo}进行加密，返回密文字节数组
     *
     * @param classInfo
     * @param contextInfo
     * @return
     */
    byte[] encrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException, IOException;

    /**
     * 对负责对{@link ClassInfo}进行解密，返回明文字节数组
     *
     * @param classInfo
     * @param contextInfo
     * @return
     */
    byte[] decrypt(ClassInfo classInfo, ExecuteContextInfo contextInfo) throws HappyException;

    /**
     * 此{@link Executor}是否支持该字节码
     *
     * @param classInfo
     * @return
     */
    boolean isAccept(ClassInfo classInfo);

    /**
     * 对于给定{@link ExecuteContextInfo}，是否有效
     *
     * @param contextInfo
     * @return
     */
    boolean isValid(ExecuteContextInfo contextInfo);
}
