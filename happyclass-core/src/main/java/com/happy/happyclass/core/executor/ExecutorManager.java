package com.happy.happyclass.core.executor;

import com.happy.happyclass.core.constant.HappyClassConstantPool;
import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.executor.impl.DefaultHappyClassExecutor;
import com.happy.happyclass.core.executor.impl.MethodBodyOverwriteExecutor;
import com.happy.happyclass.core.executor.impl.PasswordAMethodBodyOverwriteExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个简单的{@link Executor}管理器，每新增一个{@link Executor}都要
 * 在{@link HappyClassConstantPool.ExecutorSignDefine}中定义，并且在静态代码块中注册
 * Author huangzhilin
 * Date 2020/1/3
 */
public class ExecutorManager {
    private static final Map<String, Executor> executorMap = new HashMap<>();

    static {
        //在这里注册executor
        executorMap.put(HappyClassConstantPool.ExecutorSignDefine.DEFAULT, new DefaultHappyClassExecutor());
        executorMap.put(HappyClassConstantPool.ExecutorSignDefine.METHOD_BODY_OVERWRITE, new MethodBodyOverwriteExecutor());
        executorMap.put(HappyClassConstantPool.ExecutorSignDefine.PASSWORD_A_METHOD_BODY_OVERWRITE, new PasswordAMethodBodyOverwriteExecutor());
    }

    /**
     * 从{@link ExecutorManager#executorMap}中寻找{@link Executor}
     *
     * @param sign
     * @return
     * @throws HappyException
     */
    public static Executor findExecutor(String sign) throws HappyException {
        Executor executor = executorMap.get(sign);
        if (executor == null) {
            throw new HappyException("executor not found for sign:" + sign);
        }
        return executor;
    }
}
