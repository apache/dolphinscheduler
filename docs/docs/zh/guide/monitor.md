# 监控中心

## 服务管理

- 服务管理主要是对系统中的各个服务的健康状况和基本信息的监控和显示

### Master

- 主要是 master 的相关信息。

![master](../../../img/new_ui/dev/monitor/master.png)

### Worker

- 主要是 worker 的相关信息。

![worker](../../../img/new_ui/dev/monitor/worker.png)

### Alert Server

- 主要是 alert server 的相关信息。

![alert-server](../../../img/new_ui/dev/monitor/alert-server.png)

### Database

- 主要是 DB 的健康状况

![db](../../../img/new_ui/dev/monitor/db.png)

## 统计管理

### Statistics

![Command Statistics List](../../../img/new_ui/dev/monitor/command-list.png)

展示系统中的命令列表，数据来自`t_ds_command`表。

![Failure Command Statistics List](../../../img/new_ui/dev/monitor/failure-command-list.png)

展示系统中的失败命令列表，数据来自`t_ds_error_command`表。

### 审计日志

审计日志的记录提供了有关谁访问了系统，以及他或她在给定时间段内执行了哪些操作的信息，对于维护安全都很有用。

![audit-log](../../../img/new_ui/dev/monitor/audit-log.png)
