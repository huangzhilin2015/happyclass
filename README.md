# happyclass
happyclass基于javaagent实现java项目字节码层面的加解密，加大第三方获取源码的难度，以支持代码安全发布。同时，项目集成proguard混淆插件，支持对工具包进行代码混淆处理。
### 项目结构
整个项目包含以下两个子项目：
#### happyclass-core：核心算法逻辑
#### happyclass-execute：加解密主方法入口，负责解析命令行参数，创建加解密器
### 使用
