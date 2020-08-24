package com.happy.happyclass.execute.decrypt;

import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.executor.Executor;
import lombok.Data;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
@Data
public abstract class ClassFileDecryptor implements ClassFileTransformer {
    protected Executor executor;
    protected ExecuteContextInfo contextInfo;

    public ClassFileDecryptor(Executor executor) {
        this(executor, null);
    }

    public ClassFileDecryptor(Executor executor, ExecuteContextInfo contextInfo) {
        if (executor == null) {
            throw new HappyException("executor cannot be null.");
        }
        this.executor = executor;
        this.contextInfo = contextInfo;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //System.out.println("transform:" + className + ",classLoader:" + loader.toString() + ",threadId:" + Thread.currentThread().getId());
        ClassInfo classInfo = new ClassInfo(className, classfileBuffer);
        if (executor.isAccept(classInfo)) {
            return executor.decrypt(classInfo, contextInfo);
        } else {
            return classfileBuffer;
        }
    }
}
