# 负载均衡

负载均衡即通过路由算法（通常是集群环境），合理的分摊服务器压力，达到服务器性能的最大优化。

## DolphinScheduler-Worker 负载均衡算法

DolphinScheduler-Master 分配任务至 worker，提供了四种负载均衡算法：

- **随机** (RANDOM)
- **轮询** (ROUND_ROBIN)
- **平滑轮询** (FIXED_WEIGHTED_ROUND_ROBIN)
- **动态平滑轮询** (DYNAMIC_WEIGHTED_ROUND_ROBIN) - 默认算法

## 负载均衡算法配置

在配置文件中配置负载均衡算法：

位置：`master-server/conf/application.yaml`

```yaml
worker-load-balancer-configuration-properties:
  # 负载均衡算法类型：RANDOM, ROUND_ROBIN, FIXED_WEIGHTED_ROUND_ROBIN, DYNAMIC_WEIGHTED_ROUND_ROBIN
  type: DYNAMIC_WEIGHTED_ROUND_ROBIN
```

## Worker 权重配置

### 平滑轮询权重配置 (FIXED_WEIGHTED_ROUND_ROBIN)

对于 `FIXED_WEIGHTED_ROUND_ROBIN` 算法，可以在每个 worker 的配置文件中修改固定权重：

位置：`worker-server/conf/application.yaml`

```yaml
worker:
  host-weight: 100 #默认值为 100
```

### 动态平滑轮询权重配置 (DYNAMIC_WEIGHTED_ROUND_ROBIN)

当使用 `DYNAMIC_WEIGHTED_ROUND_ROBIN` 算法时，可以配置各项指标的权重：

位置：`master-server/conf/application.yaml`

```yaml
master:
  worker-load-balancer-configuration-properties:
    type: DYNAMIC_WEIGHTED_ROUND_ROBIN
    # 动态权重配置，仅用于 DYNAMIC_WEIGHTED_ROUND_ROBIN 算法
    # memory-usage、cpu-usage、task-thread-pool-usage 的权重总和必须为 100
    dynamic-weight-config-properties:
      memory-usage-weight: 30    # 内存使用率权重
      cpu-usage-weight: 30       # CPU 使用率权重  
      task-thread-pool-usage-weight: 40  # 任务线程池使用率权重
```

## 负载均衡算法详解

### 随机 (RANDOM)

在可用的 worker 节点中随机选择一台执行任务。

### 轮询 (ROUND_ROBIN)

按照固定的顺序依次选择 worker 节点执行任务，确保每个 worker 都能均匀分配到任务。

### 平滑轮询 (FIXED_WEIGHTED_ROUND_ROBIN)

每台 worker 都有两个权重，即 weight（预热完成后保持不变），
current_weight（动态变化），每次路由。都会遍历所有的 worker，使其 current_weight+weight，同时累加所有 worker 的 weight，计为 total_weight，然后挑选 current_weight 最大的作为本次执行任务的 worker，与此同时，将这台 worker 的 current_weight-total_weight。
- 示例：例如有 3 个 worker （A, B, C） 的权重分别为 1、2、3
- worker 选择顺序将为：C B C A B C C B C A B C C B C A B C C B C A B C C B C A B C ... (在上述30轮的调度例子中，每个 worker 分配任务数量：C:15, B:10, A:5, 恰好与权重比例相匹配)

### 动态平滑轮询 (DYNAMIC_WEIGHTED_ROUND_ROBIN) - 默认算法

该算法每隔一段时间 worker 会向注册中心上报自己的负载信息。我们主要根据CPU使用率、
内存使用率以及 worker 线程池使用率使用情况来进行判断，具体权重配置如下：
- **内存使用率** (默认权重 30%)
- **CPU 使用率** (默认权重 30%)
- **任务线程池使用率** (默认权重 40%)

每个 worker 的动态权重通过以下公式计算：

```
权重 = 100 - (CPU权重 × CPU使用率 + 内存权重 × 内存使用率 + 线程池权重 × 线程池使用率) ÷ 3
```

所以当 worker 的负载越低权重就会越高，系统会优先选择负载较低的 worker 节点执行任务。

在最终选择 worker 节点时，流程与平滑轮询相同，唯一的区别是，该算法下 worker 的权重会动态变化。
通过这种动态平滑轮询算法，DolphinScheduler 能够智能地将任务分配到负载最低的 worker 上，实现真正的动态负载均衡。
