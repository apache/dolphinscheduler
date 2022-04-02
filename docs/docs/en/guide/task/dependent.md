# Dependent Node

- Dependent nodes are **dependency check nodes**. For example, process A depends on the successful execution of process B from yesterday, and the dependent node will check whether process B run successful yesterday.

> Drag from the toolbar ![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_DEPENDENT.png) task node to the canvas, as shown in the figure below:

<p align="center">
   <img src="/img/dependent-nodes-en.png" width="80%" />
 </p>

> The dependent node provides a logical judgment function, such as checking whether the B process was successful yesterday, or whether the C process was executed successfully.

  <p align="center">
   <img src="/img/depend-node-en.png" width="80%" />
 </p>

> For example, process A is a weekly report task, processes B and C are daily tasks, and task A requires tasks B and C to be successfully executed every day of the last week, as shown in the figure:

 <p align="center">
   <img src="/img/depend-node1-en.png" width="80%" />
 </p>

> If the weekly report A also needs to be executed successfully last Tuesday:

 <p align="center">
   <img src="/img/depend-node3-en.png" width="80%" />
 </p>