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

-- uc_dolphin_T_t_ds_resources_R_full_name
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_resources_R_full_name;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_resources_R_full_name()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_resources'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='full_name')
    THEN
ALTER TABLE t_ds_resources MODIFY COLUMN `full_name` varchar(128);
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_resources_R_full_name;
DROP PROCEDURE uc_dolphin_T_t_ds_resources_R_full_name;

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
ALTER TABLE t_ds_relation_project_user ADD UNIQUE KEY uniq_uid_pid(user_id,project_id);

ALTER TABLE `t_ds_task_instance` ADD INDEX `idx_code_version` (`task_code`, `task_definition_version`) USING BTREE;
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_params` longtext COMMENT 'job custom parameters' AFTER `app_link`;
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_code` (`project_code`, `process_definition_code`) USING BTREE;
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`);
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`);
ALTER TABLE `t_ds_process_task_relation_log` ADD KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`) USING BTREE;

ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_project_code` (`project_code`) USING BTREE;
ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_code_version` (`code`,`version`) USING BTREE;
alter table t_ds_task_definition_log add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
alter table t_ds_task_definition_log add `task_group_priority` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `task_group_id`;
alter table t_ds_task_definition add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
alter table t_ds_task_definition add `task_group_priority` int(11) DEFAULT '0' COMMENT 'task group id' AFTER `task_group_id`;

ALTER TABLE `t_ds_user` ADD COLUMN `time_zone` varchar(32) DEFAULT NULL COMMENT 'time zone';
ALTER TABLE `t_ds_alert` ADD COLUMN `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 process is successfully, 2 process/task is failed';

ALTER TABLE `t_ds_alert` ADD INDEX `idx_status` (`alert_status`) USING BTREE;

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
  KEY `user_id_index` (`user_id`),
  UNIQUE KEY `namespace_user_unique` (`user_id`,`namespace_id`)
) ENGINE=InnoDB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;