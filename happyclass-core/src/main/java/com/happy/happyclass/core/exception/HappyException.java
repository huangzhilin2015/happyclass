package com.happy.happyclass.core.exception;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class HappyException extends RuntimeException {
    public HappyException(String msg) {
        super(msg);
    }

    public HappyException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public HappyException(Throwable throwable) {
        super(throwable);
    }
}
