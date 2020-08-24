package com.happy.happyclass.core.constant;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public final class HappyClassConstantPool {
    private HappyClassConstantPool() {
        throw new UnsupportedOperationException();
    }

    public static final String DEFAULT_CHARSET = "UTF-8";

    public final class ExecutorSignDefine {
        public static final String DEFAULT = "1";
        public static final String METHOD_BODY_OVERWRITE = "2";
        public static final String PASSWORD_A_METHOD_BODY_OVERWRITE = "3";
    }
}
