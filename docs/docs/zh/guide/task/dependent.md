# 依赖节点

- 依赖节点，就是**依赖检查节点**。比如A流程依赖昨天的B流程执行成功，依赖节点会去检查B流程在昨天是否有执行成功的实例。

> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_DEPENDENT.png)任务节点到画板中，如下图所示：

<p align="center">
   <img src="/img/dependent_edit.png" width="80%" />
 </p>

  > 依赖节点提供了逻辑判断功能，比如检查昨天的B流程是否成功，或者C流程是否执行成功。

  <p align="center">
   <img src="/img/depend-node.png" width="80%" />
 </p>

  > 例如，A流程为周报任务，B、C流程为天任务，A任务需要B、C任务在上周的每一天都执行成功，如图示：

 <p align="center">
   <img src="/img/depend-node2.png" width="80%" />
 </p>

  > 假如，周报A同时还需要自身在上周二执行成功：

 <p align="center">
   <img src="/img/depend-node3.png" width="80%" />
 </p>