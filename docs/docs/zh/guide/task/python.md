# Python节点

- 使用python节点，可以直接执行python脚本，对于python节点，worker会使用`python **`方式提交任务。

> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PYTHON.png)任务节点到画板中，如下图所示：

<p align="center">
   <img src="/img/python_edit.png" width="80%" />
 </p>

- 脚本：用户开发的Python程序
- 环境名称：执行Python程序的解释器路径，指定运行脚本的解释器。当你需要使用 Python **虚拟环境** 时，可以通过创建不同的环境名称来实现。 
- 资源：是指脚本中需要调用的资源文件列表
- 自定义参数：是Python局部的用户自定义参数，会替换脚本中以${变量}的内容
- 注意：若引入资源目录树下的python文件，需添加 `__init__.py` 文件
