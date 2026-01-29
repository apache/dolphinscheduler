# Monitor

## Service Management

- Service management is mainly to monitor and display the health status and basic information of each service in the system.

### Master Server

- Mainly related to master information.

![master](../../../img/new_ui/dev/monitor/master.png)

### Worker Server

- Mainly related to worker information.

![worker](../../../img/new_ui/dev/monitor/worker.png)

### Alert Server

- Mainly related to alert server information.

![alert-server](../../../img/new_ui/dev/monitor/alert-server.png)

### Database

- Mainly the health status of the DB.

![db](../../../img/new_ui/dev/monitor/db.png)

## Statistics Management

### Statistics

![Command Statistics List](../../../img/new_ui/dev/monitor/command-list.png)

Shows the command list in the system. Data is from the `t_ds_command` table.

![Failure Command Statistics List](../../../img/new_ui/dev/monitor/failure-command-list.png)

Shows the failure command list in the system. Data is from the `t_ds_error_command` table.

### Audit Log

The audit log provides information about who accesses the system and the operations made to the system and record related
time, which strengthen the security of the system and maintenance.

![audit-log](../../../img/new_ui/dev/monitor/audit-log.png)
