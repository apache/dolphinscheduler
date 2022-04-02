# 参数的引用

DolphinScheduler 提供参数间相互引用的能力，包括：本地参数引用全局参数、上下游参数传递。因为有引用的存在，就涉及当参数名相同时，参数的优先级问题，详见[参数优先级](priority.md)

## 本地任务引用全局参数

本地任务引用全局参数的前提是，你已经定义了[全局参数](global.md)，使用方式和[本地参数](local.md)中的使用方式类似，但是参数的值需要配置成全局参数中的key

![parameter-call-global-in-local](/img/global_parameter.png)

如上图中的`${biz_date}`以及`${curdate}`，就是本地参数引用全局参数的例子。观察上图的最后一行，local_param_bizdate通过\${global_bizdate}来引用全局参数，在shell脚本中可以通过\${local_param_bizdate}来引全局变量 global_bizdate的值，或通过JDBC直接将local_param_bizdate的值set进去。同理，local_param通过${local_param}引用上一节中定义的全局参数。​biz_date、biz_curdate、system.datetime都是用户自定义的参数，通过${全局参数}进行赋值。

## 上游任务传递给下游任务

DolphinScheduler 允许在任务间进行参数传递，目前传递方向仅支持上游单向传递给下游。目前支持这个特性的任务类型有：

* [Shell](../task/shell.md)
* [SQL](../task/sql.md)
* [Procedure](../task/stored-procedure.md)

当定义上游节点时，如果有需要将该节点的结果传递给有依赖关系的下游节点，需要在【当前节点设置】的【自定义参数】设置一个方向是 OUT 的变量。目前我们主要针对 SQL 和 SHELL 节点做了可以向下传递参数的功能。

### SQL

prop 为用户指定；方向选择为 OUT，只有当方向为 OUT 时才会被定义为变量输出；数据类型可以根据需要选择不同数据结构；value 部分不需要填写。

如果 SQL 节点的结果只有一行，一个或多个字段，prop 的名字需要和字段名称一致。数据类型可选择为除 LIST 以外的其他类型。变量会选择 SQL 查询结果中的列名中与该变量名称相同的列对应的值。

如果 SQL 节点的结果为多行，一个或多个字段，prop 的名字需要和字段名称一致。数据类型选择为LIST。获取到 SQL 查询结果后会将对应列转化为 LIST<VARCHAR>，并将该结果转化为 JSON 后作为对应变量的值。

我们再以上图中包含 SQL 节点的流程举例说明：

上图中节点【createParam1】的定义如下：

<img src="/img/globalParam/image-20210723104957031.png" alt="image-20210723104957031" style="zoom:50%;" />

节点【createParam2】的定义如下：

<img src="/img/globalParam/image-20210723105026924.png" alt="image-20210723105026924" style="zoom:50%;" />

您可以在【工作流实例】页面，找到对应的节点实例，便可以查看该变量的值。

节点实例【createParam1】如下：

<img src="/img/globalParam/image-20210723105131381.png" alt="image-20210723105131381" style="zoom:50%;" />

这里当然 "id" 的值会等于 12.

我们再来看节点实例【createParam2】的情况。

<img src="/img/globalParam/image-20210723105255850.png" alt="image-20210723105255850" style="zoom:50%;" />

这里只有 "id" 的值。尽管用户定义的 sql 查到的是 "id" 和 "database_name" 两个字段，但是由于只定义了一个为 out 的变量 "id"，所以只会设置一个变量。由于显示的原因，这里已经替您查好了该 list 的长度为 10。

### SHELL

prop 为用户指定；方向选择为 OUT，只有当方向为 OUT 时才会被定义为变量输出；数据类型可以根据需要选择不同数据结构；value 部分不需要填写。


用户需要传递参数，在定义 shell 脚本时，需要输出格式为 ${setValue(key=value)} 的语句，key 为对应参数的 prop，value 为该参数的值。


例如下图中：

<img src="/img/globalParam/image-20210723101242216.png" alt="image-20210723101242216" style="zoom:50%;" />

shell 节点定义时当日志检测到 ${setValue(key=value1)} 的格式时，会将 value1 赋值给 key，下游节点便可以直接使用变量 key 的值。同样，您可以在【工作流实例】页面，找到对应的节点实例，便可以查看该变量的值。

<img src="/img/globalParam/image-20210723102522383.png" alt="image-20210723102522383" style="zoom:50%;" />
