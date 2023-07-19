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
SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));

-- uc_dolphin_T_t_ds_command_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_command_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_command_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_command ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_command_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_command_R_test_flag;

-- uc_dolphin_T_t_ds_error_command_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_error_command_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_error_command_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_error_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_error_command ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_error_command_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_error_command_R_test_flag;


-- uc_dolphin_T_t_ds_process_instance_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_instance_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_instance_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_process_instance ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_instance_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_process_instance_R_test_flag;

-- uc_dolphin_T_t_ds_task_instance_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_task_instance ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_test_flag;

delimiter d//
CREATE TABLE `t_ds_trigger_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `trigger_type` int(11) NOT NULL DEFAULT '0' COMMENT '0 process 1 task',
    `trigger_code` bigint(20) NOT NULL,
    `job_id` bigint(20) NOT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `t_ds_trigger_relation_trigger_code_IDX` (`trigger_code`),
    UNIQUE KEY `t_ds_trigger_relation_UN` (`trigger_type`,`job_id`,`trigger_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
d//
delimiter ;

-- uc_dolphin_T_t_ds_task_definition_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_definition_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_definition_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_definition ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_definition_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_definition_R_is_cache;


-- uc_dolphin_T_t_ds_task_definition_log_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_definition_log_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_definition_log_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition_log'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_definition_log ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_definition_log_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_definition_log_R_is_cache;


-- uc_dolphin_T_t_ds_task_instance_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_instance ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_is_cache;

-- uc_dolphin_T_t_ds_task_instance_R_cache_key
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_cache_key;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_cache_key()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='cache_key')
   THEN
ALTER TABLE t_ds_task_instance ADD `cache_key` varchar(255) DEFAULT null COMMENT 'cache key';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_cache_key;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_cache_key;


-- ALTER TABLE `t_ds_task_instance` ADD KEY `cache_key`( `cache_key`);
drop PROCEDURE if EXISTS add_t_ds_task_instance_idx_cache_key;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_idx_cache_key()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='cache_key')
    THEN
ALTER TABLE `t_ds_task_instance` ADD KEY `cache_key`( `cache_key` );
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_idx_cache_key;
DROP PROCEDURE add_t_ds_task_instance_idx_cache_key;

-- ALTER TABLE `t_ds_process_instance` ADD column `project_code`, `executor_name`, `tenant_code`;
drop PROCEDURE if EXISTS add_t_ds_process_instance_add_project_code;
delimiter d//
CREATE PROCEDURE add_t_ds_process_instance_add_project_code()
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='project_code')
   THEN
ALTER TABLE t_ds_process_instance ADD `project_code` bigint(20) DEFAULT NULL COMMENT 'project code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='executor_name')
   THEN
ALTER TABLE t_ds_process_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_code')
   THEN
ALTER TABLE t_ds_process_instance ADD `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_instance_add_project_code;
DROP PROCEDURE add_t_ds_process_instance_add_project_code;

-- ALTER TABLE `t_ds_task_instance` ADD column `project_code`, `process_definition_name`, `executor_name`
drop PROCEDURE if EXISTS add_t_ds_task_instance_add_project_code;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_add_project_code()
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='process_instance_name')
   THEN
ALTER TABLE t_ds_task_instance ADD `process_instance_name` varchar(255) DEFAULT NULL COMMENT 'process instance name';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='project_code')
   THEN
ALTER TABLE t_ds_task_instance ADD `project_code` bigint(20) DEFAULT NULL COMMENT 'project code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='executor_name')
   THEN
ALTER TABLE t_ds_task_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_add_project_code;
DROP PROCEDURE add_t_ds_task_instance_add_project_code;

alter table t_ds_access_token CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_alert CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_alertgroup CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_command CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_datasource CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_error_command CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_process_definition CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_process_definition_log CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_task_definition CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_task_definition_log CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_process_task_relation CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_process_task_relation_log CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_process_instance CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_project CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_queue CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_datasource_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_process_instance CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_project_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_resources_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_udfs_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_resources CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_schedules CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_session CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_task_instance CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_tenant CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_udfs CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_worker_group CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_version CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_plugin_define CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_alert_plugin_instance CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_comparison_type CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_execute_result CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_rule CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_rule_execute_sql CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_rule_input_entry CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_dq_task_statistics_value CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_rule_execute_sql CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_rule_input_entry CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_environment CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_environment_worker_group_relation CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_task_group_queue CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_task_group CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_audit_log CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_k8s CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_k8s_namespace CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_relation_namespace_user CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_alert_send_status CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_cluster CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_fav_task CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;
alter table t_ds_trigger_relation CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;

ALTER TABLE `t_ds_alert`
    MODIFY `title` varchar (512) null comment 'title';

ALTER TABLE `t_ds_command` MODIFY `worker_group` varchar(255)  COMMENT 'worker group';
ALTER TABLE `t_ds_error_command` MODIFY `worker_group` varchar(255)  COMMENT 'worker group';
ALTER TABLE `t_ds_process_definition_log` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'process definition name';
ALTER TABLE `t_ds_task_definition` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'task definition name';
ALTER TABLE `t_ds_task_definition` MODIFY `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker grouping';
ALTER TABLE `t_ds_task_definition_log` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'task definition name';
ALTER TABLE `t_ds_task_definition_log` MODIFY `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker grouping';
ALTER TABLE `t_ds_process_task_relation` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'relation name';
ALTER TABLE `t_ds_process_task_relation_log` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'relation name';
ALTER TABLE `t_ds_process_instance` MODIFY `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group id';
ALTER TABLE `t_ds_project` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'project name';
ALTER TABLE `t_ds_schedules` MODIFY `worker_group` varchar(255) DEFAULT '' COMMENT 'worker group id';
ALTER TABLE `t_ds_task_instance` MODIFY `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group id';
ALTER TABLE `t_ds_udfs` MODIFY `func_name` varchar(255) NOT NULL COMMENT 'UDF function name';
ALTER TABLE `t_ds_version` MODIFY `version` varchar(63) NOT NULL;
ALTER TABLE `t_ds_plugin_define` MODIFY `plugin_name` varchar(255) NOT NULL COMMENT 'the name of plugin eg: email';
ALTER TABLE `t_ds_plugin_define` MODIFY `plugin_type` varchar(63) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin';
ALTER TABLE `t_ds_alert_plugin_instance` MODIFY `instance_name` varchar(255) DEFAULT NULL COMMENT 'alert instance name';
ALTER TABLE `t_ds_dq_comparison_type` MODIFY `type` varchar(255) NOT NULL;
ALTER TABLE `t_ds_dq_comparison_type` MODIFY `name` varchar(255) DEFAULT NULL;
ALTER TABLE `t_ds_dq_rule` MODIFY `name` varchar(255) DEFAULT NULL;
ALTER TABLE `t_ds_environment` MODIFY `name` varchar(255) NOT NULL COMMENT 'environment name';
ALTER TABLE `t_ds_task_group_queue` MODIFY `task_name` varchar(255) DEFAULT NULL COMMENT 'TaskInstance name';
ALTER TABLE `t_ds_task_group` MODIFY `name` varchar(255) DEFAULT NULL COMMENT 'task_group name';
ALTER TABLE `t_ds_k8s` MODIFY `k8s_name` varchar(255) DEFAULT NULL;
ALTER TABLE `t_ds_k8s_namespace` MODIFY `namespace` varchar(255) DEFAULT NULL;
ALTER TABLE `t_ds_cluster` MODIFY `name`        varchar(255) NOT NULL COMMENT 'cluster name';

-- tenant improvement
DROP PROCEDURE if EXISTS add_improvement_workflow_run_tenant;
delimiter d//
CREATE PROCEDURE add_improvement_workflow_run_tenant()
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_code')
   THEN
ALTER TABLE t_ds_command ADD `tenant_code` varchar(64) DEFAULT 'default' COMMENT 'tenant code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_error_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_code')
   THEN
ALTER TABLE t_ds_error_command ADD `tenant_code` varchar(64) DEFAULT 'default' COMMENT 'tenant code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_schedules'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_code')
   THEN
ALTER TABLE t_ds_schedules ADD `tenant_code` varchar(64) DEFAULT 'default' COMMENT 'tenant code';
END IF;
END;
d//
delimiter ;
CALL add_improvement_workflow_run_tenant;
DROP PROCEDURE add_improvement_workflow_run_tenant;

-- uc_dolphin_T_t_ds_relation_sub_workflow
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_relation_sub_workflow;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_relation_sub_workflow()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_relation_sub_workflow'
           AND TABLE_SCHEMA=(SELECT DATABASE()))
   THEN
CREATE TABLE `t_ds_relation_sub_workflow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `parent_workflow_instance_id` bigint  NOT NULL,
    `parent_task_code` bigint  NOT NULL,
    `sub_workflow_instance_id` bigint  NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_parent_workflow_instance_id` (`parent_workflow_instance_id`),
    KEY `idx_parent_task_code` (`parent_task_code`),
    KEY `idx_sub_workflow_instance_id` (`sub_workflow_instance_id`)
);
END IF;
END;

d//
