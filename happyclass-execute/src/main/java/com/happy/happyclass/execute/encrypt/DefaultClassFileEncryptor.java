package com.happy.happyclass.execute.encrypt;

import com.happy.happyclass.core.context.HappyRunContext;
import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.executor.Executor;
import com.happy.happyclass.core.info.ClassInfo;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.interceptor.impl.EncryptClassInterceptor;
import com.happy.happyclass.core.util.ByteUtil;
import com.happy.happyclass.core.util.FileUtil;
import com.happy.happyclass.core.util.LogUtil;
import com.happy.happyclass.execute.constant.EncryptConstantPool;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
@Getter
@Setter
public class DefaultClassFileEncryptor implements ClassFileEncryptor {
    private Executor executor;
    private File target;
    private String targetSuffix;
    //排除的类名
    private Set<String> excludes;
    //需要加密的jar包
    private Set<String> jars;
    private ExecuteContextInfo contextInfo;
    //加密文件输出目录
    private String output;
    //最终加密的文件数量
    private int encryptedNumber = 0;
    //失败数量
    private int failureNumber = 0;
    private String sourceParent;
    private String finalDir;

    /**
     * @param executor
     * @param target
     */
    public DefaultClassFileEncryptor(Executor executor, String target) {
        this(executor, target, null, null, null, null);
    }

    /**
     * @param executor
     * @param target
     * @param excludes
     * @param jars
     * @param contextInfo
     * @param output
     */
    public DefaultClassFileEncryptor(Executor executor, String target, String excludes, String jars, ExecuteContextInfo contextInfo, String output) {
        if (executor == null || StringUtils.isEmpty(target)) {
            throw new HappyException("executor or target cannot be null.");
        }
        this.executor = executor;
        this.target = new File(target);
        if (!this.target.exists()) {
            throw new HappyException(target + " 不存在!");
        }
        if (!this.target.isDirectory()) {
            int index = this.target.getName().lastIndexOf(".");
            if (index >= 0) {
                this.targetSuffix = this.target.getName().substring(index + 1);
            }
            if (StringUtils.isEmpty(this.targetSuffix)) {
                throw new HappyException("unsupported target:" + target);
            }
        } else {
            String targetName = this.target.getName().endsWith(File.separator)
                    ? this.getTarget().getName().substring(0, this.getTarget().getName().length() - 1)
                    : this.getTarget().getName();
            this.finalDir = targetName + EncryptConstantPool.ENCRYPTED_SUFFIX;
        }
        this.sourceParent = this.target.getParent();
        if (StringUtils.isNotEmpty(excludes)) {
            this.excludes = new HashSet<>(Arrays.asList(excludes.split(EncryptConstantPool.COMMON_SEPERATOR)));
        } else {
            this.excludes = Collections.emptySet();
        }
        if (StringUtils.isNotEmpty(jars)) {
            this.jars = new HashSet<>(Arrays.asList(jars.split(EncryptConstantPool.COMMON_SEPERATOR)));
        } else {
            this.jars = Collections.emptySet();
        }
        this.contextInfo = contextInfo;
        if (StringUtils.isEmpty(output)) {
            //如果没有指定输出目录,那么使用相同目录
            this.output = sourceParent;

        } else {
            if (FileUtil.isSameDirectory(output, sourceParent)) {
                //如果手动指定了output，则不允许和原目录相同
                throw new HappyException("output cannot be same as source.");
            }
            this.output = output;
        }
    }

    /**
     * @throws HappyException
     */
    @Override
    public void doEncrypt() throws IOException, HappyException {
        LogUtil.print("######即将开始加密######");
        LogUtil.print(">>target:" + target);
        LogUtil.print(">>exclude:" + excludes);
        LogUtil.print(">>jars:" + jars);
        LogUtil.print(">>password:********");
        LogUtil.print(">>output:" + output);
        LogUtil.print(">>开始加密，请稍后");
        long start = System.currentTimeMillis();
        //解析文件
        //区分不同的文件类型
        if (StringUtils.isEmpty(targetSuffix)) {
            //目录
            handleDirectory(target);
        } else if (EncryptConstantPool.SupportedFileSuffix.JAR.equals(targetSuffix)) {
            //jar包
            handleJar(target);
        } else if (EncryptConstantPool.SupportedFileSuffix.CLASS.equals(targetSuffix)) {
            //单个class
            handleFile(target);
        } else {
            throw new HappyException("不支持的目标:" + target);
        }
        HappyRunContext.clearRunContextInfo();
        LogUtil.print("###### 加密完成，加密文件数量：" + encryptedNumber + ",失败数量：" + failureNumber + ",消耗时间：" + (System.currentTimeMillis() - start) / 1000 + "(s) ######");
    }

    /**
     * 处理标准WEB目录
     *
     * @param file
     */
    private void handleDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            //对文件夹进行递归处理
            for (File item : file.listFiles()) {
                handleDirectory(item);
            }
        } else {
            handleFile(file);
        }
    }

    /**
     * 处理单个文件
     *
     * @param file
     * @return
     */
    private void handleFile(File file) throws IOException {
        if (file.getName().endsWith(".class")) {
            if (excludes.contains(file.getName())) {
                LogUtil.debug("跳过加密:" + file.getAbsolutePath());
                copyFile(file);
                return;
            }
            LogUtil.debug("加密文件：" + file.getAbsolutePath());
            File tmpFile = createTmpFile(file);
            if (tmpFile == null || !tmpFile.exists()) {
                throw new HappyException("create tmp file failed.based:" + file.getAbsolutePath());
            }
            byte[] buffer = ByteUtil.readStream(new FileInputStream(file), true);
            //创建一个classInfo
            ClassInfo classInfo = new ClassInfo(file.getAbsolutePath(), new ByteArrayInputStream(buffer));
            //获取加密后的数据
            byte[] encryptedData;
            try {
                encryptedData = executor.encrypt(classInfo, contextInfo);
            } catch (Exception e) {
                LogUtil.print("处理失败,跳过：" + file.getAbsolutePath());
                failureNumber++;
                encryptedData = classInfo.getClassFileBuffer();
            } finally {
                classInfo.destroy();
            }
            //将加密后的数据写入到tmpFile
            try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
                fileOutputStream.write(encryptedData, 0, encryptedData.length);
                fileOutputStream.flush();
            }
            encryptedNumber++;
        } else if (file.getName().endsWith(".jar")) {
            if (jars.contains(file.getName())) {
                //处理jar包
                handleJar(file);
            } else {
                copyFile(file);
            }
        } else if (!EncryptConstantPool.class.equals(targetSuffix)) {
            //这个判断其实不需要
            //拷贝文件
            copyFile(file);
        }
    }

    /**
     * 根据上下文递归一个临时文件
     *
     * @param file
     * @return
     */
    private File createTmpFile(File file) throws IOException {
        return FileUtil.createFile(buildFileName(file, false));
    }

    /**
     * 处理jar包
     * jar包中的字节码全部加密，不受excludes影响
     *
     * @param file
     */
    private void handleJar(File file) throws IOException {
        if (!file.getName().endsWith(".jar")) {
            return;
        }
        LogUtil.debug("加密jar:" + file.getAbsolutePath());
        //首先获取解压文件的文件夹
        final String filePath = buildFileName(file, false);
        String directory = filePath.substring(0, filePath.indexOf(".jar"));
        LogUtil.debug("解压文件:" + file.getAbsolutePath() + " =>> " + directory);
        EncryptClassInterceptor interceptor = new EncryptClassInterceptor(executor, file, contextInfo, excludes);
        FileUtil.unpack(file, directory, interceptor);
        encryptedNumber += interceptor.getEncrypted();
        //压缩回jar包
        LogUtil.debug("压缩文件：" + directory + " =>> " + filePath);
        FileUtil.pack(directory, filePath);
        //压缩后删除文件
        FileUtil.deleteFile(new File(directory));
    }

    /**
     * @param file
     * @throws IOException
     */
    private void copyFile(File file) throws IOException {
        String newFileName = buildFileName(file, true);
        FileUtil.copyFile(file, newFileName);
    }

    /**
     * @param file
     * @param isCopy
     * @return
     */
    private String buildFileName(File file, boolean isCopy) {
        String fileParent;
        String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(target.getAbsolutePath()) + target.getAbsolutePath().length());

        if (!isCopy && StringUtils.isEmpty(finalDir)) {
            //文件名加上后缀
            fileParent = output;
            fileName = file.getName() + EncryptConstantPool.ENCRYPTED_FILE_SUFFIX;
        } else {
            fileParent = FileUtil.composeFilePath(output, finalDir);
        }
        return FileUtil.composeFilePath(fileParent, fileName);
    }

    /**
     * @return
     */
    public String getJars() {
        if (jars == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String jar : jars) {
                sb.append(jar);
            }
            return sb.toString();
        }
    }

}
