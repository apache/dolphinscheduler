Q:单机运行服务老挂，应该是内存不够，测试机器4核8G。生产环境需要分布式，如果单机的话建议的配置是？

A: Easy Scheduler有5个服务组成，这些服务本身需要的内存和cpu不多，

| 服务         | 内存 | cpu核数 |
| ------------ | ---- | ------- |
| MasterServer | 2G   | 2核     |
| WorkerServer | 2G   | 2核     |
| ApiServer    | 512M | 1核     |
| AlertServer  | 512M | 1核     |
| LoggerServer | 512M | 1核     |

注意：由于如果任务较多，WorkServer所在机器建议物理内存在16G以上



---

Q: 管理员为什么不能创建项目？

A: 管理员目前属于"纯管理", 没有租户，即没有linux上对应的用户，所以没有执行权限,  但是有所有的查看权限。如果需要创建项目等业务操作，请使用管理员创建租户和普通用户，然后使用普通用户登录进行操作

---

Q: 系统支持哪些邮箱？

A: 支持绝大多数邮箱，qq、163、126、139、outlook、aliyun等皆可支持

---

Q:常用的系统变量时间参数有哪些,如何使用？

A: 请参考使用手册中的系统参数

---

Q：pip install kazoo 这个安装报错。是必须安装的吗？

A: 这个是python连接zookeeper需要使用到的

---

Q: 如果alert、api、logger服务任意一个宕机，任何还会正常执行吧

A:   不影响，影响正在运行中的任务的服务有Master和Worker服务

---

Q: 这个怎么指定机器运行任务的啊 」

A: 通过worker分组： 这个流程只能在指定的机器组里执行。默认是Default，可以在任一worker上执行。

---

Q: 跨用户的任务依赖怎么实现呢， 比如A用户写了一个任务，B用户需要依赖这个任务

就比如说 我们数仓组 写了一个 中间宽表的任务， 其他业务部门想要使用这个中间表的时候，他们应该是另外一个用户，怎么依赖这个中间表呢

A: 有两种情况，一个是要运行这个宽表任务，可以使用子工作流把宽表任务放到自己的工作流里面。另一个是检查这个宽表任务有没有完成，可以使用依赖节点来检查这个宽表任务在指定的时间周期有没有完成。

---

Q: 启动WorkerServer服务时不能正常启动，报以下信息是什么原因？

```
[INFO] 2019-05-06 16:39:31.492 cn.escheduler.server.zk.ZKWorkerClient:[155] - register failure , worker already started on : 127.0.0.1, please wait for a moment and try again
```

A：Worker/Master Server在启动时，会向Zookeeper注册自己的启动信息，是Zookeeper的临时节点，如果两次启动时间间隔较短的情况，上次启动的Worker/Master Server在Zookeeper的会话还未过期，会出现上述信息，处理办法是等待session过期，一般是1分钟左右

----

Q: 编译时escheduler-grpc模块一直报错：Information:java: Errors occurred while compiling module 'escheduler-rpc'， 找不到LogParameter、RetStrInfo、RetByteInfo等class类

A: 这是因为rpc源码包是google Grpc实现的，需要使用maven进行编译，在根目录下执行：mvn -U clean package assembly:assembly -Dmaven.test.skip=true ， 然后刷新下整个项目

----

Q：EasyScheduler支持windows上运行么？

A:  建议在Ubuntu、Centos上运行，暂不支持windows上运行，不过windows上可以进行编译。开发调试的话建议Ubuntu或者mac上进行。

-----

Q：任务为什么不执行？

A: 不执行的原因：

查看command表里有没有内容？

查看Master server的运行日志：

查看Worker Server的运行日志



