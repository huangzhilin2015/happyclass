package com.happy.happyclass.execute.decrypt;

import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.executor.Executor;
import com.happy.happyclass.core.executor.ExecutorManager;
import com.happy.happyclass.core.util.LogUtil;
import com.happy.happyclass.execute.constant.EncryptConstantPool;
import org.apache.commons.lang.StringUtils;

import java.lang.instrument.Instrumentation;

/**
 * 负责解密，即agent
 * Author huangzhilin
 * Date 2020/1/3
 */
public class Main {
    /**
     * @param agentArgs
     * @param instrumentation
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        LogUtil.print("agentArgs：" + agentArgs);
        String[] args = agentArgs.split("&");
        String sign = null;
        String password = null;
        for (String arg : args) {
            String[] params = arg.split("=");
            if (EncryptConstantPool.SIGN.equals(params[0])) {
                sign = params[1];
            } else if (EncryptConstantPool.PASSWORD.equals(params[0])) {
                password = params[1];
            }
        }
        if (StringUtils.isEmpty(sign)) {
            throw new HappyException("invalid params.");
        }
        Executor executor = ExecutorManager.findExecutor(sign);
        ExecuteContextInfo contextInfo = new ExecuteContextInfo(password, false);
        if (!executor.isValid(contextInfo)) {
            throw new HappyException("在当前参数下，该算法不可用");
        }
        instrumentation.addTransformer(new HappyClassFileDecryptor(executor, contextInfo));
    }
}

