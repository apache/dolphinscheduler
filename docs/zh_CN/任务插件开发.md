## 任务插件开发

提醒:目前任务插件开发暂不支持热部署

### 基于SHELL的任务

#### 基于YARN的计算（参见MapReduceTask）

- 需要在 **cn.escheduler.server.worker.task** 下的 **TaskManager** 类中创建自定义任务(也需在TaskType注册对应的任务类型)
- 需要继承**cn.escheduler.server.worker.task** 下的 **AbstractYarnTask**
- 构造方法调度 **AbstractYarnTask** 构造方法
- 继承 **AbstractParameters** 自定义任务参数实体
- 重写 **AbstractTask** 的 **init** 方法中解析**自定义任务参数**
- 重写 **buildCommand** 封装command



#### 基于非YARN的计算（参见ShellTask）
- 需要在 **cn.escheduler.server.worker.task** 下的 **TaskManager** 中创建自定义任务

- 需要继承**cn.escheduler.server.worker.task** 下的 **AbstractTask**

- 构造方法中实例化 **ShellCommandExecutor**

  ```
  public ShellTask(TaskProps props, Logger logger) {
    super(props, logger);
  
    this.taskDir = props.getTaskDir();
  
    this.processTask = new ShellCommandExecutor(this::logHandle,
        props.getTaskDir(), props.getTaskAppId(),
        props.getTenantCode(), props.getEnvFile(), props.getTaskStartTime(),
        props.getTaskTimeout(), logger);
    this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
  }
  ```

  传入自定义任务的 **TaskProps**和自定义**Logger**，TaskProps 封装了任务的信息，Logger分装了自定义日志信息

- 继承 **AbstractParameters** 自定义任务参数实体

- 重写 **AbstractTask** 的 **init** 方法中解析**自定义任务参数实体**

- 重写 **handle** 方法，调用 **ShellCommandExecutor** 的 **run** 方法，第一个参数传入自己的**command**，第二个参数传入 ProcessDao，设置相应的 **exitStatusCode**

### 基于非SHELL的任务（参见SqlTask）

- 需要在 **cn.escheduler.server.worker.task** 下的 **TaskManager** 中创建自定义任务
- 需要继承**cn.escheduler.server.worker.task** 下的 **AbstractTask**
- 继承 **AbstractParameters** 自定义任务参数实体
- 构造方法或者重写 **AbstractTask** 的 **init** 方法中，解析自定义任务参数实体
- 重写 **handle** 方法实现业务逻辑并设置相应的**exitStatusCode**

