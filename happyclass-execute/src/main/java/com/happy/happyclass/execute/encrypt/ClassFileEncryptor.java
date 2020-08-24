package com.happy.happyclass.execute.encrypt;

import com.happy.happyclass.core.exception.HappyException;

import java.io.IOException;

/**
 * Author huangzhilin
 * Date 2020/1/12
 */
public interface ClassFileEncryptor {
    /**
     *
     */
    void doEncrypt() throws IOException, HappyException;
}
