package com.happy.happyclass.core.util;

import com.happy.happyclass.core.constant.Symbol;
import javassist.*;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.compiler.Javac;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
public class ClassUtil {
    private static final String STANDARD_WEB_INF = "WEB-INF";
    private static final String CLASSES = "classes";
    private static final String LIB = "lib";
    private static final Set<String> addedPaths = new HashSet<>();

    /**
     * @param pool
     * @param context
     */
    private static void initClassPoolClassPathWithClasses(ClassPool pool, File[] context) {
        for (File item : context) {
            if (item.exists() && item.isDirectory()) {
                insertClassPath(pool, item);
            }
        }
    }

    /**
     * 初始化上下文依赖
     *
     * @param pool
     * @param context
     */
    private static void initClassPoolClassPathWithJar(ClassPool pool, File[] context) {
        for (File item : context) {
            if (item.exists()) {
                if (item.isDirectory()) {
                    //寻找目录下的所有jar包
                    List<File> jars = new ArrayList<>();
                    FileUtil.listFile(jars, item, ".jar");
                    for (File jar : jars) {
                        insertClassPath(pool, jar);
                    }
                } else if (item.getName().endsWith(".jar")) {
                    insertClassPath(pool, item);
                }
            }
        }
    }

    /**
     * @param pool
     * @param file
     * @throws NotFoundException
     */
    private static void insertClassPath(ClassPool pool, File file) {
        String path = file.getAbsolutePath();
        try {
            if (addedPaths.contains(path)) {
                return;
            } else {
                LogUtil.debug("insertClassPath:" + path);
                pool.insertClassPath(path);
                addedPaths.add(path);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据一个class文件初始化{@link ClassPool}依赖环境
     *
     * @param classFile
     */
    public static void initPoolClassPathByClassFile(File classFile) {
        ClassPool pool = ClassPool.getDefault();
        String filePath = classFile.getAbsolutePath();
        //是否为标准web目录
        int index = filePath.indexOf(STANDARD_WEB_INF);
        if (index >= 0) {
            //添加/classes
            String base = filePath.substring(0, index + STANDARD_WEB_INF.length());
            String directory = FileUtil.composeFilePath(base, CLASSES);
            initClassPoolClassPathWithClasses(pool, new File[]{new File(directory)});
            //添加/lib
            directory = FileUtil.composeFilePath(base, LIB);
            initClassPoolClassPathWithJar(pool, new File[]{new File(directory)});
        } else if (classFile.getName().endsWith(".jar")) {
            //非标准WEB目录，那么将自己加入classpath
            insertClassPath(pool, classFile);
        }
    }

    /**
     * 重写方法体
     *
     * @param classFile
     * @param body
     */
    public static byte[] overWriteMethodBody(InputStream classFile, String body) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.makeClass(classFile, false);
            Javac javac = new Javac(cc);
            for (CtMethod method : cc.getDeclaredMethods()) {
                if (!method.getName().contains("<") && method.getLongName().startsWith(cc.getName())) {
                    CodeAttribute codeAttribute = method.getMethodInfo().getCodeAttribute();
                    if (codeAttribute != null && codeAttribute.getCodeLength() != 1 && codeAttribute.getCode()[0] != -79) {
                        CodeIterator iterator = codeAttribute.iterator();
                        int numOfLocalVars = javac.recordParams(method.getParameterTypes(), Modifier.isStatic(method.getModifiers()));
                        javac.recordParamNames(codeAttribute, numOfLocalVars);
                        javac.recordLocalVariables(codeAttribute, 0);
                        javac.recordReturnType(Descriptor.getReturnType(method.getMethodInfo().getDescriptor(), cc.getClassPool()), false);
                        Bytecode b = javac.compileBody(method, body);
                        int maxStack = b.getMaxStack();
                        int maxLocals = b.getMaxLocals();
                        if (maxStack > codeAttribute.getMaxStack()) {
                            codeAttribute.setMaxStack(maxStack);
                        }
                        if (maxLocals > codeAttribute.getMaxLocals()) {
                            codeAttribute.setMaxLocals(maxLocals);
                        }
                        int pos = iterator.insertEx(b.get());
                        iterator.insert(b.getExceptionTable(), pos);
                        method.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
                        if ("void".equalsIgnoreCase(method.getReturnType().getName()) && method.getLongName().endsWith(".main(java.lang.String[])") && method.getMethodInfo().getAccessFlags() == 9) {
                            method.insertBefore("System.out.println(\"\\n not support.\\n\");");
                        }

                    }
                }
            }
            return cc.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从字节码中定位到访问标记的开始位置，即跳过魔数、版本号、常量池
     *
     * @param classBuffer
     * @return
     */
    public static int seekToFlagStartPosition(byte[] classBuffer) {
        //获取常量池大小
        int constantPoolCount = ByteUtil.readUnsignedShort(classBuffer, 8);
        //魔数+版本号+常量池大小，所以常量池索引从4+4+2=10开始
        int offset = 10;
        //常量池索引从1开始，0保留
        int index = 1;
        int constantSize = 0;
        //常量表大概是这种结构：type+data+type+data+....
        while (index < constantPoolCount) {
            int size;
            //每个类型占用的字节数要加上自身标识位
            switch (classBuffer[offset]) {
                case Symbol.CONSTANT_FIELDREF_TAG:
                case Symbol.CONSTANT_METHODREF_TAG:
                case Symbol.CONSTANT_INTERFACE_METHODREF_TAG:
                case Symbol.CONSTANT_INTEGER_TAG:
                case Symbol.CONSTANT_FLOAT_TAG:
                case Symbol.CONSTANT_NAME_AND_TYPE_TAG:
                case Symbol.CONSTANT_DYNAMIC_TAG:
                case Symbol.CONSTANT_INVOKE_DYNAMIC_TAG:
                    size = 5;
                    break;
                case Symbol.CONSTANT_LONG_TAG:
                case Symbol.CONSTANT_DOUBLE_TAG:
                    //long和double占两个位置
                    size = 9;
                    index++;
                    break;
                case Symbol.CONSTANT_UTF8_TAG:
                    //后两个字节存储了UTF-8编码字符串的长度1+2+readSize
                    size = 3 + ByteUtil.readUnsignedShort(classBuffer, offset + 1);
                    break;
                case Symbol.CONSTANT_METHOD_HANDLE_TAG:
                    size = 4;
                    break;
                case Symbol.CONSTANT_CLASS_TAG:
                case Symbol.CONSTANT_STRING_TAG:
                case Symbol.CONSTANT_METHOD_TYPE_TAG:
                case Symbol.CONSTANT_PACKAGE_TAG:
                case Symbol.CONSTANT_MODULE_TAG:
                    size = 3;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            constantSize += size;
            //跳到下一个常量
            offset += size;
            index++;
        }
        return 10 + constantSize;
    }

    /**
     * 定位到字段表开始位置
     *
     * @param flagStartPosition
     * @param buffer
     * @return
     */
    public static int seekToFieldPosition(int flagStartPosition, byte[] buffer) {
        return flagStartPosition + 8 + ByteUtil.readUnsignedShort(buffer, flagStartPosition + 6) * 2;
    }

    /**
     * 定位到方法表开始位置
     *
     * @param flagStartPosition
     * @param buffer
     * @return
     */
    public static int seekToMethodPosition(int flagStartPosition, byte[] buffer) {
        //字段表位置
        int currentOffset = seekToFieldPosition(flagStartPosition, buffer);
        //字段表长度标识
        int fieldsCount = ByteUtil.readUnsignedShort(buffer, currentOffset);
        currentOffset += 2;
        //跳过字段表
        while (fieldsCount-- > 0) {
            int attributesCount = ByteUtil.readUnsignedShort(buffer, currentOffset + 6);
            currentOffset += 8;
            while (attributesCount-- > 0) {
                currentOffset += 6 + ByteUtil.readInt(buffer, currentOffset + 2);
            }
        }
        return currentOffset;
    }

    /**
     * 从字节码中定位到属性数据位置（包括两个字节的属性标识）
     *
     * @param flagStartPosition
     * @param buffer
     * @return
     */
    public static int seekToAttributePosition(int flagStartPosition, byte[] buffer) {
        //方法表开始位置
        int currentOffset = seekToMethodPosition(flagStartPosition, buffer);
        //方法表长度标识
        int methodsCount = ByteUtil.readUnsignedShort(buffer, currentOffset);
        currentOffset += 2;
        //跳过方法表
        while (methodsCount-- > 0) {
            int attributesCount = ByteUtil.readUnsignedShort(buffer, currentOffset + 6);
            currentOffset += 8;
            while (attributesCount-- > 0) {
                currentOffset += 6 + ByteUtil.readInt(buffer, currentOffset + 2);
            }
        }
        return currentOffset;
    }
}
