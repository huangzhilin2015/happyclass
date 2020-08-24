package com.happy.happyclass.core.executor.impl;

import com.happy.happyclass.core.constant.HappyClassConstantPool;
import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.util.ByteUtil;
import com.happy.happyclass.core.util.HttpUtil;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * 算法和{@link MethodBodyOverwriteExecutor}一样，重写了密码的获取途径
 * 此执行器会尝试从远端获取密码
 * <p>
 * 此算法业务强相关，需要提供远端服务器和协商字节转换规则，请勿随意使用
 * Author huangzhilin
 * Date 2020/1/10
 */
public class PasswordAMethodBodyOverwriteExecutor extends MethodBodyOverwriteExecutor {
    //标识id
    private static final byte[] h = new byte[]{0x6c, 0x23, 0x61, 0x24, 0x7a, 0x25, 0x61, 0x5e, 0x64, 0x26, 0x61, 0x2e, 0x32, 0x30, 0x30, 0x32, 0x30, 0x40, 0x74, 0x61, 0x40, 0x67};
    //远端服务器地址
    private static final byte[] r = new byte[]{0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x31, 0x32, 0x37, 0x2e, 0x30, 0x2e, 0x30, 0x2e, 0x31, 0x3a, 0x39, 0x38, 0x39, 0x39, 0x2f, 0x63, 0x63, 0x73, 0x2f, 0x72, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74};
    //private static final byte[] r = new byte[]{0x68, 0x74, 0x74, 0x70, 0x73, 0x3a, 0x2f, 0x2f, 0x78, 0x64, 0x2e, 0x65, 0x63, 0x68, 0x61, 0x74, 0x73, 0x6f, 0x66, 0x74, 0x2e, 0x63, 0x6f, 0x6d, 0x2f, 0x63, 0x63, 0x73, 0x2f, 0x72, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74};
    //id字符串
    private static final byte[] p = new byte[]{0x69, 0x64};
    //密码
    private String ps;

    public static void main(String[] args) throws Exception {
        System.out.println(new String(r, "UTF-8"));
    }

    /**
     * 尝试从指定远端获取密码
     * <p>
     * 注意{@link MethodBodyOverwriteExecutor#decrypt(ClassInfo, ExecuteContextInfo)}方法有锁
     * 所以此方法不做同步控制
     *
     * @param contextInfo
     * @return
     */
    @Override
    protected String getDecryptPassword(ExecuteContextInfo contextInfo) throws HappyException {
        if (ps == null) {
            try {
                HashMap<String, String> params = new HashMap<>(1);
                params.put(new String(p, HappyClassConstantPool.DEFAULT_CHARSET), new String(h, HappyClassConstantPool.DEFAULT_CHARSET));
                InputStream is = HttpUtil.postFormData(new String(r, HappyClassConstantPool.DEFAULT_CHARSET), params);
                if (is != null) {
                    byte[] bytes = ByteUtil.readStream(is, true);
                    if (bytes.length > 8) {
                        ps = new String(transfer(bytes), HappyClassConstantPool.DEFAULT_CHARSET);
                    }
                }
            } catch (Exception e) {
                throw new HappyException("invalid");
            }
        }
        if (ps == null) {
            throw new HappyException("invalid");
        }
        return ps;
    }

    /**
     * 交换前后4个字节，和远端协商
     *
     * @param bytes
     * @return
     */
    private byte[] transfer(byte[] bytes) {
        byte[] front = new byte[4];
        byte[] back = new byte[4];
        int backIndex = bytes.length - back.length;
        System.arraycopy(bytes, 0, front, 0, front.length);
        System.arraycopy(bytes, backIndex, back, 0, back.length);
        System.arraycopy(back, 0, bytes, 0, back.length);
        System.arraycopy(front, 0, bytes, backIndex, front.length);
        return bytes;
    }

    @Override
    public boolean isValid(ExecuteContextInfo contextInfo) {
        if (contextInfo.isEncrypt()) {
            //加密时判断密码长度
            if (StringUtils.isNotEmpty(contextInfo.getPassword())
                    && contextInfo.getPassword().getBytes(Charset.forName(HappyClassConstantPool.DEFAULT_CHARSET)).length > 8) {
                return true;
            } else {
                return false;
            }
        } else {
            //解密时不用输入密码，密码从远端获取
            return true;
        }
    }
}
