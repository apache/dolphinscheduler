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

-- uc_dolphin_T_t_ds_user_A_state
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_user_A_state;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_user_A_state()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_user'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='state')
   THEN
         ALTER TABLE t_ds_user ADD `state` int(1) DEFAULT 1 COMMENT 'state 0:disable 1:enable';
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_user_A_state;
DROP PROCEDURE uc_dolphin_T_t_ds_user_A_state;

-- uc_dolphin_T_t_ds_tenant_A_tenant_name
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_tenant_A_tenant_name;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_tenant_A_tenant_name()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_tenant'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_name')
   THEN
         ALTER TABLE t_ds_tenant DROP `tenant_name`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_tenant_A_tenant_name;
DROP PROCEDURE uc_dolphin_T_t_ds_tenant_A_tenant_name;

-- uc_dolphin_T_t_ds_task_instance_A_first_submit_time
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_A_first_submit_time;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_A_first_submit_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='first_submit_time')
   THEN
         ALTER TABLE t_ds_task_instance ADD `first_submit_time` datetime DEFAULT NULL COMMENT 'task first submit time';
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_A_first_submit_time();
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_A_first_submit_time;

-- uc_dolphin_T_t_ds_task_instance_A_delay_time
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_A_delay_time;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_A_delay_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='delay_time')
   THEN
         ALTER TABLE t_ds_task_instance ADD `delay_time` int(4) DEFAULT '0' COMMENT 'task delay execution time';
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_A_delay_time();
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_A_delay_time;

-- uc_dolphin_T_t_ds_task_instance_A_var_pool
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_A_var_pool;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_A_var_pool()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='var_pool')
   THEN
         ALTER TABLE t_ds_task_instance ADD `var_pool` longtext NULL;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_A_var_pool();
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_A_var_pool;

-- uc_dolphin_T_t_ds_process_instance_A_var_pool
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_instance_A_var_pool;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_instance_A_var_pool()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='var_pool')
   THEN
         ALTER TABLE t_ds_process_instance ADD `var_pool` longtext NULL;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_instance_A_var_pool();
DROP PROCEDURE uc_dolphin_T_t_ds_process_instance_A_var_pool;

-- uc_dolphin_T_t_ds_process_definition_A_modify_by
drop PROCEDURE if EXISTS ct_dolphin_T_t_ds_process_definition_version;
delimiter d//
CREATE PROCEDURE ct_dolphin_T_t_ds_process_definition_version()
BEGIN
    CREATE TABLE IF NOT EXISTS `t_ds_process_definition_version` (
        `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
        `process_definition_id` int(11) NOT NULL COMMENT 'process definition id',
        `version` int(11) DEFAULT NULL COMMENT 'process definition version',
        `process_definition_json` longtext COMMENT 'process definition json content',
        `description` text,
        `global_params` text COMMENT 'global parameters',
        `locations` text COMMENT 'Node location information',
        `connects` text COMMENT 'Node connection information',
        `receivers` text COMMENT 'receivers',
        `receivers_cc` text COMMENT 'cc',
        `create_time` datetime DEFAULT NULL COMMENT 'create time',
        `timeout` int(11) DEFAULT '0' COMMENT 'time out',
        `resource_ids` varchar(255) DEFAULT NULL COMMENT 'resource ids',
        PRIMARY KEY (`id`),
        UNIQUE KEY `process_definition_id_and_version` (`process_definition_id`,`version`) USING BTREE,
        KEY `process_definition_index` (`id`) USING BTREE
    ) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8;
END;

d//

delimiter ;
CALL ct_dolphin_T_t_ds_process_definition_version;
DROP PROCEDURE ct_dolphin_T_t_ds_process_definition_version;

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_plugin_define`;
CREATE TABLE `t_ds_plugin_define` (
    `id` int NOT NULL AUTO_INCREMENT,
    `plugin_name` varchar(100) NOT NULL COMMENT 'the name of plugin eg: email',
    `plugin_type` varchar(100) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
    `plugin_params` text COMMENT 'plugin params',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert_plugin_instance`;
CREATE TABLE `t_ds_alert_plugin_instance` (
    `id`                     int NOT NULL AUTO_INCREMENT,
    `plugin_define_id`       int NOT NULL,
    `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
    `create_time`            timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `instance_name`          varchar(200) DEFAULT NULL COMMENT 'alert instance name',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- uc_dolphin_T_t_ds_process_definition_A_warning_group_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_A_warning_group_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_A_warning_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='warning_group_id')
   THEN
         ALTER TABLE t_ds_process_definition ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_A_warning_group_id();
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_A_warning_group_id;

-- uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition_version'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='warning_group_id')
   THEN
         ALTER TABLE t_ds_process_definition_version ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id();
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id;

-- uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alertgroup'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='alert_instance_ids')
   THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `alert_instance_ids` varchar (255) DEFAULT NULL COMMENT 'alert instance ids' AFTER `id`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids();
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids;

-- uc_dolphin_T_t_ds_alertgroup_A_create_user_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_A_create_user_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_create_user_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alertgroup'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='create_user_id')
   THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `create_user_id` int(11) DEFAULT NULL COMMENT 'create user id' AFTER `alert_instance_ids`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_A_create_user_id();
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_create_user_id;

-- uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_NAME='t_ds_alertgroup'
                     AND TABLE_SCHEMA=(SELECT DATABASE())
                     AND INDEX_NAME ='t_ds_alertgroup_name_un')
    THEN
        ALTER TABLE t_ds_alertgroup ADD UNIQUE KEY `t_ds_alertgroup_name_un` (`group_name`);
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName();
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName;

-- uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_NAME='t_ds_datasource'
                     AND TABLE_SCHEMA=(SELECT DATABASE())
                     AND INDEX_NAME ='t_ds_datasource_name_un')
    THEN
        ALTER TABLE t_ds_datasource ADD UNIQUE KEY `t_ds_datasource_name_un` (`name`, `type`);
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName();
DROP PROCEDURE uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName;

-- uc_dolphin_T_t_ds_schedules_A_add_timezone
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_schedules_A_add_timezone;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_schedules_A_add_timezone()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_NAME='t_ds_schedules'
                     AND TABLE_SCHEMA=(SELECT DATABASE())
                     AND COLUMN_NAME ='timezone_id')
    THEN
        ALTER TABLE t_ds_schedules ADD COLUMN `timezone_id` varchar(40) default NULL COMMENT 'schedule timezone id' AFTER `end_time`;
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_schedules_A_add_timezone();
DROP PROCEDURE uc_dolphin_T_t_ds_schedules_A_add_timezone;

-- uc_dolphin_T_t_ds_task_definition_A_drop_UN_taskName
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_definition_A_drop_UN_taskName;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_definition_A_drop_UN_taskName()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.STATISTICS
                   WHERE TABLE_NAME='t_ds_task_definition'
                     AND TABLE_SCHEMA=(SELECT DATABASE())
                     AND INDEX_NAME ='task_unique')
        ALTER TABLE t_ds_task_definition drop INDEX `task_unique`;
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_definition_A_drop_UN_taskName();
DROP PROCEDURE uc_dolphin_T_t_ds_task_definition_A_drop_UN_taskName;

-- ----------------------------
-- Table structure for t_ds_environment
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_environment`;
CREATE TABLE `t_ds_environment` (
   `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
   `code` bigint(20) DEFAULT NULL COMMENT 'encoding',
   `name` varchar(100) NOT NULL COMMENT 'environment config name',
   `config` text NULL DEFAULT NULL COMMENT 'this config contains many environment variables config',
   `description` text NULL DEFAULT NULL COMMENT 'the details',
   `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`),
   UNIQUE KEY `environment_name_unique` (`name`),
   UNIQUE KEY `environment_code_unique` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_definition`;
CREATE TABLE `t_ds_task_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
  `version` int(11) DEFAULT NULL COMMENT 'task definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_params` longtext COMMENT 'job custom parameters',
  `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
  `task_priority` tinyint(4) DEFAULT NULL COMMENT 'job priority',
  `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
  `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
  `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
  `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
  `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
  `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
  `resource_ids` varchar(255) DEFAULT NULL COMMENT 'resource id, separated by comma',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_definition_log`;
CREATE TABLE `t_ds_task_definition_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
  `version` int(11) DEFAULT NULL COMMENT 'task definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_params` text COMMENT 'job custom parameters',
  `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
  `task_priority` tinyint(4) DEFAULT NULL COMMENT 'job priority',
  `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
  `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
  `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
  `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
  `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
  `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
  `resource_ids` varchar(255) DEFAULT NULL COMMENT 'resource id, separated by comma',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE t_ds_command ADD COLUMN `environment_code` bigint(20) default '-1' COMMENT 'environment code' AFTER `worker_group`;
ALTER TABLE t_ds_error_command ADD COLUMN `environment_code` bigint(20) default '-1' COMMENT 'environment code' AFTER `worker_group`;
ALTER TABLE t_ds_schedules ADD COLUMN `environment_code` bigint(20) default '-1' COMMENT 'environment code' AFTER `worker_group`;
ALTER TABLE t_ds_process_instance ADD COLUMN `environment_code` bigint(20) default '-1' COMMENT 'environment code' AFTER `worker_group`;
ALTER TABLE t_ds_task_instance ADD COLUMN `environment_code` bigint(20) default '-1' COMMENT 'environment code' AFTER `worker_group`;
ALTER TABLE t_ds_task_instance ADD COLUMN `environment_config` text COMMENT 'environment config' AFTER `environment_code`;

-- ----------------------------
-- Table structure for t_ds_environment_worker_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_environment_worker_group_relation`;
CREATE TABLE `t_ds_environment_worker_group_relation` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `environment_code` bigint(20) NOT NULL COMMENT 'environment code',
  `worker_group` varchar(255) NOT NULL COMMENT 'worker group id',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `environment_worker_group_unique` (`environment_code`,`worker_group`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- These columns will not be used in the new version,if you determine that the historical data is useless, you can delete it using the sql below
-- ----------------------------

-- ALTER TABLE t_ds_alert DROP `show_type`, DROP `alert_type`, DROP `receivers`, DROP `receivers_cc`;

-- ALTER TABLE t_ds_alertgroup DROP `group_type`;

-- ALTER TABLE t_ds_process_definition DROP `receivers`, DROP `receivers_cc`;

-- ALTER TABLE t_ds_process_definition_version DROP `receivers`, DROP `receivers_cc`;

-- DROP TABLE IF EXISTS t_ds_relation_user_alertgroup;

--
-- Table structure for table t_ds_dq_comparison_type
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

INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(1, '固定值', NULL, NULL, NULL, '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(2, '日波动', 'select round(avg(statistics_value),2) as day_avg from `t_ds_dq_task_statistics_value` where data_time >=date_trunc(''DAY'', ${data_time}) and data_time < date_add(date_trunc(''day'', ${data_time}),1) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'day_range', 'day_range.day_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(3, '周波动', 'select round(avg(statistics_value),2) as week_avg from `t_ds_dq_task_statistics_value` where  data_time >= date_trunc(''WEEK'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'week_range', 'week_range.week_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(4, '月波动', 'select round(avg(statistics_value),2) as month_avg from `t_ds_dq_task_statistics_value` where  data_time >= date_trunc(''MONTH'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'month_range', 'month_range.month_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(5, '最近7天波动', 'select round(avg(statistics_value),2) as last_7_avg from `t_ds_dq_task_statistics_value` where  data_time >= date_add(date_trunc(''day'', ${data_time}),-7) and  data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_seven_days', 'last_seven_days.last_7_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(6, '最近30天波动', 'select round(avg(statistics_value),2) as last_30_avg from `t_ds_dq_task_statistics_value` where  data_time >= date_add(date_trunc(''day'', ${data_time}),-30) and  data_time < date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_thirty_days', 'last_thirty_days.last_30_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(7, '源表总行数', 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(8, '目标表总行数', 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);

--
-- Table structure for table t_ds_dq_execute_result
--
DROP TABLE IF EXISTS `t_ds_dq_execute_result`;
CREATE TABLE `t_ds_dq_execute_result` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
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

INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(1, '空值校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(2, '自定义SQL', 1, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(3, '跨表准确性', 2, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(4, '跨表值比对', 3, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(5, '字段长度校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(6, '唯一性校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(7, '正则表达式', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(8, '及时性校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(9, '枚举值校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(10, '表行数校验', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');

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

INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(1, 1, 'SELECT COUNT(*) AS nulls FROM null_items', 'null_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(2, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(3, 1, 'SELECT COUNT(*) AS miss from miss_items', 'miss_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(4, 1, 'SELECT COUNT(*) AS valids FROM invalid_length_items', 'invalid_length_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(5, 1, 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(6, 1, 'SELECT ${src_field} FROM ${src_table} group by ${src_field} having count(*) > 1', 'duplicate_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(7, 1, 'SELECT COUNT(*) AS duplicates FROM duplicate_items', 'duplicate_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(8, 1, 'SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}', 'miss_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(9, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} not regexp ''${regexp_pattern}'') AND (${src_filter}) ', 'regexp_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(10, 1, 'SELECT COUNT(*) AS regexps FROM regexp_items', 'regexp_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(11, 1, 'SELECT * FROM ${src_table} WHERE (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${deadline}'', ''${datetime_format}'') <= 0) AND (${src_filter}) ', 'timeliness_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(12, 1, 'SELECT COUNT(*) AS timeliness FROM timeliness_items', 'timeliness_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(13, 1, 'SELECT * FROM ${src_table} where (${src_field} not in ( ${enum_list} ) or ${src_field} is null) AND (${src_filter}) ', 'enum_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(14, 1, 'SELECT COUNT(*) AS enums FROM enum_items', 'enum_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(15, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'table_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(16, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '''') AND (${src_filter})', 'null_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(17, 1, 'SELECT * FROM ${src_table} WHERE (length(${src_field}) ${logic_operator} ${field_length}) AND (${src_filter})', 'invalid_length_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table t_ds_dq_rule_input_entry
--
DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;
CREATE TABLE `t_ds_dq_rule_input_entry` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `field` varchar(255) DEFAULT NULL,
    `type` int(11) DEFAULT NULL,
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

INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(1, 'src_connector_type', 2, '源数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(2, 'src_datasource_id', 2, '源数据源', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(3, 'src_table', 2, '源数据表', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(4, 'src_filter', 0, '源表过滤条件', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(5, 'src_field', 2, '源表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(6, 'statistics_name', 0, '统计值名', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(7, 'check_type', 2, '校验方式', '0', '[{"label":"比对值 - 统计值","value":"0"},{"label":"统计值 - 比对值","value":"1"},{"label":"统计值 / 比对值","value":"2"},{"label":"(比对值-统计值) / 比对值","value":"3"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(8, 'operator', 2, '校验操作符', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(9, 'threshold', 0, '阈值', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(10, 'failure_strategy', 2, '失败策略', '0', '[{"label":"告警","value":"0"},{"label":"阻断","value":"1"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(11, 'target_connector_type', 2, '目标数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(12, 'target_datasource_id', 2, '目标数据源', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(13, 'target_table', 2, '目标数据表', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(14, 'target_filter', 0, '目标表过滤条件', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(15, 'mapping_columns', 6, 'ON语句', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"源数据列"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"操作符"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"目标数据列"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(16, 'statistics_execute_sql', 5, '统计值计算SQL', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(17, 'comparison_name', 0, '比对值名', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(18, 'comparison_execute_sql', 5, '比对值计算SQL', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(19, 'comparison_type', 2, '比对值类型', '', NULL, 'Please enter comparison title', 3, 0, 2, 1, 0, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(20, 'writer_connector_type', 2, '输出数据类型', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(21, 'writer_datasource_id', 2, '输出数据源', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(22, 'target_field', 2, '目标表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(23, 'field_length', 0, '字段长度限制', NULL, NULL, 'Please enter length limit', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(24, 'logic_operator', 2, '逻辑操作符', '=', '[{"label":"=","value":"="},{"label":"<","value":"<"},{"label":"<=","value":"<="},{"label":">","value":">"},{"label":">=","value":">="},{"label":"<>","value":"<>"}]', 'please select logic operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(25, 'regexp_pattern', 0, '正则表达式', NULL, NULL, 'Please enter regexp pattern', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(26, 'deadline', 0, '截止时间', NULL, NULL, 'Please enter deadline', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(27, 'datetime_format', 0, '时间格式', NULL, NULL, 'Please enter datetime format', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(28, 'enum_list', 0, '枚举值列表', NULL, NULL, 'Please enter enumeration', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

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
-- Table structure for table `t_ds_relation_rule_execute_sql`
--
DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;
CREATE TABLE `t_ds_relation_rule_execute_sql` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `rule_id` int(11) DEFAULT NULL,
    `execute_sql_id` int(11) DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(3, 5, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(2, 3, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(4, 3, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(5, 6, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(6, 6, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(7, 7, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(8, 7, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(9, 8, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(10, 8, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(11, 9, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(12, 9, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(13, 10, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(14, 1, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(15, 5, 17, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

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

INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(1, 1, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(2, 1, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(3, 1, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(4, 1, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(5, 1, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(6, 1, 6, '{"statistics_name":"null_count.nulls"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(7, 1, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(8, 1, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(9, 1, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(10, 1, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(11, 1, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(12, 1, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(13, 2, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(14, 2, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(15, 2, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(16, 2, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(17, 2, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(18, 2, 4, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(19, 2, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(20, 2, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(21, 2, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(22, 2, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(24, 2, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(25, 3, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(26, 3, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(27, 3, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(28, 3, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(29, 3, 11, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(30, 3, 12, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(31, 3, 13, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(32, 3, 14, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(33, 3, 15, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(34, 3, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(35, 3, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(36, 3, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(37, 3, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(38, 3, 17, '{"comparison_name":"total_count.total"}', 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(39, 3, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(40, 4, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(41, 4, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(42, 4, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(43, 4, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(44, 4, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(45, 4, 11, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(46, 4, 12, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(47, 4, 13, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(48, 4, 17, '{"is_show":"true","can_edit":"true"}', 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(49, 4, 18, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(50, 4, 7, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(51, 4, 8, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(52, 4, 9, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(53, 4, 10, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(62, 3, 6, '{"statistics_name":"miss_count.miss"}', 18, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(63, 5, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(64, 5, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(65, 5, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(66, 5, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(67, 5, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(68, 5, 6, '{"statistics_name":"invalid_length_count.valids"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(69, 5, 24, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(70, 5, 23, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(71, 5, 7, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(72, 5, 8, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(73, 5, 9, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(74, 5, 10, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(75, 5, 17, '', 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(76, 5, 19, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(79, 6, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(80, 6, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(81, 6, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(82, 6, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(83, 6, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(84, 6, 6, '{"statistics_name":"duplicate_count.duplicates"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(85, 6, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(86, 6, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(87, 6, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(88, 6, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(89, 6, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(90, 6, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(93, 7, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(94, 7, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(95, 7, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(96, 7, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(97, 7, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(98, 7, 6, '{"statistics_name":"regexp_count.regexps"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(99, 7, 25, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(100, 7, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(101, 7, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(102, 7, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(103, 7, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(104, 7, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(105, 7, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(108, 8, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(109, 8, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(110, 8, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(111, 8, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(112, 8, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(113, 8, 6, '{"statistics_name":"timeliness_count.timeliness"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(114, 8, 26, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(115, 8, 27, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(116, 8, 7, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(117, 8, 8, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(118, 8, 9, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(119, 8, 10, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(120, 8, 17, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(121, 8, 19, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(124, 9, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(125, 9, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(126, 9, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(127, 9, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(128, 9, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(129, 9, 6, '{"statistics_name":"enum_count.enums"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(130, 9, 28, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(131, 9, 7, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(132, 9, 8, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(133, 9, 9, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(134, 9, 10, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(135, 9, 17, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(136, 9, 19, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(139, 10, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(140, 10, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(141, 10, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(142, 10, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(143, 10, 6, '{"statistics_name":"table_count.total"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(144, 10, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(145, 10, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(146, 10, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(147, 10, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(148, 10, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(149, 10, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

-- ALTER TABLE t_ds_command DROP `dependence`;

-- ALTER TABLE t_ds_error_command DROP `dependence`;

