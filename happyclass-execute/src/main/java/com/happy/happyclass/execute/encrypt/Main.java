package com.happy.happyclass.execute.encrypt;

import com.happy.happyclass.core.context.HappyRunContext;
import com.happy.happyclass.core.context.RunContextInfo;
import com.happy.happyclass.core.exception.HappyException;
import com.happy.happyclass.core.executor.Executor;
import com.happy.happyclass.core.executor.ExecutorManager;
import com.happy.happyclass.core.info.ExecuteContextInfo;
import com.happy.happyclass.core.util.LogUtil;
import com.happy.happyclass.execute.constant.EncryptConstantPool;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * 负责加密，默认加密classes下的所有class
 * Author huangzhilin
 * Date 2020/1/3
 */
public class Main {
    /**
     * @param args
     */
    public static void main(String[] args) throws IOException, HappyException {
        if (args == null || args.length == 0) {
            throw new HappyException("参数错误!-h查看帮助信息");
        }
        //解析命令行参数
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption(EncryptConstantPool.HELP, false, "帮助信息");
        options.addOption(EncryptConstantPool.SIGN, true, "加解密算法");
        options.addOption(EncryptConstantPool.PASSWORD, true, "密码");
        options.addOption(EncryptConstantPool.TARGET, true, "需要加密的目标目录或文件");
        options.addOption(EncryptConstantPool.JAR, true, "需要加密的依赖jar包，多个逗号分割");
        options.addOption(EncryptConstantPool.EXCLUDE, true, "排除的类名，多个逗号分割");
        options.addOption(EncryptConstantPool.OUTPUT, true, "输出目录");
        options.addOption(EncryptConstantPool.DEBUG, false, "debug模式");
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException var1) {
            throw new HappyException(var1.getMessage());
        }
        if (commandLine.hasOption(EncryptConstantPool.HELP)) {
            //打印帮助信息
            printHelpInfo();
            return;
        }
        if (commandLine.hasOption(EncryptConstantPool.DEBUG)) {
            HappyRunContext.initRunContextInfo(new RunContextInfo(true));
        } else {
            HappyRunContext.initRunContextInfo(new RunContextInfo(false));
        }
        //进行加密
        Executor executor = ExecutorManager.findExecutor(commandLine.getOptionValue(EncryptConstantPool.SIGN));
        ExecuteContextInfo contextInfo = new ExecuteContextInfo(commandLine.getOptionValue(EncryptConstantPool.PASSWORD), true);
        if (!executor.isValid(contextInfo)) {
            throw new HappyException("在当前参数下，该算法不可用");
        }
        ClassFileEncryptor classFileEncryptor = new DefaultClassFileEncryptor(ExecutorManager.findExecutor(commandLine.getOptionValue(EncryptConstantPool.SIGN)),
                commandLine.getOptionValue(EncryptConstantPool.TARGET),
                commandLine.getOptionValue(EncryptConstantPool.EXCLUDE),
                commandLine.getOptionValue(EncryptConstantPool.JAR),
                contextInfo,
                commandLine.getOptionValue(EncryptConstantPool.OUTPUT));
        classFileEncryptor.doEncrypt();
    }

    /**
     * 打印帮助信息
     */
    private static void printHelpInfo() {
        LogUtil.print("参数：\n" +
                "[必传]-s：加密算法选择，目前提供：1、2、3\n" +
                "[非必传]-t：需要加密的目标目录或class或jar，对于目录，目前只支持标准web结构\n" +
                "[非必传]-j：-t指定的是加密目录时，该目录下可能包含多个jar包，此参数指定这些jar包中哪些需要加密。典型的是WEB-INF/lib下的jar包\n" +
                "[非必传]-p：加密使用的密钥，具体需要参照选择的算法\n" +
                "[非必传]-h：帮助信息\n" +
                "[非必传]-e：排除的类名，多个逗号分割\n" +
                "[非必传]-o：指定输出目录，默认为当前目录\n" +
                "[非必传]-d：开启调试模式");
    }

}
