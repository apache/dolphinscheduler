# Dolphin Scheduler 1.3元数据文档

<a name="25Ald"></a>
### 表概览
| 表名 | 表信息 |
| :---: | :---: |
| t_ds_access_token | 访问ds后端的token |
| t_ds_alert | 告警信息 |
| t_ds_alertgroup | 告警组 |
| t_ds_command | 执行命令 |
| t_ds_datasource | 数据源 |
| t_ds_error_command | 错误命令 |
| t_ds_process_definition | 流程定义 |
| t_ds_process_instance | 流程实例 |
| t_ds_project | 项目 |
| t_ds_queue | 队列 |
| t_ds_relation_datasource_user | 用户关联数据源 |
| t_ds_relation_process_instance | 子流程 |
| t_ds_relation_project_user | 用户关联项目 |
| t_ds_relation_resources_user | 用户关联资源 |
| t_ds_relation_udfs_user | 用户关联UDF函数 |
| t_ds_relation_user_alertgroup | 用户关联告警组 |
| t_ds_resources | 资源文件 |
| t_ds_schedules | 流程定时调度 |
| t_ds_session | 用户登录的session |
| t_ds_task_instance | 任务实例 |
| t_ds_tenant | 租户 |
| t_ds_udfs | UDF资源 |
| t_ds_user | 用户 |
| t_ds_version | ds版本信息 |

<a name="VNVGr"></a>
### 用户	队列	数据源
![image.png](/img/metadata-erd/user-queue-datasource.png)

- 一个租户下可以有多个用户<br />
- t_ds_user中的queue字段存储的是队列表中的queue_name信息，t_ds_tenant下存的是queue_id，在流程定义执行过程中，用户队列优先级最高，用户队列为空则采用租户队列<br />
- t_ds_datasource表中的user_id字段表示创建该数据源的用户，t_ds_relation_datasource_user中的user_id表示，对数据源有权限的用户<br />
<a name="HHyGV"></a>
### 项目	资源	告警
![image.png](/img/metadata-erd/project-resource-alert.png)

- 一个用户可以有多个项目，用户项目授权通过t_ds_relation_project_user表完成project_id和user_id的关系绑定<br />
- t_ds_projcet表中的user_id表示创建该项目的用户，t_ds_relation_project_user表中的user_id表示对项目有权限的用户<br />
- t_ds_resources表中的user_id表示创建该资源的用户，t_ds_relation_resources_user中的user_id表示对资源有权限的用户<br />
- t_ds_udfs表中的user_id表示创建该UDF的用户，t_ds_relation_udfs_user表中的user_id表示对UDF有权限的用户<br />
<a name="Bg2Sn"></a>
### 命令	流程	任务
![image.png](/img/metadata-erd/command.png)<br />![image.png](/img/metadata-erd/process-task.png)

- 一个项目有多个流程定义，一个流程定义可以生成多个流程实例，一个流程实例可以生成多个任务实例<br />
- t_ds_schedulers表存放流程定义的定时调度信息<br />
- t_ds_relation_process_instance表存放的数据用于处理流程定义中含有子流程的情况，parent_process_instance_id表示含有子流程的主流程实例id，process_instance_id表示子流程实例的id，parent_task_instance_id表示子流程节点的任务实例id，流程实例表和任务实例表分别对应t_ds_process_instance表和t_ds_task_instance表
<a name="Pv25P"></a>
### 核心表Schema
<a name="32Jzd"></a>
#### t_ds_process_definition
| 字段 | 类型 | 注释 |
| --- | --- | --- |
| id | int | 主键 |
| name | varchar | 流程定义名称 |
| version | int | 流程定义版本 |
| release_state | tinyint | 流程定义的发布状态：0 未上线  1已上线 |
| project_id | int | 项目id |
| user_id | int | 流程定义所属用户id |
| process_definition_json | longtext | 流程定义json串 |
| description | text | 流程定义描述 |
| global_params | text | 全局参数 |
| flag | tinyint | 流程是否可用：0 不可用，1 可用 |
| locations | text | 节点坐标信息 |
| connects | text | 节点连线信息 |
| receivers | text | 收件人 |
| receivers_cc | text | 抄送人 |
| create_time | datetime | 创建时间 |
| timeout | int | 超时时间 |
| tenant_id | int | 租户id |
| update_time | datetime | 更新时间 |
| modify_by | varchar | 修改用户 |
| resource_ids | varchar | 资源id集 |

<a name="e6jfz"></a>
#### t_ds_process_instance
| 字段 | 类型 | 注释 |
| --- | --- | --- |
| id | int | 主键 |
| name | varchar | 流程实例名称 |
| process_definition_id | int | 流程定义id |
| state | tinyint | 流程实例状态：0 提交成功,1 正在运行,2 准备暂停,3 暂停,4 准备停止,5 停止,6 失败,7 成功,8 需要容错,9 kill,10 等待线程,11 等待依赖完成 |
| recovery | tinyint | 流程实例容错标识：0 正常,1 需要被容错重启 |
| start_time | datetime | 流程实例开始时间 |
| end_time | datetime | 流程实例结束时间 |
| run_times | int | 流程实例运行次数 |
| host | varchar | 流程实例所在的机器 |
| command_type | tinyint | 命令类型：0 启动工作流,1 从当前节点开始执行,2 恢复被容错的工作流,3 恢复暂停流程,4 从失败节点开始执行,5 补数,6 调度,7 重跑,8 暂停,9 停止,10 恢复等待线程 |
| command_param | text | 命令的参数（json格式） |
| task_depend_type | tinyint | 节点依赖类型：0 当前节点,1 向前执行,2 向后执行 |
| max_try_times | tinyint | 最大重试次数 |
| failure_strategy | tinyint | 失败策略 0 失败后结束，1 失败后继续 |
| warning_type | tinyint | 告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发 |
| warning_group_id | int | 告警组id |
| schedule_time | datetime | 预期运行时间 |
| command_start_time | datetime | 开始命令时间 |
| global_params | text | 全局参数（固化流程定义的参数） |
| process_instance_json | longtext | 流程实例json(copy的流程定义的json) |
| flag | tinyint | 是否可用，1 可用，0不可用 |
| update_time | timestamp | 更新时间 |
| is_sub_process | int | 是否是子工作流 1 是，0 不是 |
| executor_id | int | 命令执行用户 |
| locations | text | 节点坐标信息 |
| connects | text | 节点连线信息 |
| history_cmd | text | 历史命令，记录所有对流程实例的操作 |
| dependence_schedule_times | text | 依赖节点的预估时间 |
| process_instance_priority | int | 流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest |
| worker_group | varchar | 任务指定运行的worker分组 |
| timeout | int | 超时时间 |
| tenant_id | int | 租户id |

<a name="IvHEc"></a>
#### t_ds_task_instance
| 字段 | 类型 | 注释 |
| --- | --- | --- |
| id | int | 主键 |
| name | varchar | 任务名称 |
| task_type | varchar | 任务类型 |
| process_definition_id | int | 流程定义id |
| process_instance_id | int | 流程实例id |
| task_json | longtext | 任务节点json |
| state | tinyint | 任务实例状态：0 提交成功,1 正在运行,2 准备暂停,3 暂停,4 准备停止,5 停止,6 失败,7 成功,8 需要容错,9 kill,10 等待线程,11 等待依赖完成 |
| submit_time | datetime | 任务提交时间 |
| start_time | datetime | 任务开始时间 |
| end_time | datetime | 任务结束时间 |
| host | varchar | 执行任务的机器 |
| execute_path | varchar | 任务执行路径 |
| log_path | varchar | 任务日志路径 |
| alert_flag | tinyint | 是否告警 |
| retry_times | int | 重试次数 |
| pid | int | 进程pid |
| app_link | varchar | yarn app id |
| flag | tinyint | 是否可用：0 不可用，1 可用 |
| retry_interval | int | 重试间隔 |
| max_retry_times | int | 最大重试次数 |
| task_instance_priority | int | 任务实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest |
| worker_group | varchar | 任务指定运行的worker分组 |

<a name="pPQkU"></a>
#### t_ds_schedules
| 字段 | 类型 | 注释 |
| --- | --- | --- |
| id | int | 主键 |
| process_definition_id | int | 流程定义id |
| start_time | datetime | 调度开始时间 |
| end_time | datetime | 调度结束时间 |
| crontab | varchar | crontab 表达式 |
| failure_strategy | tinyint | 失败策略： 0 结束，1 继续 |
| user_id | int | 用户id |
| release_state | tinyint | 状态：0 未上线，1 上线 |
| warning_type | tinyint | 告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发 |
| warning_group_id | int | 告警组id |
| process_instance_priority | int | 流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest |
| worker_group | varchar | 任务指定运行的worker分组 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

<a name="TkQzn"></a>
#### t_ds_command
| 字段 | 类型 | 注释 |
| --- | --- | --- |
| id | int | 主键 |
| command_type | tinyint | 命令类型：0 启动工作流,1 从当前节点开始执行,2 恢复被容错的工作流,3 恢复暂停流程,4 从失败节点开始执行,5 补数,6 调度,7 重跑,8 暂停,9 停止,10 恢复等待线程 |
| process_definition_id | int | 流程定义id |
| command_param | text | 命令的参数（json格式） |
| task_depend_type | tinyint | 节点依赖类型：0 当前节点,1 向前执行,2 向后执行 |
| failure_strategy | tinyint | 失败策略：0结束，1继续 |
| warning_type | tinyint | 告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发 |
| warning_group_id | int | 告警组 |
| schedule_time | datetime | 预期运行时间 |
| start_time | datetime | 开始时间 |
| executor_id | int | 执行用户id |
| dependence | varchar | 依赖字段 |
| update_time | datetime | 更新时间 |
| process_instance_priority | int | 流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest |
| worker_group | varchar | 任务指定运行的worker分组 |



