# 全局参数开发文档

用户在定义方向为 OUT 的参数后，会保存在 task 的 localParam 中。

## 参数的使用

从 DAG 中获取当前需要创建的 taskInstance 的直接前置节点 preTasks，获取 preTasks 的 varPool，将该 `varPool(List<Property>)`合并为一个 varPool，在合并过程中，如果发现有相同的变量名的变量，按照以下逻辑处理

* 若所有的值都是 null，则合并后的值为 null
* 若有且只有一个值为非 null，则合并后的值为该非 null 值
* 若所有的值都不是 null，则根据取 varPool 的 taskInstance 的 endtime 最早的一个

在合并过程中将所有的合并过来的 Property 的方向更新为 IN

合并后的结果保存在 taskInstance.varPool 中。

Worker 收到后将 varPool 解析为 Map<String,Property> 的格式，其中 map 的 key 为 property.prop 也就是变量名。

在 processor 处理参数时，会将 varPool 和 localParam 和 globalParam 三个变量池参数合并，合并过程中若有参数名重复的参数，按照以下优先级进行替换，高优先级保留，低优先级被替换：

* `globalParam` ：高
* `varPool` ：中
* `localParam` ：低

参数会在节点内容执行之前利用正则表达式比配到 ${变量名}，替换为对应的值。

## 参数的设置

目前仅支持 SQL 和 SHELL 节点的参数获取。
从 localParam 中获取方向为 OUT 的参数，根据不同节点的类型做以下方式处理。

### SQL 节点

参数返回的结构为 List<Map<String,String>>

其中，List 的元素为每行数据，Map 的 key 为列名，value 为该列对应的值

* 若 SQL 语句返回为有一行数据，则根据用户在定义 task 时定义的 OUT 参数名匹配列名，若没有匹配到则放弃。
* 若 SQL 语句返回多行，按照根据用户在定义 task 时定义的类型为 LIST 的 OUT 参数名匹配列名，将对应列的所有行数据转换为 `List<String>`，作为该参数的值。若没有匹配到则放弃。

### SHELL 节点

processor 执行后的结果返回为 `Map<String,String>`

用户在定义 shell 脚本时需要在输出中定义 `${setValue(key=value)}`

在参数处理时去掉 ${setValue()}，按照 “=” 进行拆分，第 0 个为 key，第 1 个为 value。

同样匹配用户定义 task 时定义的 OUT 参数名与 key，将 value 作为该参数的值。

返回参数处理

* 获取到的 processor 的结果为 String
* 判断 processor 是否为空，为空退出
* 判断 localParam 是否为空，为空退出
* 获取 localParam 中为 OUT 的参数，为空退出
* 将String按照上诉格式格式化（SQL为List<Map<String,String>>，shell为Map<String,String>）
* 将匹配好值的参数赋值给 varPool（List<Property>，其中包含原有 IN 的参数）

varPool 格式化为 json，传递给 master。
Master 接收到 varPool 后，将其中为 OUT 的参数回写到 localParam 中。
