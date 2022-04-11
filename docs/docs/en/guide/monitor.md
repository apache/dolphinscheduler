# Monitor

## Service Management

- Service management is mainly to monitor and display the health status and basic information of each service in the system.

### Master Server

- Mainly related to master information.

![master](/img/new_ui/dev/monitor/master.png)

### Worker Server

- Mainly related to worker information.

![worker](/img/new_ui/dev/monitor/worker.png)

### Database

- Mainly the health status of the DB.

![db](/img/new_ui/dev/monitor/db.png)

## Statistics Management

### Statistics

![statistics](/img/new_ui/dev/monitor/statistics.png)

- Number of commands wait to be executed: statistics of the `t_ds_command` table data.
- The number of failed commands: statistics of the `t_ds_error_command` table data.
- Number of tasks wait to run: count the data of `task_queue` in the ZooKeeper.
- Number of tasks wait to be killed: count the data of `task_kill` in the ZooKeeper.

### Audit Log

审计日志的记录提供了有关谁访问了系统，以及他或她在给定时间段内执行了哪些操作的信息，他对于维护安全都很有用。

![audit-log](/img/new_ui/dev/monitor/audit-log.jpg)
