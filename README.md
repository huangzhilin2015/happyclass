# happyclass
happyclass基于javaagent实现java项目字节码层面的加解密，加大第三方获取源码的难度，以支持代码安全发布。同时，项目集成proguard混淆插件，支持对工具包进行代码混淆处理。
### 项目结构
整个项目包含以下两个子项目：
#### happyclass-core：核心算法逻辑
#### happyclass-execute：加解密主方法入口，负责解析命令行参数，创建加解密器
### 使用
    mvn clean install

会在target目录生成happyclass-execute-1.0.jar和happyclass-execute-1.0-pg.jar两个jar包，-pg.jar为混淆之后的工具，配合编码，可以增大他人解析工具源码的难度

### 带参数运行jar包进行代码加密：

    java -jar happyclass-execute-1.0.jar(或happyclass-execute-1.0-pg.jar)

#### 提供以下参数：

- (必传)-s：加密算法选择:
  - 1：此算法虚构长度为0的字段表和方法表，原始字节加密之后拼接到密文末端，加解密都需要密钥参数
  - 2：此算法重写了方法体（清空），原始字节加密之后拼接到密文末端，加解密需要密钥参数
  - 3：此算法同2算法，密钥从密钥管理服务器获取，解密不需要密钥参数(秘钥服务地址和字节转换规则硬编码在代码中，使用此算法需要修改工具代码，见后文)，加密需要密钥参数
- (非必传)-t：需要加密的目标目录或class或jar，对于目录，目前只支持标准web结构
- (非必传)-j：-t指定的是加密目录时，该目录下可能包含多个jar包，此参数指定这些jar包中，哪些需要加密。典型的是WEB-INF/lib下的jar包
- (非必传)-p：加密使用的密钥，具体需要参照选择的算法
- (非必传)-h：帮助信息
- (非必传)-e：排除的类名，多个逗号分割
- (非必传)-o：指定输出目录，默认为当前目录
- (非必传)-d：开启调试模式

### 加密示例

- java -jar happyclass-execute-1.0.jar -h

    显示帮助信息

- java -jar happyclass-execute-1.0.jar -s 2 -p password123 -t D:\work\java\demoApp -j custom-1.0.jar -o D:\work\java\encrypt -d

    以debug模式启动，使用算法2(重写方法体)，密钥"password123"，加密项目"D:\work\java\demoApp"，该项目需要是标准web结构，同时加密WEB-INF/lib/custom-1.0.jar，lib下的其它jar包保持原样，结果输出到目录"D:\work\java\encrypt"
    
### 解密

此工具以javaagent方式提供服务，在JVM启动参数处加上javaagent即可。

**加jvm启动参数**：

- -javaagent:D:\data\happyclass-execute-1.0.jar=s=2&p=echatsoft

  使用密码"echatsoft",根据算法2解密字节码
  
- -javaagent:D:\data\happyclass-execute-1.0.jar=s=3

  根据算法3解密字节码，此时密钥会从密钥管理服务器获取，地址在工具中硬编码
