### Dolphin Scheduler的Ambari插件使用说明

##### 备注

1. 本文档适用于对Ambari中基本了解的用户
2. 本文档是对已安装Ambari服务添加Dolphin Scheduler(1.3.0版本)服务的说明

##### 一  安装准备

1. 准备RPM包

   - 在源码dolphinscheduler-dist目录下执行命令```mvn -U clean install rpm:attached-rpm  -Prpmbuild  -Dmaven.test.skip=true -X```即可生成(在目录 dolphinscheduler-dist/target/rpm/apache-dolphinscheduler-incubating/RPMS/noarch 下)

2. 创建DS的安装用户--权限

3. 初始化数据库信息

   ```
   -- 创建Dolphin Scheduler的数据库：dolphinscheduler
   CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE
   utf8_general_ci;
   
   -- 初始化dolphinscheduler数据库的用户和密码，并分配权限
   -- 替换下面sql语句中的{user}为dolphinscheduler数据库的用户
   GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
   GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY
   '{password}';
   flush privileges;
   ```

 

##### 二  Ambari安装Dolphin Scheduler

1. Ambari界面安装Dolphin Scheduler

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_001.png)

2. 选择Dolphin Scheduler的Master安装的节点

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_002.png)

3. 配置Dolphin Scheduler的Worker、Api、Logger、Alert安装的节点

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_003.png)

4. 设置Dolphin Scheduler服务的安装用户（**步骤一中创建的**）及所属的用户组

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_004.png)

5. 配置数据库的信息（和步骤一中初始化数据库中一致）

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_005.png)

6. 配置其它的信息--如果需要的话

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_006.png)

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_007.png)

7. 正常执行接下来的步骤

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_008.png)

8. 安装成功后的界面

   ![](https://github.com/apache/incubator-dolphinscheduler-website/blob/master/img/ambari-plugin/DS2_AMBARI_009.png)

