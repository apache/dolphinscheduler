# MetaData

## DolphinScheduler DB Table Overview

| Table Name | Comment |
| :---: | :---: |
| t_ds_access_token | token for access DolphinScheduler backend |
| t_ds_alert | alert detail |
| t_ds_alertgroup | alert group |
| t_ds_command | command detail |
| t_ds_datasource | data source |
| t_ds_error_command | error command detail |
| t_ds_process_definition | process definition |
| t_ds_process_instance | process instance |
| t_ds_project | project |
| t_ds_queue | queue |
| t_ds_relation_datasource_user | datasource related to user |
| t_ds_relation_process_instance | sub process |
| t_ds_relation_project_user | project related to user |
| t_ds_relation_resources_user | resource related to user |
| t_ds_relation_udfs_user | UDF functions related to user |
| t_ds_relation_user_alertgroup | alert group related to user |
| t_ds_resources | resoruce center file |
| t_ds_schedules | process definition schedule |
| t_ds_session | user login session |
| t_ds_task_instance | task instance |
| t_ds_tenant | tenant |
| t_ds_udfs | UDF resource |
| t_ds_user | user detail |
| t_ds_version | DolphinScheduler version |


---

## E-R Diagram

### User Queue DataSource

![image.png](/img/metadata-erd/user-queue-datasource.png)

- One tenant can own Multiple users.
- The queue field in the t_ds_user table stores the queue_name information in the t_ds_queue table, t_ds_tenant stores queue information using queue_id column. During the execution of the process definition, the user queue has the highest priority. If the user queue is null, use the tenant queue.
- The user_id field in the t_ds_datasource table shows the user who create the data source. The user_id in t_ds_relation_datasource_user shows the user who has permission to the data source.
  
### Project Resource Alert

![image.png](/img/metadata-erd/project-resource-alert.png)

- User can have multiple projects, user project authorization completes the relationship binding using project_id and user_id in t_ds_relation_project_user table.
- The user_id in the t_ds_projcet table represents the user who create the project, and the user_id in the t_ds_relation_project_user table represents users who have permission to the project.
- The user_id in the t_ds_resources table represents the user who create the resource, and the user_id in t_ds_relation_resources_user represents the user who has permissions to the resource.
- The user_id in the t_ds_udfs table represents the user who create the UDF, and the user_id in the t_ds_relation_udfs_user table represents a user who has permission to the UDF.
  
### Command Process Task

![image.png](/img/metadata-erd/command.png)<br />![image.png](/img/metadata-erd/process-task.png)

- A project has multiple process definitions, a process definition can generate multiple process instances, and a process instance can generate multiple task instances.
- The t_ds_schedulers table stores the specified time schedule information for process definition.
- The data stored in the t_ds_relation_process_instance table is used to deal with the sub-processes of a process definition, parent_process_instance_id field represents the id of the main process instance who contains child processes, process_instance_id field represents the id of the sub-process instance, parent_task_instance_id field represents the task instance id of the sub-process node.
- The process instance table and the task instance table correspond to the t_ds_process_instance table and the t_ds_task_instance table, respectively.

---

## Core Table Schema

### t_ds_process_definition

| Field | Type | Comment |
| --- | --- | --- |
| id | int | primary key |
| name | varchar | process definition name |
| version | int | process definition version |
| release_state | tinyint | process definition release state：0:offline,1:online |
| project_id | int | project id |
| user_id | int | process definition creator id |
| process_definition_json | longtext | process definition JSON content |
| description | text | process definition description |
| global_params | text | global parameters |
| flag | tinyint | whether process available: 0 not available, 1 available |
| locations | text | Node location information |
| connects | text | Node connection information |
| receivers | text | receivers |
| receivers_cc | text | carbon copy list |
| create_time | datetime | create time |
| timeout | int | timeout |
| tenant_id | int | tenant id |
| update_time | datetime | update time |
| modify_by | varchar | define user modify the process |
| resource_ids | varchar | resource id set |

### t_ds_process_instance

| Field | Type | Comment |
| --- | --- | --- |
| id | int | primary key |
| name | varchar | process instance name |
| process_definition_id | int | process definition id |
| state | tinyint | process instance Status: 0 successful commit, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete |
| recovery | tinyint | process instance failover flag：0: normal,1: failover instance needs restart |
| start_time | datetime | process instance start time |
| end_time | datetime | process instance end time |
| run_times | int | process instance run times |
| host | varchar | process instance host |
| command_type | tinyint | command type：0 start ,1 start from the current node,2 resume a fault-tolerant process,3 resume from pause process, 4 execute from the failed node,5 complement, 6 dispatch, 7 re-run, 8 pause, 9 stop, 10 resume waiting thread |
| command_param | text | JSON command parameters |
| task_depend_type | tinyint | node dependency type: 0 current node, 1 forward, 2 backward |
| max_try_times | tinyint | max try times |
| failure_strategy | tinyint | failure strategy, 0: end the process when node failed,1: continue run the other nodes when failed |
| warning_type | tinyint | warning type 0: no warning, 1: warning if process success, 2: warning if process failed, 3: warning whatever results |
| warning_group_id | int | warning group id |
| schedule_time | datetime | schedule time |
| command_start_time | datetime | command start time |
| global_params | text | global parameters |
| process_instance_json | longtext | process instance JSON |
| flag | tinyint | whether process instance is available: 0 not available, 1 available |
| update_time | timestamp | update time |
| is_sub_process | int | whether the process is sub process: 1 sub-process, 0 not sub-process |
| executor_id | int | executor id |
| locations | text | node location information |
| connects | text | node connection information |
| history_cmd | text | history commands, record all the commands to a instance |
| dependence_schedule_times | text | depend schedule estimate time |
| process_instance_priority | int | process instance priority. 0 highest,1 high,2 medium,3 low,4 lowest |
| worker_group | varchar | worker group who assign the task |
| timeout | int | timeout |
| tenant_id | int | tenant id |

### t_ds_task_instance

| Field | Type | Comment |
| --- | --- | --- |
| id | int | primary key |
| name | varchar | task name |
| task_type | varchar | task type |
| process_definition_id | int | process definition id |
| process_instance_id | int | process instance id |
| task_json | longtext | task content JSON |
| state | tinyint | Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete |
| submit_time | datetime | task submit time |
| start_time | datetime | task start time |
| end_time | datetime | task end time |
| host | varchar | host of task running on |
| execute_path | varchar | task execute path in the host |
| log_path | varchar | task log path |
| alert_flag | tinyint | whether alert |
| retry_times | int | task retry times |
| pid | int | pid of task |
| app_link | varchar | Yarn app id |
| flag | tinyint | task instance is available : 0 not available, 1 available |
| retry_interval | int | retry interval when task failed |
| max_retry_times | int | max retry times |
| task_instance_priority | int | task instance priority:0 highest,1 high,2 medium,3 low,4 lowest |
| worker_group | varchar | worker group who assign the task |

#### t_ds_schedules

| Field | Type | Comment |
| --- | --- | --- |
| id | int | primary key |
| process_definition_id | int | process definition id |
| start_time | datetime | schedule start time |
| end_time | datetime | schedule end time |
| crontab | varchar | crontab expression |
| failure_strategy | tinyint | failure strategy: 0 end,1 continue |
| user_id | int | user id |
| release_state | tinyint | release status: 0 not yet released,1 released |
| warning_type | tinyint | warning type: 0: no warning, 1: warning if process success, 2: warning if process failed, 3: warning whatever results |
| warning_group_id | int | warning group id |
| process_instance_priority | int | process instance priority:0 highest,1 high,2 medium,3 low,4 lowest |
| worker_group | varchar | worker group who assign the task |
| create_time | datetime | create time |
| update_time | datetime | update time |

### t_ds_command

| Field | Type | Comment |
| --- | --- | --- |
| id | int | primary key |
| command_type | tinyint | command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 re-run, 8 pause, 9 stop, 10 resume waiting thread |
| process_definition_id | int | process definition id |
| command_param | text | JSON command parameters |
| task_depend_type | tinyint | node dependency type: 0 current node, 1 forward, 2 backward |
| failure_strategy | tinyint | failed policy: 0 end, 1 continue |
| warning_type | tinyint | alarm type: 0 no alarm, 1 alarm if process success, 2: alarm if process failed, 3: warning whatever results |
| warning_group_id | int | warning group id |
| schedule_time | datetime | schedule time |
| start_time | datetime | start time |
| executor_id | int | executor id |
| dependence | varchar | dependence column |
| update_time | datetime | update time |
| process_instance_priority | int | process instance priority: 0 highest,1 high,2 medium,3 low,4 lowest |
| worker_group_id | int |  worker group who assign the task |