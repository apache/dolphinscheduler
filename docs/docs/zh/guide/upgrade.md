
# DolphinScheduler升级文档

## 1. 备份上一版本文件和数据库

## 2. 停止dolphinscheduler所有服务

 `sh ./script/stop-all.sh`

## 3. 下载新版本的安装包

- [下载](/zh-cn/download/download.html), 下载最新版本的二进制安装包
- 以下升级操作都需要在新版本的目录进行

## 4. 数据库升级

- 如果选择 MySQL，请修改`./bin/env/dolphinscheduler_env.sh`中的如下配置（{user}和{password}改成你数据库的用户名和密码）, 还需要手动添加 [ mysql-connector-java 驱动 jar ](https://downloads.MySQL.com/archives/c-j/) 包到 lib 目录（`./tools/lib`）下，这里下载的是mysql-connector-java-8.0.16.jar

    ```shell
    export DATABASE=${DATABASE:-mysql}
    export SPRING_PROFILES_ACTIVE=${DATABASE}
    export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
    export SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
    export SPRING_DATASOURCE_USERNAME={user}
    export SPRING_DATASOURCE_PASSWORD={password}
    ```

- 执行数据库升级脚本

`sh ./tools/bin/upgrade-schema.sh`

## 5. 服务升级

### 5.1 修改`conf/config/install_config.conf`配置内容
单机部署请参照[单机部署(Standalone)](./installation/standalone.md)中的`6.修改运行参数部分`
集群部署请参照[集群部署(Cluster)](./installation/cluster.md)中的`6.修改运行参数部分`

### 注意事项
创建worker分组在1.3.1版本和之前版本有了不同的设计

- worker分组在1.3.1版本之前是通过UI界面创建
- worker分组在1.3.1版本是修改worker配置指定

### 1.3.1之前的版本升级1.3.2时如何设置worker分组与之前一致

1、查询已备份的数据库,查看t_ds_worker_group表记录，重点看下id、name和ip_list三个字段

| id | name | ip_list    |
| :---         |     :---:      |          ---: |
| 1   | service1     | 192.168.xx.10    |
| 2   | service2     | 192.168.xx.11,192.168.xx.12      |

2、修改conf/config/install_config.conf中的workers参数

假设以下为要部署的worker主机名和ip的对应关系
| 主机名 | ip |
| :---  | :---:  |
| ds1   | 192.168.xx.10     |
| ds2   | 192.168.xx.11     |
| ds3   | 192.168.xx.12     |

那么为了保持与之前版本worker分组一致，则需要把workers参数改为如下

```shell
#worker服务部署在哪台机器上,并指定此worker属于哪一个worker组
workers="ds1:service1,ds2:service2,ds3:service2"
```

### 1.3.2的worker分组进行了增强
1.3.1版本的worker不能同时属于多个worker分组，1.3.2是可以支持的
所以在1.3.1里面的workers="ds1:service1,ds1:service2"是不支持的，
在1.3.2可以设置workers="ds1:service1,ds1:service2"
  
### 5.2 执行部署脚本
```shell
`sh install.sh`
```


