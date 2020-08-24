package com.happy.happyclass.core.interceptor;

/**
 * @Author huangzhilin
 * @Date 2020/1/5
 */
public interface Interceptor<T,S> {
    S doIntercept(Object param,T t);
}
