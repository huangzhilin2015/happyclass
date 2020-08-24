package com.happy.happyclass.core.info;

import com.happy.happyclass.core.util.ByteUtil;
import lombok.Data;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
@Data
@ToString
public class ClassInfo {
    private File sourceFile;
    private String classFileName;
    private byte[] classFileBuffer;
    private boolean inJar;
    private ByteArrayInputStream inputStream;

    public ClassInfo() {
    }

    public ClassInfo(String classFileName, ByteArrayInputStream inputStream) {
        this.classFileName = classFileName;
        this.inputStream = inputStream;
    }

    public ClassInfo(String classFileName, byte[] classFileBuffer) {
        this.classFileName = classFileName;
        this.classFileBuffer = classFileBuffer;
    }

    public ClassInfo setInJar(boolean inJar) {
        this.inJar = inJar;
        return this;
    }

    public ClassInfo setSourceFile(File file) {
        this.sourceFile = file;
        return this;
    }

    public byte[] getClassFileBuffer() {
        if (classFileBuffer != null) {
            return classFileBuffer;
        } else if (inputStream != null) {
            inputStream.mark(0);
            try {
                return ByteUtil.readStream(inputStream, false);
            } catch (IOException e) {
                return null;
            } finally {
                inputStream.reset();
            }
        } else {
            return null;
        }
    }

    public void destroy() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
