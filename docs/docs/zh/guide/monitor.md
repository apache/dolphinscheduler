# 监控中心

## 服务管理

- 服务管理主要是对系统中的各个服务的健康状况和基本信息的监控和显示

### Master 监控

- 主要是 master 的相关信息。

![master](/img/new_ui/dev/monitor/master.png)

### Worker 监控

- 主要是 worker 的相关信息。

![worker](/img/new_ui/dev/monitor/worker.png)

### DB 监控

- 主要是 DB 的健康状况

![db](/img/new_ui/dev/monitor/db.png)
 
## 统计管理

![statistics](/img/new_ui/dev/monitor/statistics.png)
 
- 待执行命令数：统计 t_ds_command 表的数据
- 执行失败的命令数：统计 t_ds_error_command 表的数据
- 待运行任务数：统计 Zookeeper 中 task_queue 的数据
- 待杀死任务数：统计 Zookeeper 中 task_kill 的数据
