### 缓存

#### 缓存目的

由于在master-server调度过程中，会产生大量的数据库读取操作，如tenant，user，processDefinition等，一方面对DB产生很大的读压力，另一方面则会使整个核心调度流程变得缓慢；

考虑到这部分业务数据是读多写少的场景，故引入了缓存模块，以减少DB读压力，加快核心调度流程；

#### 缓存设置

```yaml
spring:
  cache:
    # default enable cache, you can disable by `type: none`
    type: none
    cache-names:
      - tenant
      - user
      - processDefinition
      - processTaskRelation
      - taskDefinition
    caffeine:
      spec: maximumSize=100,expireAfterWrite=300s,recordStats
```

缓存模块采用[spring-cache](https://spring.io/guides/gs/caching/)机制，可直接在spring配置文件中配置是否开启缓存（默认`none`关闭）, 缓存类型；

目前采用[caffeine](https://github.com/ben-manes/caffeine)进行缓存管理，可自由设置缓存相关配置，如缓存大小、过期时间等；

#### 缓存读取

缓存采用spring-cache的注解，配置在相关的mapper层，可参考如：`TenantMapper`.

#### 缓存更新

业务数据的更新来自于api-server, 而缓存端在master-server, 故需要对api-server的数据更新做监听(aspect切面拦截`@CacheEvict`)，当需要进行缓存驱逐时会通知master-server，master-server接收到cacheEvictCommand后进行缓存驱逐；

需要注意的是：缓存更新的兜底策略来自于用户在caffeine中的过期策略配置，请结合业务进行配置；

时序图如下图所示：

<img src="../../../img/cache-evict.png" alt="cache-evict" style="zoom: 67%;" />
