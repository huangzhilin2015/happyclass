package com.happy.happyclass.execute.constant;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public final class EncryptConstantPool {
    private EncryptConstantPool() {
        throw new UnsupportedOperationException();
    }

    /*逗号分隔符*/
    public static final String COMMON_SEPERATOR = ",";
    /*加密文件存储的目录后缀*/
    public static final String ENCRYPTED_SUFFIX = "-encrypted";
    public static final String ENCRYPTED_FILE_SUFFIX = ".encrypted";
    /*命令行参数*/
    public static final String HELP = "h";
    public static final String SIGN = "s";
    public static final String PASSWORD = "p";
    public static final String TARGET = "t";
    public static final String COVER = "c";
    public static final String OUTPUT = "o";
    public static final String JAR = "j";
    public static final String EXCLUDE = "e";
    public static final String DEBUG = "d";

    public final class SupportedFileSuffix {
        private SupportedFileSuffix() {
            throw new UnsupportedOperationException();
        }

        public static final String JAR = "jar";
        public static final String WAR = "war";
        public static final String ZIP = "zip";
        public static final String RAR = "rar";
        public static final String CLASS = "class";
    }

}
