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
         ALTER TABLE t_ds_user ADD `state` tinyint(4) DEFAULT '1' COMMENT 'state 0:disable 1:enable';
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

-- uc_dolphin_T_t_ds_project_A_add_code
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_project_A_add_code;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_project_A_add_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_NAME='t_ds_project'
                     AND TABLE_SCHEMA=(SELECT DATABASE())
                     AND COLUMN_NAME ='code')
    THEN
        alter table t_ds_project add `code` bigint(20) COMMENT 'encoding' AFTER `name`;
        -- update default value for not null
        UPDATE t_ds_project SET code = id;
        alter table t_ds_project modify `code` bigint(20) NOT NULL;
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_project_A_add_code();
DROP PROCEDURE uc_dolphin_T_t_ds_project_A_add_code;

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
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
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_define_id` int NOT NULL,
  `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `instance_name` varchar(200) DEFAULT NULL COMMENT 'alert instance name',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_environment
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_environment`;
CREATE TABLE `t_ds_environment` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `code` bigint(20)  DEFAULT NULL COMMENT 'encoding',
  `name` varchar(100) NOT NULL COMMENT 'environment name',
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
-- Table structure for t_ds_process_definition_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_definition_log`;
CREATE TABLE `t_ds_process_definition_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(200) DEFAULT NULL COMMENT 'process definition name',
  `version` int(11) DEFAULT '0' COMMENT 'process definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online',
  `user_id` int(11) DEFAULT NULL COMMENT 'process definition creator id',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out,unit: minute',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_definition`;
CREATE TABLE `t_ds_task_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(200) DEFAULT NULL COMMENT 'task definition name',
  `version` int(11) DEFAULT '0' COMMENT 'task definition version',
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
  `resource_ids` text COMMENT 'resource id, separated by comma',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
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
  `version` int(11) DEFAULT '0' COMMENT 'task definition version',
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
  `resource_ids` text DEFAULT NULL COMMENT 'resource id, separated by comma',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_process_task_relation
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_task_relation`;
CREATE TABLE `t_ds_process_task_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `name` varchar(200) DEFAULT NULL COMMENT 'relation name',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process code',
  `process_definition_version` int(11) NOT NULL COMMENT 'process version',
  `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
  `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
  `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
  `post_task_version` int(11) NOT NULL COMMENT 'post task version',
  `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
  `condition_params` text COMMENT 'condition params(json)',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_process_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_task_relation_log`;
CREATE TABLE `t_ds_process_task_relation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `name` varchar(200) DEFAULT NULL COMMENT 'relation name',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process code',
  `process_definition_version` int(11) NOT NULL COMMENT 'process version',
  `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
  `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
  `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
  `post_task_version` int(11) NOT NULL COMMENT 'post task version',
  `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
  `condition_params` text COMMENT 'condition params(json)',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- t_ds_worker_group
DROP TABLE IF EXISTS `t_ds_worker_group`;
CREATE TABLE `t_ds_worker_group` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) NOT NULL COMMENT 'worker group name',
  `addr_list` text NULL DEFAULT NULL COMMENT 'worker addr list. split by [,]',
  `create_time` datetime NULL DEFAULT NULL COMMENT 'create time',
  `update_time` datetime NULL DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
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

-- t_ds_command
alter table t_ds_command change process_definition_id process_definition_code bigint(20) NOT NULL COMMENT 'process definition code';
alter table t_ds_command add environment_code bigint(20) DEFAULT '-1' COMMENT 'environment code' AFTER worker_group;
alter table t_ds_command add dry_run tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run' AFTER environment_code;
alter table t_ds_command add process_definition_version int(11) DEFAULT '0' COMMENT 'process definition version' AFTER process_definition_code;
alter table t_ds_command add process_instance_id int(11) DEFAULT '0' COMMENT 'process instance id' AFTER process_definition_version;
alter table t_ds_command add KEY `priority_id_index` (`process_instance_priority`,`id`) USING BTREE;

-- t_ds_error_command
alter table t_ds_error_command change process_definition_id process_definition_code bigint(20) NOT NULL COMMENT 'process definition code';
alter table t_ds_error_command add environment_code bigint(20) DEFAULT '-1' COMMENT 'environment code' AFTER worker_group;
alter table t_ds_error_command add dry_run tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run' AFTER message;
alter table t_ds_error_command add process_definition_version int(11) DEFAULT '0' COMMENT 'process definition version' AFTER process_definition_code;
alter table t_ds_error_command add process_instance_id int(11) DEFAULT '0' COMMENT 'process instance id' AFTER process_definition_version;

-- t_ds_process_instance  note: Data migration is not supported
alter table t_ds_process_instance change process_definition_id process_definition_code bigint(20) NOT NULL COMMENT 'process definition code';
alter table t_ds_process_instance add process_definition_version int(11) DEFAULT '0' COMMENT 'process definition version' AFTER process_definition_code;
alter table t_ds_process_instance add environment_code bigint(20) DEFAULT '-1' COMMENT 'environment code' AFTER worker_group;
alter table t_ds_process_instance add var_pool longtext COMMENT 'var_pool' AFTER tenant_id;
alter table t_ds_process_instance add dry_run tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run' AFTER var_pool;
alter table t_ds_process_instance drop KEY `process_instance_index`;
alter table t_ds_process_instance add KEY `process_instance_index` (`process_definition_code`,`id`) USING BTREE;
alter table t_ds_process_instance drop process_instance_json;
alter table t_ds_process_instance drop locations;
alter table t_ds_process_instance drop connects;
alter table t_ds_process_instance drop dependence_schedule_times;

-- t_ds_task_instance   note: Data migration is not supported
alter table t_ds_task_instance change process_definition_id task_code bigint(20) NOT NULL COMMENT 'task definition code';
alter table t_ds_task_instance add task_definition_version int(11) DEFAULT '0' COMMENT 'task definition version' AFTER task_code;
alter table t_ds_task_instance add task_params text COMMENT 'job custom parameters' AFTER app_link;
alter table t_ds_task_instance add environment_code bigint(20) DEFAULT '-1' COMMENT 'environment code' AFTER worker_group;
alter table t_ds_task_instance add environment_config text COMMENT 'this config contains many environment variables config' AFTER environment_code;
alter table t_ds_task_instance add first_submit_time datetime DEFAULT NULL COMMENT 'task first submit time' AFTER executor_id;
alter table t_ds_task_instance add delay_time int(4) DEFAULT '0' COMMENT 'task delay execution time' AFTER first_submit_time;
alter table t_ds_task_instance add var_pool longtext COMMENT 'var_pool' AFTER delay_time;
alter table t_ds_task_instance add dry_run tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run' AFTER var_pool;
alter table t_ds_task_instance drop KEY `task_instance_index`;
alter table t_ds_task_instance drop task_json;

-- t_ds_schedules
alter table t_ds_schedules change process_definition_id process_definition_code bigint(20) NOT NULL COMMENT 'process definition code';
alter table t_ds_schedules add timezone_id varchar(40) DEFAULT NULL COMMENT 'timezoneId' AFTER end_time;
alter table t_ds_schedules add environment_code bigint(20) DEFAULT '-1' COMMENT 'environment code' AFTER worker_group;

-- t_ds_process_definition
alter table t_ds_process_definition add `code` bigint(20) COMMENT 'encoding' AFTER `id`;
-- update default value for not null
UPDATE t_ds_process_definition SET code = id;
alter table t_ds_process_definition modify `code` bigint(20) NOT NULL;
alter table t_ds_process_definition change project_id project_code bigint(20) NOT NULL COMMENT 'project code' AFTER `description`;
alter table t_ds_process_definition add `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `locations`;
alter table t_ds_process_definition add UNIQUE KEY `process_unique` (`name`,`project_code`) USING BTREE;
alter table t_ds_process_definition modify `description` text COMMENT 'description' after `version`;
alter table t_ds_process_definition modify `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online' after `project_code`;
alter table t_ds_process_definition modify `create_time` datetime DEFAULT NULL COMMENT 'create time' after `tenant_id`;
