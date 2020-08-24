package com.happy.happyclass.core.context;

/**
 * Author huangzhilin
 * Date 2020/1/9
 */
public class HappyRunContext {
    private static ThreadLocal<RunContextInfo> runContextInfoThreadLocal = new ThreadLocal<>();

    public static void initRunContextInfo(RunContextInfo contextInfo) {
        runContextInfoThreadLocal.set(contextInfo);
    }

    public static RunContextInfo getRunContextInfo() {
        return runContextInfoThreadLocal.get();
    }

    public static void clearRunContextInfo() {
        runContextInfoThreadLocal.remove();
    }
}
