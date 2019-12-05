# 后端开发文档

## 环境要求

 * [Mysql](http://geek.analysys.cn/topic/124) (5.5+) :  必装
 * [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8+) :  必装
 * [ZooKeeper](https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper)(3.4.6+) ：必装 
 * [Maven](http://maven.apache.org/download.cgi)(3.3+) ：必装 

因EasyScheduler中escheduler-rpc模块使用到Grpc，需要用到Maven编译生成所需要的类
对maven不熟的伙伴请参考: [maven in five minutes](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)(3.3+)

http://maven.apache.org/install.html

## 项目编译
将EasyScheduler源码下载导入Idea开发工具后，首先转为Maven项目(右键点击后选择"Add Framework Support")

* 执行编译命令：

```
 mvn -U clean package assembly:assembly -Dmaven.test.skip=true
```

* 查看目录

正常编译完后，会在当前目录生成 ./target/escheduler-{version}/

```
    bin
    conf
    lib
    script
    sql
    install.sh
```

- 说明

```
bin : 基础服务启动脚本
conf : 项目配置文件
lib : 项目依赖jar包，包括各个模块jar和第三方jar
script : 集群启动、停止和服务监控启停脚本
sql : 项目依赖sql文件
install.sh : 一键部署脚本
```

   
