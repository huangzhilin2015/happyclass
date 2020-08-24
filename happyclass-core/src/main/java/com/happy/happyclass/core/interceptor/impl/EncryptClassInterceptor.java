package com.happy.happyclass.core.interceptor.impl;

import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.interceptor.Interceptor;
import com.happy.happyclass.core.executor.Executor;
import com.happy.happyclass.core.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Set;

/**
 * 用于在解压jar包的过程中加密字节码
 * Author huangzhilin
 * Date 2020/1/8
 */
public class EncryptClassInterceptor implements Interceptor<byte[], byte[]> {
    private Executor executor;
    private ExecuteContextInfo contextInfo;
    private File sourceFile;
    private ClassInfo cacheClassInfo;
    private int encrypted = 0;
    private Set<String> excludes;

    public EncryptClassInterceptor(Executor executor, File sourceFile, ExecuteContextInfo contextInfo, Set<String> excludes) {
        this.executor = executor;
        this.sourceFile = sourceFile;
        this.contextInfo = contextInfo;
        this.excludes = excludes;
    }

    /**
     * @param param 代表原jar包对象，{@link Executor} 中可能需要依赖该对象初始化相关依赖
     * @param bytes
     * @return
     */
    @Override
    public byte[] doIntercept(Object param, byte[] bytes) {
        try {
            if (param != null && param.toString().endsWith(".class")) {
                LogUtil.debug("加密jar包下的文件：" + param);
                String className = param.toString().substring(param.toString().lastIndexOf("/") + 1);
                if (excludes.contains(className)) {
                    LogUtil.debug("[jar]跳过加密:" + param);
                    return bytes;
                }
                //创建一个classInfo
                if (cacheClassInfo == null) {
                    cacheClassInfo = new ClassInfo().setSourceFile(sourceFile);
                }
                cacheClassInfo.setClassFileName(param.toString());
                cacheClassInfo.setInputStream(new ByteArrayInputStream(bytes));
                //获取加密后的数据
                encrypted++;
                return executor.encrypt(cacheClassInfo, contextInfo);
            } else {
                LogUtil.debug("[jar]跳过加密:" + param);
                return bytes;
            }
        } catch (Exception e) {
            LogUtil.print("[jar]加密失败，跳过:" + param);
            encrypted--;
            return bytes;
        }
    }

    public int getEncrypted() {
        return encrypted;
    }
}
