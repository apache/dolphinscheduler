# 全局参数

## 作用域

在工作流定义页面配置的参数，作用于该工作流中全部的任务

## 使用方式

全局参数配置方式如下：在工作流定义页面，点击“设置全局”右边的加号，填写对应的变量名称和对应的值，保存即可

<p align="center">
   <img src="/img/supplement_global_parameter.png" width="80%" />
</p>

<p align="center">
   <img src="/img/local_parameter.png" width="80%" />
</p>

这里定义的global_bizdate参数可以被其它任一节点的局部参数引用，并设置global_bizdate的value为通过引用系统参数system.biz.date获得的值
