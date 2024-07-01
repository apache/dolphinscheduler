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

-- uc_dolphin_T_t_ds_alert_R_sign
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alert_R_sign;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alert_R_sign()
BEGIN
        IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_NAME='t_ds_alert'
            AND TABLE_SCHEMA=(SELECT DATABASE())
            AND COLUMN_NAME='sign')
    THEN
ALTER TABLE `t_ds_alert` ADD COLUMN `sign` char(40) NOT NULL DEFAULT '' COMMENT 'sign=sha1(content)' after `id`;
ALTER TABLE `t_ds_alert` ADD INDEX `idx_sign` (`sign`) USING BTREE;
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alert_R_sign;
DROP PROCEDURE uc_dolphin_T_t_ds_alert_R_sign;

-- add unique key to t_ds_relation_project_user
drop PROCEDURE if EXISTS add_t_ds_relation_project_user_uk_uniq_uid_pid;
delimiter d//
CREATE PROCEDURE add_t_ds_relation_project_user_uk_uniq_uid_pid()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_relation_project_user'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='uniq_uid_pid')
    THEN
ALTER TABLE t_ds_relation_project_user ADD UNIQUE KEY uniq_uid_pid(user_id, project_id);
END IF;
END;

d//

delimiter ;
CALL add_t_ds_relation_project_user_uk_uniq_uid_pid;
DROP PROCEDURE add_t_ds_relation_project_user_uk_uniq_uid_pid;

-- drop t_ds_relation_project_user key user_id_index
drop PROCEDURE if EXISTS drop_t_ds_relation_project_user_key_user_id_index;
delimiter d//
CREATE PROCEDURE drop_t_ds_relation_project_user_key_user_id_index()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_relation_project_user'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='user_id_index')
    THEN
ALTER TABLE `t_ds_relation_project_user` DROP KEY `user_id_index`;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_relation_project_user_key_user_id_index;
DROP PROCEDURE drop_t_ds_relation_project_user_key_user_id_index;

-- add unique key to t_ds_project
drop PROCEDURE if EXISTS add_t_ds_project_uk_unique_name;
delimiter d//
CREATE PROCEDURE add_t_ds_project_uk_unique_name()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_project'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='unique_name')
    THEN
ALTER TABLE t_ds_project ADD UNIQUE KEY unique_name(name);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_project_uk_unique_name;
DROP PROCEDURE add_t_ds_project_uk_unique_name;

drop PROCEDURE if EXISTS add_t_ds_project_uk_unique_code;
delimiter d//
CREATE PROCEDURE add_t_ds_project_uk_unique_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_project'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='unique_code')
    THEN
ALTER TABLE t_ds_project ADD UNIQUE KEY unique_code(code);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_project_uk_unique_code;
DROP PROCEDURE add_t_ds_project_uk_unique_code;

-- add unique key to t_ds_queue
drop PROCEDURE if EXISTS add_t_ds_queue_uk_unique_queue_name;
delimiter d//
CREATE PROCEDURE add_t_ds_queue_uk_unique_queue_name()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_queue'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='unique_queue_name')
    THEN
ALTER TABLE t_ds_queue ADD UNIQUE KEY unique_queue_name(queue_name);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_queue_uk_unique_queue_name;
DROP PROCEDURE add_t_ds_queue_uk_unique_queue_name;

-- add unique key to t_ds_udfs
drop PROCEDURE if EXISTS add_t_ds_udfs_uk_unique_func_name;
delimiter d//
CREATE PROCEDURE add_t_ds_udfs_uk_unique_func_name()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_udfs'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='unique_func_name')
    THEN
ALTER TABLE t_ds_udfs ADD UNIQUE KEY unique_func_name(func_name);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_udfs_uk_unique_func_name;
DROP PROCEDURE add_t_ds_udfs_uk_unique_func_name;

-- add unique key to t_ds_tenant
drop PROCEDURE if EXISTS add_t_ds_tenant_uk_unique_tenant_code;
delimiter d//
CREATE PROCEDURE add_t_ds_tenant_uk_unique_tenant_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_tenant'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='unique_tenant_code')
    THEN
ALTER TABLE t_ds_tenant ADD UNIQUE KEY unique_tenant_code(tenant_code);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_tenant_uk_unique_tenant_code;
DROP PROCEDURE add_t_ds_tenant_uk_unique_tenant_code;

-- ALTER TABLE `t_ds_task_instance` ADD INDEX `idx_code_version` (`task_code`, `task_definition_version`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_task_instance_uk_idx_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_uk_idx_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_code_version')
    THEN
ALTER TABLE `t_ds_task_instance` ADD INDEX `idx_code_version` (`task_code`, `task_definition_version`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_uk_idx_code_version;
DROP PROCEDURE add_t_ds_task_instance_uk_idx_code_version;

-- ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_params` longtext COMMENT 'job custom parameters' AFTER `app_link`;
drop PROCEDURE if EXISTS modify_t_ds_task_instance_col_task_params;
delimiter d//
CREATE PROCEDURE modify_t_ds_task_instance_col_task_params()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='task_params')
    THEN
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_params` longtext COMMENT 'job custom parameters' AFTER `app_link`;
END IF;
END;
d//
delimiter ;
CALL modify_t_ds_task_instance_col_task_params;
DROP PROCEDURE modify_t_ds_task_instance_col_task_params;

-- ALTER TABLE `t_ds_task_instance` ADD COLUMN `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id';
drop PROCEDURE if EXISTS add_t_ds_task_instance_col_task_group_id;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_col_task_group_id()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='task_group_id')
    THEN
ALTER TABLE `t_ds_task_instance` ADD COLUMN `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' after `var_pool`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_col_task_group_id;
DROP PROCEDURE add_t_ds_task_instance_col_task_group_id;

-- ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_code` (`project_code`, `process_definition_code`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_process_task_relation_key_idx_code;
delimiter d//
CREATE PROCEDURE add_t_ds_process_task_relation_key_idx_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_process_task_relation'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_code')
    THEN
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_code` (`project_code`, `process_definition_code`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_task_relation_key_idx_code;
DROP PROCEDURE add_t_ds_process_task_relation_key_idx_code;

-- ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`);
drop PROCEDURE if EXISTS add_t_ds_process_task_relation_key_idx_pre_task_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_process_task_relation_key_idx_pre_task_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_process_task_relation'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_pre_task_code_version')
    THEN
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_task_relation_key_idx_pre_task_code_version;
DROP PROCEDURE add_t_ds_process_task_relation_key_idx_pre_task_code_version;

-- ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`);
drop PROCEDURE if EXISTS add_t_ds_process_task_relation_key_idx_post_task_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_process_task_relation_key_idx_post_task_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_process_task_relation'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_post_task_code_version')
    THEN
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_task_relation_key_idx_post_task_code_version;
DROP PROCEDURE add_t_ds_process_task_relation_key_idx_post_task_code_version;

-- ALTER TABLE `t_ds_process_task_relation_log` ADD KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_process_task_relation_key_idx_process_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_process_task_relation_key_idx_process_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_process_task_relation_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_process_code_version')
    THEN
ALTER TABLE `t_ds_process_task_relation_log` ADD KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_task_relation_key_idx_process_code_version;
DROP PROCEDURE add_t_ds_process_task_relation_key_idx_process_code_version;

-- ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_project_code` (`project_code`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_key_idx_process_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_key_idx_process_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_project_code')
    THEN
ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_project_code` (`project_code`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_key_idx_process_code_version;
DROP PROCEDURE add_t_ds_task_definition_log_key_idx_process_code_version;

-- ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_code_version` (`code`,`version`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_key_idx_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_key_idx_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_code_version')
    THEN
ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_code_version` (`code`,`version`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_key_idx_code_version;
DROP PROCEDURE add_t_ds_task_definition_log_key_idx_code_version;

-- alter table t_ds_task_definition_log add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_col_task_group_id;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_col_task_group_id()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_group_id')
    THEN
alter table t_ds_task_definition_log add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_col_task_group_id;
DROP PROCEDURE add_t_ds_task_definition_log_col_task_group_id;

-- alter table t_ds_task_definition_log add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_col_task_group_id;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_col_task_group_id()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_group_id')
    THEN
alter table t_ds_task_definition add `task_group_id` int DEFAULT NULL COMMENT 'task group id';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_col_task_group_id;
DROP PROCEDURE add_t_ds_task_definition_col_task_group_id;

-- alter table t_ds_task_definition_log add `task_group_priority` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `task_group_id`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_col_task_group_priority;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_col_task_group_priority()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_group_priority')
    THEN
alter table t_ds_task_definition_log add `task_group_priority` tinyint DEFAULT '0' COMMENT 'task group priority' AFTER `task_group_id`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_col_task_group_priority;
DROP PROCEDURE add_t_ds_task_definition_log_col_task_group_priority;

-- alter table t_ds_task_definition add `task_group_priority` int(11) DEFAULT '0' COMMENT 'task group id' AFTER `task_group_id`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_col_task_group_priority;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_col_task_group_priority()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_group_priority')
    THEN
alter table t_ds_task_definition add `task_group_priority` tinyint DEFAULT '0' COMMENT 'task group priority' AFTER `task_group_id`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_col_task_group_priority;
DROP PROCEDURE add_t_ds_task_definition_col_task_group_priority;

-- ALTER TABLE `t_ds_user` ADD COLUMN `time_zone` varchar(32) DEFAULT NULL COMMENT 'time zone';
drop PROCEDURE if EXISTS add_t_ds_user_col_time_zone;
delimiter d//
CREATE PROCEDURE add_t_ds_user_col_time_zone()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_user'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='time_zone')
    THEN
ALTER TABLE `t_ds_user` ADD COLUMN `time_zone` varchar(32) DEFAULT NULL COMMENT 'time zone';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_user_col_time_zone;
DROP PROCEDURE add_t_ds_user_col_time_zone;

-- ALTER TABLE `t_ds_alert` ADD COLUMN `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 process is successfully, 2 process/task is failed';
drop PROCEDURE if EXISTS add_t_ds_alert_col_warning_type;
delimiter d//
CREATE PROCEDURE add_t_ds_alert_col_warning_type()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_alert'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='warning_type')
    THEN
ALTER TABLE `t_ds_alert` ADD COLUMN `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 process is successfully, 2 process/task is failed';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_alert_col_warning_type;
DROP PROCEDURE add_t_ds_alert_col_warning_type;

-- ALTER TABLE `t_ds_alert` ADD INDEX `idx_status` (`alert_status`) USING BTREE;
drop PROCEDURE if EXISTS add_t_ds_alert_idx_idx_status;
delimiter d//
CREATE PROCEDURE add_t_ds_alert_idx_idx_status()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_alert'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_status')
    THEN
ALTER TABLE `t_ds_alert` ADD INDEX `idx_status` (`alert_status`) USING BTREE;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_alert_idx_idx_status;
DROP PROCEDURE add_t_ds_alert_idx_idx_status;

-- ALTER TABLE `t_ds_alert` ADD COLUMN `project_code` bigint DEFAULT NULL COMMENT 'project_code';
-- ALTER TABLE `t_ds_alert` ADD COLUMN `process_definition_code` bigint DEFAULT NULL COMMENT 'process_definition_code';
-- ALTER TABLE `t_ds_alert` ADD COLUMN `process_instance_id` int DEFAULT NULL COMMENT 'process_instance_id';
-- ALTER TABLE `t_ds_alert` ADD COLUMN `alert_type` int DEFAULT NULL COMMENT 'alert_type';
drop PROCEDURE if EXISTS add_t_ds_alert_col_project_code;
delimiter d//
CREATE PROCEDURE add_t_ds_alert_col_project_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_alert'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='project_code')
    THEN
ALTER TABLE `t_ds_alert` ADD COLUMN `project_code` bigint DEFAULT NULL COMMENT 'project_code';
ALTER TABLE `t_ds_alert` ADD COLUMN `process_definition_code` bigint DEFAULT NULL COMMENT 'process_definition_code';
ALTER TABLE `t_ds_alert` ADD COLUMN `process_instance_id` int DEFAULT NULL COMMENT 'process_instance_id';
ALTER TABLE `t_ds_alert` ADD COLUMN `alert_type` int DEFAULT NULL COMMENT 'alert_type';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_alert_col_project_code;
DROP PROCEDURE add_t_ds_alert_col_project_code;

-- t_ds_task_instance
drop PROCEDURE if EXISTS alter_t_ds_task_instance_col_log_path;
delimiter d//
CREATE PROCEDURE alter_t_ds_task_instance_col_log_path()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='log_path')
    THEN
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `log_path` longtext DEFAULT NULL COMMENT 'task log path';
END IF;
END;
d//
delimiter ;
CALL alter_t_ds_task_instance_col_log_path;
DROP PROCEDURE alter_t_ds_task_instance_col_log_path;

--
-- Table structure for table `t_ds_dq_comparison_type`
--
DROP TABLE IF EXISTS `t_ds_dq_comparison_type`;
CREATE TABLE `t_ds_dq_comparison_type` (
                                           `id` int(11) NOT NULL AUTO_INCREMENT,
                                           `type` varchar(100) NOT NULL,
                                           `execute_sql` text DEFAULT NULL,
                                           `output_table` varchar(100) DEFAULT NULL,
                                           `name` varchar(100) DEFAULT NULL,
                                           `create_time` datetime DEFAULT NULL,
                                           `update_time` datetime DEFAULT NULL,
                                           `is_inner_source` tinyint(1) DEFAULT '0',
                                           PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_execute_result
--
DROP TABLE IF EXISTS `t_ds_dq_execute_result`;
CREATE TABLE `t_ds_dq_execute_result` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT,
                                          `process_definition_id` int(11) DEFAULT NULL,
                                          `process_instance_id` int(11) DEFAULT NULL,
                                          `task_instance_id` int(11) DEFAULT NULL,
                                          `rule_type` int(11) DEFAULT NULL,
                                          `rule_name` varchar(255) DEFAULT NULL,
                                          `statistics_value` double DEFAULT NULL,
                                          `comparison_value` double DEFAULT NULL,
                                          `check_type` int(11) DEFAULT NULL,
                                          `threshold` double DEFAULT NULL,
                                          `operator` int(11) DEFAULT NULL,
                                          `failure_strategy` int(11) DEFAULT NULL,
                                          `state` int(11) DEFAULT NULL,
                                          `user_id` int(11) DEFAULT NULL,
                                          `comparison_type` int(11) DEFAULT NULL,
                                          `error_output_path` text DEFAULT NULL,
                                          `create_time` datetime DEFAULT NULL,
                                          `update_time` datetime DEFAULT NULL,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_rule
--
DROP TABLE IF EXISTS `t_ds_dq_rule`;
CREATE TABLE `t_ds_dq_rule` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `name` varchar(100) DEFAULT NULL,
                                `type` int(11) DEFAULT NULL,
                                `user_id` int(11) DEFAULT NULL,
                                `create_time` datetime DEFAULT NULL,
                                `update_time` datetime DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_rule_execute_sql
--
DROP TABLE IF EXISTS `t_ds_dq_rule_execute_sql`;
CREATE TABLE `t_ds_dq_rule_execute_sql` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `index` int(11) DEFAULT NULL,
                                            `sql` text DEFAULT NULL,
                                            `table_alias` varchar(255) DEFAULT NULL,
                                            `type` int(11) DEFAULT NULL,
                                            `is_error_output_sql` tinyint(1) DEFAULT '0',
                                            `create_time` datetime DEFAULT NULL,
                                            `update_time` datetime DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_rule_input_entry
--
DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;
CREATE TABLE `t_ds_dq_rule_input_entry` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `field` varchar(255) DEFAULT NULL,
                                            `type` varchar(255) DEFAULT NULL,
                                            `title` varchar(255) DEFAULT NULL,
                                            `value` varchar(255)  DEFAULT NULL,
                                            `options` text DEFAULT NULL,
                                            `placeholder` varchar(255) DEFAULT NULL,
                                            `option_source_type` int(11) DEFAULT NULL,
                                            `value_type` int(11) DEFAULT NULL,
                                            `input_type` int(11) DEFAULT NULL,
                                            `is_show` tinyint(1) DEFAULT '1',
                                            `can_edit` tinyint(1) DEFAULT '1',
                                            `is_emit` tinyint(1) DEFAULT '0',
                                            `is_validate` tinyint(1) DEFAULT '1',
                                            `create_time` datetime DEFAULT NULL,
                                            `update_time` datetime DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_task_statistics_value
--
DROP TABLE IF EXISTS `t_ds_dq_task_statistics_value`;
CREATE TABLE `t_ds_dq_task_statistics_value` (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                                 `process_definition_id` int(11) DEFAULT NULL,
                                                 `task_instance_id` int(11) DEFAULT NULL,
                                                 `rule_id` int(11) NOT NULL,
                                                 `unique_code` varchar(255) NULL,
                                                 `statistics_name` varchar(255) NULL,
                                                 `statistics_value` double NULL,
                                                 `data_time` datetime DEFAULT NULL,
                                                 `create_time` datetime DEFAULT NULL,
                                                 `update_time` datetime DEFAULT NULL,
                                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_relation_rule_execute_sql
--
DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;
CREATE TABLE `t_ds_relation_rule_execute_sql` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `execute_sql_id` int(11) DEFAULT NULL,
                                                  `create_time` datetime NULL,
                                                  `update_time` datetime NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_relation_rule_input_entry
--
DROP TABLE IF EXISTS `t_ds_relation_rule_input_entry`;
CREATE TABLE `t_ds_relation_rule_input_entry` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `rule_input_entry_id` int(11) DEFAULT NULL,
                                                  `values_map` text DEFAULT NULL,
                                                  `index` int(11) DEFAULT NULL,
                                                  `create_time` datetime DEFAULT NULL,
                                                  `update_time` datetime DEFAULT NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for t_ds_k8s
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_k8s`;
CREATE TABLE `t_ds_k8s` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `k8s_name` varchar(100) DEFAULT NULL,
  `k8s_config` text DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;

-- ----------------------------
-- Table structure for t_ds_k8s_namespace
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_k8s_namespace`;
CREATE TABLE `t_ds_k8s_namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `limits_memory` int(11) DEFAULT NULL,
  `namespace` varchar(100) DEFAULT NULL,
  `online_job_num` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `pod_replicas` int(11) DEFAULT NULL,
  `pod_request_cpu` decimal(14,3) DEFAULT NULL,
  `pod_request_memory` int(11) DEFAULT NULL,
  `limits_cpu` decimal(14,3) DEFAULT NULL,
  `k8s` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `k8s_namespace_unique` (`namespace`,`k8s`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;

-- ----------------------------
-- Table structure for t_ds_relation_namespace_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_namespace_user`;
CREATE TABLE `t_ds_relation_namespace_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `namespace_id` int(11) DEFAULT NULL COMMENT 'namespace id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `namespace_user_unique` (`user_id`,`namespace_id`)
) ENGINE=InnoDB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;

-- ----------------------------
-- Table structure for t_ds_alert_send_status
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert_send_status;
CREATE TABLE t_ds_alert_send_status (
    `id`                            int(11) NOT NULL AUTO_INCREMENT,
    `alert_id`                      int(11) NOT NULL,
    `alert_plugin_instance_id`      int(11) NOT NULL,
    `send_status`                   tinyint(4) DEFAULT '0',
    `log`                           text,
    `create_time`                   datetime DEFAULT NULL COMMENT 'create time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `alert_send_status_unique` (`alert_id`,`alert_plugin_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_audit_log`;
CREATE TABLE `t_ds_audit_log` (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT'key',
    `user_id` int(11) NOT NULL COMMENT 'user id',
    `resource_type` int(11) NOT NULL COMMENT 'resource type',
    `operation` int(11) NOT NULL COMMENT 'operation',
    `time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `resource_id` int(11) NULL DEFAULT NULL COMMENT 'resource id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT= 1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_task_group
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_group`;
CREATE TABLE `t_ds_task_group` (
   `id`  int(11)  NOT NULL AUTO_INCREMENT COMMENT'key',
   `name` varchar(100) DEFAULT NULL COMMENT 'task_group name',
   `description` varchar(200) DEFAULT NULL,
   `group_size` int (11) NOT NULL COMMENT'group size',
   `use_size` int (11) DEFAULT '0' COMMENT 'used size',
   `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
   `project_code` bigint(20) DEFAULT 0 COMMENT 'project code',
   `status` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY(`id`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;

-- ----------------------------
-- Table structure for t_ds_task_group_queue
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_group_queue`;
CREATE TABLE `t_ds_task_group_queue` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT'key',
   `task_id` int(11) DEFAULT NULL COMMENT 'taskintanceid',
   `task_name` varchar(100) DEFAULT NULL COMMENT 'TaskInstance name',
   `group_id`  int(11) DEFAULT NULL COMMENT 'taskGroup id',
   `process_id` int(11) DEFAULT NULL COMMENT 'processInstace id',
   `priority` int(8) DEFAULT '0' COMMENT 'priority',
   `status` tinyint(4) DEFAULT '-1' COMMENT '-1: waiting  1: running  2: finished',
   `force_start` tinyint(4) DEFAULT '0' COMMENT 'is force start 0 NO ,1 YES',
   `in_queue` tinyint(4) DEFAULT '0' COMMENT 'ready to get the queue by other task finish 0 NO ,1 YES',
   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY( `id` )
)ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;
