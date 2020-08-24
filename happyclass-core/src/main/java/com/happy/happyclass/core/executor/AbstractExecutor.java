package com.happy.happyclass.core.executor;

import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * Author huangzhilin
 * Date 2020/1/6
 */
public abstract class AbstractExecutor implements Executor {
    protected static final byte[] H = new byte[]{(byte) 0xCA, (byte) 0xEF, (byte) 0xBE, (byte) 0xBA};
    protected static final byte[] HH = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};

    @Override
    public boolean isAccept(ClassInfo classInfo) {
        if (classInfo == null || classInfo.getClassFileBuffer() == null || classInfo.getClassFileBuffer().length < 4) {
            return false;
        }
        byte[] h = new byte[4];
        h[0] = classInfo.getClassFileBuffer()[0];
        h[1] = classInfo.getClassFileBuffer()[1];
        h[2] = classInfo.getClassFileBuffer()[2];
        h[3] = classInfo.getClassFileBuffer()[3];
        return Arrays.equals(h, H);
    }

    @Override
    public boolean isValid(ExecuteContextInfo contextInfo) {
        return contextInfo != null && StringUtils.isNotEmpty(contextInfo.getPassword());
    }

    /**
     * 获取解密密码
     *
     * @param contextInfo
     * @return
     */
    protected String getDecryptPassword(ExecuteContextInfo contextInfo) {
        return contextInfo == null ? null : contextInfo.getPassword();
    }

    /**
     * 获取加密密码
     *
     * @param contextInfo
     * @return
     */
    protected String getEncryptPassword(ExecuteContextInfo contextInfo) {
        return contextInfo == null ? null : contextInfo.getPassword();
    }
}
