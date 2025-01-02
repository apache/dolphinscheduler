/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

DROP TABLE IF EXISTS `t_ds_workflow_task_lineage`;
CREATE TABLE `t_ds_workflow_task_lineage` (
     `id` int NOT NULL AUTO_INCREMENT,
     `workflow_definition_code` bigint NOT NULL DEFAULT 0,
     `workflow_definition_version` int NOT NULL DEFAULT 0,
     `task_definition_code` bigint NOT NULL DEFAULT 0,
     `task_definition_version` int NOT NULL DEFAULT 0,
     `dept_project_code` bigint NOT NULL DEFAULT 0 COMMENT 'dependent project code',
     `dept_workflow_definition_code` bigint NOT NULL DEFAULT 0 COMMENT 'dependent workflow definition code',
     `dept_task_definition_code` bigint NOT NULL DEFAULT 0 COMMENT 'dependent task definition code',
     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
     PRIMARY KEY (`id`),
     KEY `idx_workflow_code_version` (`workflow_definition_code`,`workflow_definition_version`),
     KEY `idx_task_code_version` (`task_definition_code`,`task_definition_version`),
     KEY `idx_dept_code` (`dept_project_code`,`dept_workflow_definition_code`,`dept_task_definition_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_data`;
CREATE TABLE `t_ds_jdbc_registry_data`
(
    `id`               bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',
    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',
    `data_type`        varchar(64)  NOT NULL COMMENT 'EPHEMERAL, PERSISTENT',
    `client_id`        bigint(11)   NOT NULL COMMENT 'client id',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last update time',
    PRIMARY KEY (`id`),
    unique Key `uk_t_ds_jdbc_registry_dataKey` (`data_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `t_ds_jdbc_registry_lock`;
CREATE TABLE `t_ds_jdbc_registry_lock`
(
    `id`          bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `lock_key`    varchar(256) NOT NULL COMMENT 'lock path',
    `lock_owner`  varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',
    `client_id`   bigint(11)   NOT NULL COMMENT 'client id',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`),
    unique Key `uk_t_ds_jdbc_registry_lockKey` (`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_client_heartbeat`;
CREATE TABLE `t_ds_jdbc_registry_client_heartbeat`
(
    `id`                  bigint(11)   NOT NULL COMMENT 'primary key',
    `client_name`         varchar(256) NOT NULL COMMENT 'client name, ip_processId',
    `last_heartbeat_time` bigint(11)   NOT NULL COMMENT 'last heartbeat timestamp',
    `connection_config`   text         NOT NULL COMMENT 'connection config',
    `create_time`         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_data_change_event`;
CREATE TABLE `t_ds_jdbc_registry_data_change_event`
(
    `id`                 bigint(11)  NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `event_type`         varchar(64) NOT NULL COMMENT 'ADD, UPDATE, DELETE',
    `jdbc_registry_data` text        NOT NULL COMMENT 'jdbc registry data',
    `create_time`        timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_listener_event`;

-- drop_column_t_ds_alert_plugin_instance behavior change
DROP PROCEDURE if EXISTS drop_column_t_ds_alert_plugin_instance;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_alert_plugin_instance()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alert_plugin_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='instance_type')
   THEN
ALTER TABLE `t_ds_alert_plugin_instance`
    DROP COLUMN `instance_type`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_alert_plugin_instance;
DROP PROCEDURE drop_column_t_ds_alert_plugin_instance;

-- drop_column_t_ds_alert_plugin_instance behavior change
DROP PROCEDURE if EXISTS drop_column_t_ds_alert_plugin_instance;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_alert_plugin_instance()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alert_plugin_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='warning_type')
   THEN
ALTER TABLE `t_ds_alert_plugin_instance`
    DROP COLUMN `warning_type`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_alert_plugin_instance;
DROP PROCEDURE drop_column_t_ds_alert_plugin_instance;

DROP TABLE IF EXISTS `t_ds_trigger_relation`;

-- Rename tables and fields from process to workflow
DROP PROCEDURE if EXISTS rename_tables_and_fields_from_process_to_workflow;
delimiter d//
CREATE PROCEDURE rename_tables_and_fields_from_process_to_workflow()
BEGIN

ALTER TABLE t_ds_alert change process_definition_code workflow_definition_code bigint(20);
ALTER TABLE t_ds_alert change process_instance_id workflow_instance_id int(11);

ALTER TABLE t_ds_command change process_definition_code workflow_definition_code bigint(20);
ALTER TABLE t_ds_command change process_instance_priority workflow_instance_priority int(11);
ALTER TABLE t_ds_command change process_instance_id workflow_instance_id int(11);
ALTER TABLE t_ds_command change process_definition_version workflow_definition_version int(11);

ALTER TABLE t_ds_error_command change process_definition_code workflow_definition_code bigint(20);
ALTER TABLE t_ds_error_command change process_instance_priority workflow_instance_priority int(11);
ALTER TABLE t_ds_error_command change process_instance_id workflow_instance_id int(11);
ALTER TABLE t_ds_error_command change process_definition_version workflow_definition_version int(11);

ALTER TABLE t_ds_process_task_relation change process_definition_version workflow_definition_version int(11);
ALTER TABLE t_ds_process_task_relation change process_definition_code workflow_definition_code bigint(20);

ALTER TABLE t_ds_process_task_relation_log change process_definition_version workflow_definition_version int(11);
ALTER TABLE t_ds_process_task_relation_log change process_definition_code workflow_definition_code bigint(20);

ALTER TABLE t_ds_process_instance change process_definition_code workflow_definition_code bigint(20);
ALTER TABLE t_ds_process_instance change process_definition_version workflow_definition_version int(11);
ALTER TABLE t_ds_process_instance change is_sub_process is_sub_workflow int(11);
ALTER TABLE t_ds_process_instance change process_instance_priority workflow_instance_priority int(11);
ALTER TABLE t_ds_process_instance change next_process_instance_id next_workflow_instance_id int(11);

ALTER TABLE t_ds_schedules change process_definition_code workflow_definition_code bigint(20);
ALTER TABLE t_ds_schedules change process_instance_priority workflow_instance_priority int(11);

ALTER TABLE t_ds_task_instance change process_instance_id workflow_instance_id int(11);
ALTER TABLE t_ds_task_instance change process_instance_name workflow_instance_name varchar(255);

ALTER TABLE t_ds_dq_execute_result change process_definition_id workflow_definition_id int(11);
ALTER TABLE t_ds_dq_execute_result change process_instance_id workflow_instance_id int(11);

ALTER TABLE t_ds_dq_task_statistics_value change process_definition_id workflow_definition_id int(11);

ALTER TABLE t_ds_task_group_queue change process_id workflow_instance_id int(11);

ALTER TABLE t_ds_relation_process_instance change parent_process_instance_id parent_workflow_instance_id int(11);
ALTER TABLE t_ds_relation_process_instance change process_instance_id workflow_instance_id int(11);

RENAME TABLE t_ds_process_definition TO t_ds_workflow_definition;
RENAME TABLE t_ds_process_definition_log TO t_ds_workflow_definition_log;
RENAME TABLE t_ds_process_task_relation TO t_ds_workflow_task_relation;
RENAME TABLE t_ds_process_task_relation_log TO t_ds_workflow_task_relation_log;
RENAME TABLE t_ds_process_instance TO t_ds_workflow_instance;
RENAME TABLE t_ds_relation_process_instance TO t_ds_relation_workflow_instance;

ALTER TABLE `t_ds_alert` MODIFY COLUMN `warning_type` tinyint NULL DEFAULT 2 COMMENT "1 workflow is successfully, 2 workflow/task is failed", MODIFY COLUMN `workflow_definition_code` bigint NULL COMMENT "workflow_definition_code", MODIFY COLUMN `workflow_instance_id` int NULL COMMENT "workflow_instance_id";
ALTER TABLE `t_ds_command` MODIFY COLUMN `command_type` tinyint NULL COMMENT "Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause workflow, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread", MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow definition code", MODIFY COLUMN `workflow_definition_version` int NULL DEFAULT 0 COMMENT "workflow definition version", MODIFY COLUMN `workflow_instance_id` int NULL DEFAULT 0 COMMENT "workflow instance id", MODIFY COLUMN `warning_type` tinyint NULL DEFAULT 0 COMMENT "Alarm type: 0 is not sent, 1 workflow is sent successfully, 2 workflow is sent failed, 3 workflow is sent successfully and all failures are sent", MODIFY COLUMN `workflow_instance_priority` int NULL DEFAULT 2 COMMENT "workflow instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest";
ALTER TABLE `t_ds_error_command` MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow definition code", MODIFY COLUMN `workflow_definition_version` int NULL DEFAULT 0 COMMENT "workflow definition version", MODIFY COLUMN `workflow_instance_id` int NULL DEFAULT 0 COMMENT "workflow instance id: 0", MODIFY COLUMN `workflow_instance_priority` int NULL DEFAULT 2 COMMENT "workflow instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest";
ALTER TABLE `t_ds_relation_workflow_instance` MODIFY COLUMN `parent_task_instance_id` int NULL COMMENT "parent workflow instance id", MODIFY COLUMN `parent_workflow_instance_id` int NULL COMMENT "parent workflow instance id", MODIFY COLUMN `workflow_instance_id` int NULL COMMENT "child workflow instance id", DROP INDEX `idx_parent_process_task`, ADD INDEX `idx_parent_workflow_task` (`parent_workflow_instance_id`, `parent_task_instance_id`), ADD INDEX `idx_workflow_instance_id` (`workflow_instance_id`);
ALTER TABLE `t_ds_schedules` MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow definition code", MODIFY COLUMN `warning_type` tinyint NOT NULL COMMENT "Alarm type: 0 is not sent, 1 workflow is sent successfully, 2 workflow is sent failed, 3 workflow is sent successfully and all failures are sent", MODIFY COLUMN `workflow_instance_priority` int NULL DEFAULT 2 COMMENT "workflow instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest";
ALTER TABLE `t_ds_task_group_queue` MODIFY COLUMN `workflow_instance_id` int NULL COMMENT "workflow instance id";
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `workflow_instance_id` int NULL COMMENT "workflow instance id", MODIFY COLUMN `workflow_instance_name` varchar(255) NULL COMMENT "workflow instance name", RENAME INDEX `process_instance_id` TO `workflow_instance_id`;
ALTER TABLE `t_ds_workflow_definition` MODIFY COLUMN `name` varchar(255) NULL COMMENT "workflow definition name", MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "workflow definition version", MODIFY COLUMN `release_state` tinyint NULL COMMENT "workflow definition release state：0:offline,1:online", MODIFY COLUMN `user_id` int NULL COMMENT "workflow definition creator id", RENAME INDEX `process_unique` TO `workflow_unique`;
ALTER TABLE `t_ds_workflow_definition_log` MODIFY COLUMN `name` varchar(255) NULL COMMENT "workflow definition name", MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "workflow definition version", MODIFY COLUMN `release_state` tinyint NULL COMMENT "workflow definition release state：0:offline,1:online", MODIFY COLUMN `user_id` int NULL COMMENT "workflow definition creator id";
ALTER TABLE `t_ds_workflow_instance` MODIFY COLUMN `name` varchar(255) NULL COMMENT "workflow instance name", MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow definition code", MODIFY COLUMN `workflow_definition_version` int NOT NULL DEFAULT 1 COMMENT "workflow definition version", MODIFY COLUMN `state` tinyint NULL COMMENT "workflow instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete", MODIFY COLUMN `recovery` tinyint NULL COMMENT "workflow instance failover flag：0:normal,1:failover instance", MODIFY COLUMN `start_time` datetime NULL COMMENT "workflow instance start time", MODIFY COLUMN `end_time` datetime NULL COMMENT "workflow instance end time", MODIFY COLUMN `run_times` int NULL COMMENT "workflow instance run times", MODIFY COLUMN `host` varchar(135) NULL COMMENT "workflow instance host", MODIFY COLUMN `failure_strategy` tinyint NULL DEFAULT 0 COMMENT "failure strategy. 0:end the workflow when node failed,1:continue running the other nodes when node failed", MODIFY COLUMN `warning_type` tinyint NULL DEFAULT 0 COMMENT "warning type. 0:no warning,1:warning if workflow success,2:warning if workflow failed,3:warning if success", MODIFY COLUMN `is_sub_workflow` int NULL DEFAULT 0 COMMENT "flag, whether the workflow is sub workflow", MODIFY COLUMN `history_cmd` text NULL COMMENT "history commands of workflow instance operation", MODIFY COLUMN `workflow_instance_priority` int NULL DEFAULT 2 COMMENT "workflow instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest", MODIFY COLUMN `next_workflow_instance_id` int NULL DEFAULT 0 COMMENT "serial queue next workflowInstanceId", MODIFY COLUMN `restart_time` datetime NULL COMMENT "workflow instance restart time", RENAME INDEX `process_instance_index` TO `workflow_instance_index`;
ALTER TABLE `t_ds_workflow_task_relation` MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow code", MODIFY COLUMN `workflow_definition_version` int NOT NULL COMMENT "workflow version";
ALTER TABLE `t_ds_workflow_task_relation_log` MODIFY COLUMN `workflow_definition_code` bigint NOT NULL COMMENT "workflow code", MODIFY COLUMN `workflow_definition_version` int NOT NULL COMMENT "workflow version", RENAME INDEX `idx_process_code_version` TO `idx_workflow_code_version`;
ALTER TABLE `t_ds_relation_workflow_instance` DROP INDEX `idx_process_instance_id`;


END;
d//
delimiter ;
CALL rename_tables_and_fields_from_process_to_workflow;
DROP PROCEDURE rename_tables_and_fields_from_process_to_workflow;

-- Drop data quality tables
DROP PROCEDURE if EXISTS drop_data_quality_tables;
delimiter d//
CREATE PROCEDURE drop_data_quality_tables()
BEGIN

DROP TABLE IF EXISTS t_ds_dq_comparison_type;
DROP TABLE IF EXISTS t_ds_dq_rule_execute_sql;
DROP TABLE IF EXISTS t_ds_dq_rule_input_entry;
DROP TABLE IF EXISTS t_ds_dq_task_statistics_value;
DROP TABLE IF EXISTS t_ds_dq_execute_result;
DROP TABLE IF EXISTS t_ds_dq_rule;
DROP TABLE IF EXISTS t_ds_relation_rule_input_entry;
DROP TABLE IF EXISTS t_ds_relation_rule_execute_sql;

END;
d//
delimiter ;
CALL drop_data_quality_tables;
DROP PROCEDURE drop_data_quality_tables;

ALTER TABLE `t_ds_workflow_definition` ADD KEY `idx_project_code` (`project_code`) USING BTREE;
ALTER TABLE `t_ds_workflow_definition_log` ADD KEY `idx_project_code` (`project_code`) USING BTREE;

-- drop_column_t_ds_worker_group other_params_json
DROP PROCEDURE if EXISTS drop_column_t_ds_worker_group_other_params_json;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_worker_group_other_params_json()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_worker_group'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='other_params_json')
   THEN
ALTER TABLE `t_ds_worker_group`
    DROP COLUMN `other_params_json`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_worker_group_other_params_json;
DROP PROCEDURE drop_column_t_ds_worker_group_other_params_json;

ALTER TABLE `t_ds_task_definition` ADD INDEX `idx_project_code` USING BTREE (`project_code`);


-- drop_column_t_ds_task_definition is_cache
DROP PROCEDURE if EXISTS drop_column_t_ds_task_definition_is_cache;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_definition_is_cache()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE `t_ds_task_definition`
DROP COLUMN `is_cache`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_definition_is_cache;
DROP PROCEDURE drop_column_t_ds_task_definition_is_cache;

-- drop_column_t_ds_task_definition cache_key
DROP PROCEDURE if EXISTS drop_column_t_ds_task_definition_cache_key;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_definition_cache_key()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='cache_key')
   THEN
ALTER TABLE `t_ds_task_definition`
DROP COLUMN `cache_key`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_definition_cache_key;
DROP PROCEDURE drop_column_t_ds_task_definition_cache_key;

-- drop_column_t_ds_task_definition_log is_cache
DROP PROCEDURE if EXISTS drop_column_t_ds_task_definition_log_is_cache;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_definition_log_is_cache()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition_log'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE `t_ds_task_definition_log`
DROP COLUMN `is_cache`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_definition_log_is_cache;
DROP PROCEDURE drop_column_t_ds_task_definition_log_is_cache;

-- drop_column_t_ds_task_definition_log cache_key
DROP PROCEDURE if EXISTS drop_column_t_ds_task_definition_log_cache_key;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_definition_log_cache_key()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition_log'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='cache_key')
   THEN
ALTER TABLE `t_ds_task_definition_log`
DROP COLUMN `cache_key`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_definition_log_cache_key;
DROP PROCEDURE drop_column_t_ds_task_definition_log_cache_key;

-- drop_column_t_ds_task_instance is_cache
DROP PROCEDURE if EXISTS drop_column_t_ds_task_instance_is_cache;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_instance_is_cache()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE `t_ds_task_instance`
DROP COLUMN `is_cache`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_instance_is_cache;
DROP PROCEDURE drop_column_t_ds_task_instance_is_cache;

-- drop_column_t_ds_task_instance cache_key
DROP PROCEDURE if EXISTS drop_column_t_ds_task_instance_cache_key;
delimiter d//
CREATE PROCEDURE drop_column_t_ds_task_instance_cache_key()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='cache_key')
   THEN
ALTER TABLE `t_ds_task_instance`
DROP COLUMN `cache_key`;
END IF;
END;
d//
delimiter ;
CALL drop_column_t_ds_task_instance_cache_key;
DROP PROCEDURE drop_column_t_ds_task_instance_cache_key;

DROP TABLE IF EXISTS `t_ds_task_instance_context`;
CREATE TABLE `t_ds_task_instance_context` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_instance_id` int(11) NOT NULL,
  `context` text NOT NULL,
  `context_type` varchar(255) NOT NULL COMMENT 'context type',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_instance_id` (`task_instance_id`,`context_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;
