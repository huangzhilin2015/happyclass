package com.happy.happyclass.core.util;

import com.happy.happyclass.core.context.HappyRunContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class LogUtil {
    private static final SimpleDateFormat dateFormator = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 控制台打印
     *
     * @param msg
     */
    public static void print(String msg) {
        System.out.println(new StringBuilder(dateFormator.format(new Date())).append("[happyclass]").append(msg));
    }

    /**
     * @param msg
     */
    public static void debug(String msg) {
        if (HappyRunContext.getRunContextInfo() != null && HappyRunContext.getRunContextInfo().isDebugEnable()) {
            System.out.println(new StringBuilder(dateFormator.format(new Date())).append("[happyclass][DEBUG]").append(msg));
        }
    }

    /**
     * @param msg
     */
    public static void error(String msg) {
        System.out.println(new StringBuilder(dateFormator.format(new Date())).append("[happyclass][ERROR]").append(msg));
    }
}
