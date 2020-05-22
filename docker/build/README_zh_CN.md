## Dolphin Scheduler是什么?

一个分布式易扩展的可视化DAG工作流任务调度系统。致力于解决数据处理流程中错综复杂的依赖关系，使调度系统在数据处理流程中`开箱即用`。

Github URL: https://github.com/apache/incubator-dolphinscheduler

Official Website: https://dolphinscheduler.apache.org

![Dolphin Scheduler](https://dolphinscheduler.apache.org/img/hlogo_colorful.svg)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## 如何使用docker镜像

#### 你可以运行一个dolphinscheduler实例
```
$ docker run -dit --name dolphinscheduler \ 
-e POSTGRESQL_USERNAME=test -e POSTGRESQL_PASSWORD=test -e POSTGRESQL_DATABASE=dolphinscheduler \
-p 8888:8888 \
dolphinscheduler all
```

在`startup.sh`脚本中，默认的创建`Postgres`的用户、密码和数据库，默认值分别为：`root`、`root`、`dolphinscheduler`。

同时，默认的`Zookeeper`也会在`startup.sh`脚本中被创建。

#### 或者通过环境变量 **`POSTGRESQL_HOST`** **`POSTGRESQL_PORT`** **`ZOOKEEPER_QUORUM`** 使用已存在的服务

你可以指定一个已经存在的 **`Postgres`** 服务. 如下:

```
$ docker run -dit --name dolphinscheduler \
-e POSTGRESQL_HOST="192.168.x.x" -e POSTGRESQL_PORT="5432" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" \
-p 8888:8888 \
dolphinscheduler all
```

你也可以指定一个已经存在的 **Zookeeper** 服务. 如下:

```
$ docker run -dit --name dolphinscheduler \
-e ZOOKEEPER_QUORUM="l92.168.x.x:2181"
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-p 8888:8888 \
dolphinscheduler all
```

#### 或者运行dolphinscheduler中的部分服务

你能够运行dolphinscheduler中的部分服务。

* 启动一个 **master server**, 如下:

```
$ docker run -dit --name dolphinscheduler \
-e ZOOKEEPER_QUORUM="l92.168.x.x:2181"
-e POSTGRESQL_HOST="192.168.x.x" -e POSTGRESQL_PORT="5432" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" \
dolphinscheduler master-server
```

* 启动一个 **worker server**, 如下:

```
$ docker run -dit --name dolphinscheduler \
-e ZOOKEEPER_QUORUM="l92.168.x.x:2181"
-e POSTGRESQL_HOST="192.168.x.x" -e POSTGRESQL_PORT="5432" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" \
dolphinscheduler worker-server
```

* 启动一个 **api server**, 如下:

```
$ docker run -dit --name dolphinscheduler \
-e POSTGRESQL_HOST="192.168.x.x" -e POSTGRESQL_PORT="5432" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" \
-p 12345:12345 \
dolphinscheduler api-server
```

* 启动一个 **alert server**, 如下:

```
$ docker run -dit --name dolphinscheduler \
-e POSTGRESQL_HOST="192.168.x.x" -e POSTGRESQL_PORT="5432" -e POSTGRESQL_DATABASE="dolphinscheduler" \
-e POSTGRESQL_USERNAME="test" -e POSTGRESQL_PASSWORD="test" \
dolphinscheduler alert-server
```

* 启动一个 **frontend**, 如下:

```
$ docker run -dit --name dolphinscheduler \
-e FRONTEND_API_SERVER_HOST="192.168.x.x" -e FRONTEND_API_SERVER_PORT="12345" \
-p 8888:8888 \
dolphinscheduler frontend
```

**注意**: 当你运行dolphinscheduler中的部分服务时，你必须指定这些环境变量 `POSTGRESQL_HOST` `POSTGRESQL_PORT` `POSTGRESQL_DATABASE` `POSTGRESQL_USERNAME` `POSTGRESQL_PASSWORD` `ZOOKEEPER_QUORUM`。

## 如何构建一个docker镜像

你能够在类Unix系统和Windows系统中构建一个docker镜像。

类Unix系统, 如下:

```bash
$ cd path/incubator-dolphinscheduler
$ sh ./docker/build/hooks/build
```

Windows系统, 如下:

```bat
c:\incubator-dolphinscheduler>.\docker\build\hooks\build.bat
```

如果你不理解这些脚本 `./docker/build/hooks/build` `./docker/build/hooks/build.bat`，请阅读里面的内容。

## 环境变量

Dolphin Scheduler映像使用了几个容易遗漏的环境变量。虽然这些变量不是必须的，但是可以帮助你更容易配置镜像并根据你的需求定义相应的服务配置。

**`POSTGRESQL_HOST`**

配置`PostgreSQL`的`HOST`， 默认值 `127.0.0.1`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`POSTGRESQL_PORT`**

配置`PostgreSQL`的`PORT`， 默认值 `5432`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`POSTGRESQL_USERNAME`**

配置`PostgreSQL`的`USERNAME`， 默认值 `root`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`POSTGRESQL_PASSWORD`**

配置`PostgreSQL`的`PASSWORD`， 默认值 `root`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`POSTGRESQL_DATABASE`**

配置`PostgreSQL`的`DATABASE`， 默认值 `dolphinscheduler`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DOLPHINSCHEDULER_ENV_PATH`**

任务执行时的环境变量配置文件， 默认值 `/opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh`。

**`DOLPHINSCHEDULER_DATA_BASEDIR_PATH`**

用户数据目录, 用户自己配置, 请确保这个目录存在并且用户读写权限， 默认值 `/tmp/dolphinscheduler`。

**`ZOOKEEPER_QUORUM`**

配置`master-server`和`worker-serverr`的`Zookeeper`地址, 默认值 `127.0.0.1:2181`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`MASTER_EXEC_THREADS`**

配置`master-server`中的执行线程数量，默认值 `100`。

**`MASTER_EXEC_TASK_NUM`**

配置`master-server`中的执行任务数量，默认值 `20`。

**`MASTER_HEARTBEAT_INTERVAL`**

配置`master-server`中的心跳交互时间，默认值 `10`。

**`MASTER_TASK_COMMIT_RETRYTIMES`**

配置`master-server`中的任务提交重试次数，默认值 `5`。

**`MASTER_TASK_COMMIT_INTERVAL`**

配置`master-server`中的任务提交交互时间，默认值 `1000`。

**`MASTER_MAX_CPULOAD_AVG`**

配置`master-server`中的CPU中的`load average`值，默认值 `100`。

**`MASTER_RESERVED_MEMORY`**

配置`master-server`的保留内存，默认值 `0.1`。

**`MASTER_LISTEN_PORT`**

配置`master-server`的端口，默认值 `5678`。

**`WORKER_EXEC_THREADS`**

配置`worker-server`中的执行线程数量，默认值 `100`。

**`WORKER_HEARTBEAT_INTERVAL`**

配置`worker-server`中的心跳交互时间，默认值 `10`。

**`WORKER_FETCH_TASK_NUM`**

配置`worker-server`中的获取任务的数量，默认值 `3`。

**`WORKER_MAX_CPULOAD_AVG`**

配置`worker-server`中的CPU中的最大`load average`值，默认值 `100`。

**`WORKER_RESERVED_MEMORY`**

配置`worker-server`的保留内存，默认值 `0.1`。

**`WORKER_LISTEN_PORT`**

配置`worker-server`的端口，默认值 `1234`。

**`WORKER_GROUP`**

配置`worker-server`的分组，默认值 `default`。

**`XLS_FILE_PATH`**

配置`alert-server`的`XLS`文件的存储路径，默认值 `/tmp/xls`。

**`MAIL_SERVER_HOST`**

配置`alert-server`的邮件服务地址，默认值 `空`。

**`MAIL_SERVER_PORT`**

配置`alert-server`的邮件服务端口，默认值 `空`。

**`MAIL_SENDER`**

配置`alert-server`的邮件发送人，默认值 `空`。

**`MAIL_USER=`**

配置`alert-server`的邮件服务用户名，默认值 `空`。

**`MAIL_PASSWD`**

配置`alert-server`的邮件服务用户密码，默认值 `空`。

**`MAIL_SMTP_STARTTLS_ENABLE`**

配置`alert-server`的邮件服务是否启用TLS，默认值 `true`。

**`MAIL_SMTP_SSL_ENABLE`**

配置`alert-server`的邮件服务是否启用SSL，默认值 `false`。

**`MAIL_SMTP_SSL_TRUST`**

配置`alert-server`的邮件服务SSL的信任地址，默认值 `空`。

**`ENTERPRISE_WECHAT_ENABLE`**

配置`alert-server`的邮件服务是否启用企业微信，默认值 `false`。

**`ENTERPRISE_WECHAT_CORP_ID`**

配置`alert-server`的邮件服务企业微信`ID`，默认值 `空`。

**`ENTERPRISE_WECHAT_SECRET`**

配置`alert-server`的邮件服务企业微信`SECRET`，默认值 `空`。

**`ENTERPRISE_WECHAT_AGENT_ID`**

配置`alert-server`的邮件服务企业微信`AGENT_ID`，默认值 `空`。

**`ENTERPRISE_WECHAT_USERS`**

配置`alert-server`的邮件服务企业微信`USERS`，默认值 `空`。

**`FRONTEND_API_SERVER_HOST`**

配置`frontend`的连接`api-server`的地址，默认值 `127.0.0.1`。

**Note**: 当单独运行`api-server`时，你应该指定`api-server`这个值。

**`FRONTEND_API_SERVER_PORT`**

配置`frontend`的连接`api-server`的端口，默认值 `12345`。

**Note**: 当单独运行`api-server`时，你应该指定`api-server`这个值。

## 初始化脚本

如果你想在编译的时候或者运行的时候附加一些其它的操作及新增一些环境变量，你可以在`/root/start-init-conf.sh`文件中进行修改，同时如果涉及到配置文件的修改，请在`/opt/dolphinscheduler/conf/*.tpl`中修改相应的配置文件

例如，在`/root/start-init-conf.sh`添加一个环境变量`API_SERVER_PORT`：

```
export API_SERVER_PORT=5555
``` 

当添加以上环境变量后，你应该在相应的模板文件`/opt/dolphinscheduler/conf/application-api.properties.tpl`中添加这个环境变量配置:
```
server.port=${API_SERVER_PORT}
```

`/root/start-init-conf.sh`将根据模板文件动态的生成配置文件：

```sh
echo "generate app config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done

echo "generate nginx config"
sed -i "s/FRONTEND_API_SERVER_HOST/${FRONTEND_API_SERVER_HOST}/g" /etc/nginx/conf.d/dolphinscheduler.conf
sed -i "s/FRONTEND_API_SERVER_PORT/${FRONTEND_API_SERVER_PORT}/g" /etc/nginx/conf.d/dolphinscheduler.conf
```
