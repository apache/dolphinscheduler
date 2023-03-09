### 负载均衡

负载均衡即通过路由算法（通常是集群环境），合理的分摊服务器压力，达到服务器性能的最大优化。

### DolphinScheduler-Worker 负载均衡算法

DolphinScheduler-Master 分配任务至 worker,默认提供了三种算法:

加权随机（random）

平滑轮询（roundrobin）

线性负载（lowerweight）

默认配置为线性加权负载。

由于路由是在客户端做的，即 master 服务，因此你可以更改 master.properties 中的 master.host.selector 来配置你所想要的算法。

eg：master.host.selector=random（不区分大小写）

### Worker 负载均衡配置

配置文件 worker.properties

#### 权重

上述所有的负载算法都是基于权重来进行加权分配的，权重影响分流结果。你可以在 修改 worker.weight 的值来给不同的机器设置不同的权重。

#### 预热

考虑到 JIT 优化，我们会让 worker 在启动后低功率的运行一段时间，使其逐渐达到最佳状态，这段过程我们称之为预热。感兴趣的同学可以去阅读 JIT 相关的文章。

因此 worker 在启动后，他的权重会随着时间逐渐达到最大（默认十分钟，我们没有提供配置项，如果需要，你可以修改并提交相关的 PR）。

### 负载均衡算法细述

#### 随机（加权）

该算法比较简单，即在符合的 worker 中随机选取一台（权重会影响他的比重）。

#### 平滑轮询（加权）

加权轮询算法一个明显的缺陷。即在某些特殊的权重下，加权轮询调度会生成不均匀的实例序列，这种不平滑的负载可能会使某些实例出现瞬时高负载的现象，导致系统存在宕机的风险。为了解决这个调度缺陷，我们提供了平滑加权轮询算法。

每台 worker 都有两个权重，即 weight（预热完成后保持不变），current_weight（动态变化），每次路由。都会遍历所有的 worker，使其 current_weight+weight，同时累加所有 worker 的 weight，计为  total_weight，然后挑选 current_weight 最大的作为本次执行任务的 worker，与此同时，将这台 worker 的 current_weight-total_weight。

#### 线性加权(默认算法)

该算法每隔一段时间会向注册中心上报自己的负载信息。我们主要根据两个信息来进行判断

* load 平均值（默认是 CPU 核数 *2）
* 可用物理内存（默认是 0.3，单位是 G）

如果两者任何一个低于配置项，那么这台 worker 将不参与负载。（即不分配流量）

你可以在 worker.properties 修改下面的属性来自定义配置

* worker.max.cpuload.avg=-1 (worker最大cpuload均值，只有高于系统cpuload均值时，worker服务才能被派发任务. 默认值为-1: cpu cores * 2)
* worker.reserved.memory=0.3 (worker预留内存，只有低于系统可用内存时，worker服务才能被派发任务，单位为G)

