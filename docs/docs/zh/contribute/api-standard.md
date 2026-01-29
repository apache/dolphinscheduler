# API 设计规范

规范统一的 API 是项目设计的基石。DolphinScheduler 的 API 遵循 REST ful 标准，REST ful 是目前最流行的一种互联网软件架构，它结构清晰，符合标准，易于理解，扩展方便。

本文以 DolphinScheduler 项目的接口为样例，讲解如何构造具有 Restful 风格的 API。

## 1. URI 设计

REST 即为 Representational State Transfer 的缩写，即“表现层状态转化”。

“表现层”指的就是“资源”。资源对应网络上的一种实体，例如：一段文本，一张图片，一种服务。且每种资源都对应一个特定的 URI。

Restful URI 的设计基于资源：
+ 一类资源：用复数表示，如 `task-instances`、`groups` 等；
+ 单个资源：用单数，或是用 id 值表示某类资源下的一个，如 `group`、`groups/{groupId}`；
+ 子资源：某个资源下的资源：`/instances/{instanceId}/tasks`；
+ 子资源下的单个资源：`/instances/{instanceId}/tasks/{taskId}`；

## 2. Method 设计

我们需要通过 URI 来定位某种资源，再通过 Method，或者在路径后缀声明动作来体现对资源的操作。

### ① 查询操作 - GET

通过 URI 来定位要资源，通过 GET 表示查询。

+ 当 URI 为一类资源时表示查询一类资源，例如下面样例表示分页查询 `alter-groups`。

```
Method: GET
/dolphinscheduler/alert-groups
```

+ 当 URI 为单个资源时表示查询此资源，例如下面样例表示查询对应的 `alter-group`。

```
Method: GET
/dolphinscheduler/alter-groups/{id}
```

+ 此外，我们还可以根据 URI 来表示查询子资源，如下：

```
Method: GET
/dolphinscheduler/projects/{projectId}/tasks
```

**上述的关于查询的方式都表示分页查询，如果我们需要查询全部数据的话，则需在 URI 的后面加 `/list` 来区分。分页查询和查询全部不要混用一个 API。**

```
Method: GET
/dolphinscheduler/alert-groups/list
```

### ② 创建操作 - POST

通过 URI 来定位要创建的资源类型，通过 POST 表示创建动作，并且将创建后的 `id` 返回给请求者。

+ 下面样例表示创建一个 `alter-group`：

```
Method: POST
/dolphinscheduler/alter-groups
```

+ 创建子资源也是类似的操作：

```
Method: POST
/dolphinscheduler/alter-groups/{alterGroupId}/tasks
```

### ③ 修改操作 - PUT

通过 URI 来定位某一资源，通过 PUT 指定对其修改。

```
Method: PUT
/dolphinscheduler/alter-groups/{alterGroupId}
```

### ④ 删除操作 -DELETE

通过 URI 来定位某一资源，通过 DELETE 指定对其删除。

+ 下面例子表示删除 `alterGroupId` 对应的资源：

```
Method: DELETE
/dolphinscheduler/alter-groups/{alterGroupId}
```

+ 批量删除：对传入的 id 数组进行批量删除，使用 POST 方法。**（这里不要用 DELETE 方法，因为 DELETE 请求的 body 在语义上没有任何意义，而且有可能一些网关，代理，防火墙在收到 DELETE 请求后会把请求的 body 直接剥离掉。）**

```
Method: POST
/dolphinscheduler/alter-groups/batch-delete
```

### ⑤ 部分更新操作 -PATCH

通过 URI 来定位某一资源，通过 PATCH 指定对其部分更新。

+ 下面例子表示部分更新 `alterGroupId` 对应的资源：

```
Method: PATCH
/dolphinscheduler/alter-groups/{alterGroupId}
```

### ⑥ 其他操作

除增删改查外的操作，我们同样也通过 `url` 定位到对应的资源，然后再在路径后面追加对其进行的操作。例如：

```
/dolphinscheduler/alert-groups/verify-name
/dolphinscheduler/projects/{projectCode}/process-instances/{code}/view-gantt
```

## 3. 参数设计

参数分为两种，一种是请求参数(Request Param 或 Request Body)，另一种是路径参数(Path Param)。

参数变量必须用小驼峰表示，并且在分页场景中，用户输入的参数小于 1，则前端需要返给后端 1 表示请求第一页；当后端发现用户输入的参数大于总页数时，直接返回最后一页。

## 4. 其他设计

### 基础路径

整个项目的 URI 需要以 `/<project_name>` 作为基础路径，从而标识这类 API 都是项目下的，即：

```
/dolphinscheduler
```

