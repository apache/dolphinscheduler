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

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(200) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_access_token
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_access_token`;
CREATE TABLE `t_ds_access_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `token` varchar(64) DEFAULT NULL COMMENT 'token',
  `expire_time` datetime DEFAULT NULL COMMENT 'end time of token ',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_access_token
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alert
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert`;
CREATE TABLE `t_ds_alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `title` varchar(64) DEFAULT NULL COMMENT 'title',
  `content` text COMMENT 'Message content (can be email, can be SMS. Mail is stored in JSON map, and SMS is string)',
  `alert_status` tinyint(4) DEFAULT '0' COMMENT '0:wait running,1:success,2:failed',
  `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 process is successfully, 2 process/task is failed',
  `log` text COMMENT 'log',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`alert_status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alert
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alertgroup`;
CREATE TABLE `t_ds_alertgroup`(
  `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `alert_instance_ids` varchar (255) DEFAULT NULL COMMENT 'alert instance ids',
  `create_user_id` int(11) DEFAULT NULL COMMENT 'create user id',
  `group_name`     varchar(255) DEFAULT NULL COMMENT 'group name',
  `description`    varchar(255) DEFAULT NULL,
  `create_time`    datetime     DEFAULT NULL COMMENT 'create time',
  `update_time`    datetime     DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_alertgroup_name_un` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_command`;
CREATE TABLE `t_ds_command` (
  `id`                        int(11)    NOT NULL AUTO_INCREMENT COMMENT 'key',
  `command_type`              tinyint(4) DEFAULT NULL COMMENT 'Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
  `process_definition_code`   bigint(20) NOT NULL COMMENT 'process definition code',
  `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
  `process_instance_id`       int(11) DEFAULT '0' COMMENT 'process instance id',
  `command_param`             text COMMENT 'json command parameters',
  `task_depend_type`          tinyint(4) DEFAULT NULL COMMENT 'Node dependency type: 0 current node, 1 forward, 2 backward',
  `failure_strategy`          tinyint(4) DEFAULT '0' COMMENT 'Failed policy: 0 end, 1 continue',
  `warning_type`              tinyint(4) DEFAULT '0' COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id`          int(11) DEFAULT NULL COMMENT 'warning group',
  `schedule_time`             datetime DEFAULT NULL COMMENT 'schedule time',
  `start_time`                datetime DEFAULT NULL COMMENT 'start time',
  `executor_id`               int(11) DEFAULT NULL COMMENT 'executor id',
  `update_time`               datetime DEFAULT NULL COMMENT 'update time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group`              varchar(64)  COMMENT 'worker group',
  `environment_code`          bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `dry_run`                   tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
  PRIMARY KEY (`id`),
  KEY `priority_id_index` (`process_instance_priority`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_datasource`;
CREATE TABLE `t_ds_datasource` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(64) NOT NULL COMMENT 'data source name',
  `note` varchar(255) DEFAULT NULL COMMENT 'description',
  `type` tinyint(4) NOT NULL COMMENT 'data source type: 0:mysql,1:postgresql,2:hive,3:spark',
  `user_id` int(11) NOT NULL COMMENT 'the creator id',
  `connection_params` text NOT NULL COMMENT 'json connection params',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_datasource_name_un` (`name`, `type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_datasource
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_error_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_error_command`;
CREATE TABLE `t_ds_error_command` (
  `id` int(11) NOT NULL COMMENT 'key',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
  `executor_id` int(11) DEFAULT NULL COMMENT 'executor id',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
  `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
  `process_instance_id` int(11) DEFAULT '0' COMMENT 'process instance id: 0',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'scheduler time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64)  COMMENT 'worker group',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `message` text COMMENT 'message',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_definition`;
CREATE TABLE `t_ds_process_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(255) DEFAULT NULL COMMENT 'process definition name',
  `version` int(11) DEFAULT '0' COMMENT 'process definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online',
  `user_id` int(11) DEFAULT NULL COMMENT 'process definition creator id',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out, unit: minute',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`,`code`),
  UNIQUE KEY `process_unique` (`name`,`project_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_definition
-- ----------------------------

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
  `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority',
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
  `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
  `task_group_priority` tinyint(4) DEFAULT '0' COMMENT 'task group priority',
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
  `task_group_priority` tinyint(4) DEFAULT 0 COMMENT 'task group priority',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `idx_code_version` (`code`,`version`),
  KEY `idx_project_code` (`project_code`)
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
  PRIMARY KEY (`id`),
  KEY `idx_code` (`project_code`,`process_definition_code`),
  KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`),
  KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`)
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
  PRIMARY KEY (`id`),
  KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_instance`;
CREATE TABLE `t_ds_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'process instance name',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
  `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
  `state` tinyint(4) DEFAULT NULL COMMENT 'process instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `recovery` tinyint(4) DEFAULT NULL COMMENT 'process instance failover flag：0:normal,1:failover instance',
  `start_time` datetime DEFAULT NULL COMMENT 'process instance start time',
  `end_time` datetime DEFAULT NULL COMMENT 'process instance end time',
  `run_times` int(11) DEFAULT NULL COMMENT 'process instance run times',
  `host` varchar(135) DEFAULT NULL COMMENT 'process instance host',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type. 0: only current node,1:before the node,2:later nodes',
  `max_try_times` tinyint(4) DEFAULT '0' COMMENT 'max try times',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy. 0:end the process when node failed,1:continue running the other nodes when node failed',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type. 0:no warning,1:warning if process success,2:warning if process failed,3:warning if success',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
  `command_start_time` datetime DEFAULT NULL COMMENT 'command start time',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT '1' COMMENT 'flag',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_sub_process` int(11) DEFAULT '0' COMMENT 'flag, whether the process is sub process',
  `executor_id` int(11) NOT NULL COMMENT 'executor id',
  `history_cmd` text COMMENT 'history commands of process instance operation',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `var_pool` longtext COMMENT 'var_pool',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
  `next_process_instance_id` int(11) DEFAULT '0' COMMENT 'serial queue next processInstanceId',
  `restart_time` datetime DEFAULT NULL COMMENT 'process instance restart time',
  PRIMARY KEY (`id`),
  KEY `process_instance_index` (`process_definition_code`,`id`) USING BTREE,
  KEY `start_time_index` (`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_project`;
CREATE TABLE `t_ds_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(100) DEFAULT NULL COMMENT 'project name',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `description` varchar(200) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_project
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_queue
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_queue`;
CREATE TABLE `t_ds_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `queue_name` varchar(64) DEFAULT NULL COMMENT 'queue name',
  `queue` varchar(64) DEFAULT NULL COMMENT 'yarn queue name',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT INTO `t_ds_queue` VALUES ('1', 'default', 'default', null, null);

-- ----------------------------
-- Table structure for t_ds_relation_datasource_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_datasource_user`;
CREATE TABLE `t_ds_relation_datasource_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `datasource_id` int(11) DEFAULT NULL COMMENT 'data source id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_datasource_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_process_instance`;
CREATE TABLE `t_ds_relation_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `parent_process_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
  `parent_task_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'child process instance id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_project_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_project_user`;
CREATE TABLE `t_ds_relation_project_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `project_id` int(11) DEFAULT NULL COMMENT 'project id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_project_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_resources_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_resources_user`;
CREATE TABLE `t_ds_relation_resources_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `resources_id` int(11) DEFAULT NULL COMMENT 'resource id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_resources_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_udfs_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_udfs_user`;
CREATE TABLE `t_ds_relation_udfs_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'userid',
  `udf_id` int(11) DEFAULT NULL COMMENT 'udf id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_resources
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_resources`;
CREATE TABLE `t_ds_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `alias` varchar(64) DEFAULT NULL COMMENT 'alias',
  `file_name` varchar(64) DEFAULT NULL COMMENT 'file name',
  `description` varchar(255) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `type` tinyint(4) DEFAULT NULL COMMENT 'resource type,0:FILE，1:UDF',
  `size` bigint(20) DEFAULT NULL COMMENT 'resource size',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `pid` int(11) DEFAULT NULL,
  `full_name` varchar(128) DEFAULT NULL,
  `is_directory` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_resources_un` (`full_name`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_resources
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_schedules
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_schedules`;
CREATE TABLE `t_ds_schedules` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `timezone_id` varchar(40) DEFAULT NULL COMMENT 'schedule timezone id',
  `crontab` varchar(255) NOT NULL COMMENT 'crontab description',
  `failure_strategy` tinyint(4) NOT NULL COMMENT 'failure strategy. 0:end,1:continue',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `release_state` tinyint(4) NOT NULL COMMENT 'release state. 0:offline,1:online ',
  `warning_type` tinyint(4) NOT NULL COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT '' COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_schedules
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_session
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_session`;
CREATE TABLE `t_ds_session` (
  `id` varchar(64) NOT NULL COMMENT 'key',
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `ip` varchar(45) DEFAULT NULL COMMENT 'ip',
  `last_login_time` datetime DEFAULT NULL COMMENT 'last login time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_session
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_task_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_instance`;
CREATE TABLE `t_ds_task_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'task name',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_code` bigint(20) NOT NULL COMMENT 'task definition code',
  `task_definition_version` int(11) DEFAULT '0' COMMENT 'task definition version',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
  `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
  `start_time` datetime DEFAULT NULL COMMENT 'task start time',
  `end_time` datetime DEFAULT NULL COMMENT 'task end time',
  `host` varchar(135) DEFAULT NULL COMMENT 'host of task running on',
  `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'task log path',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
  `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
  `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
  `app_link` text COMMENT 'yarn app id',
  `task_params` longtext COMMENT 'job custom parameters',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `retry_interval` int(4) DEFAULT NULL COMMENT 'retry interval when task failed ',
  `max_retry_times` int(2) DEFAULT NULL COMMENT 'max retry times',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT 'task instance priority:0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `environment_config` text COMMENT 'this config contains many environment variables config',
  `executor_id` int(11) DEFAULT NULL,
  `first_submit_time` datetime DEFAULT NULL COMMENT 'task first submit time',
  `delay_time` int(4) DEFAULT '0' COMMENT 'task delay execution time',
  `var_pool` longtext COMMENT 'var_pool',
  `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
  PRIMARY KEY (`id`),
  KEY `process_instance_id` (`process_instance_id`) USING BTREE,
  KEY `idx_code_version` (`task_code`, `task_definition_version`) USING BTREE,
  CONSTRAINT `foreign_key_instance_id` FOREIGN KEY (`process_instance_id`) REFERENCES `t_ds_process_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_tenant
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_tenant`;
CREATE TABLE `t_ds_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code',
  `description` varchar(255) DEFAULT NULL,
  `queue_id` int(11) DEFAULT NULL COMMENT 'queue id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_udfs
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_udfs`;
CREATE TABLE `t_ds_udfs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `func_name` varchar(100) NOT NULL COMMENT 'UDF function name',
  `class_name` varchar(255) NOT NULL COMMENT 'class of udf',
  `type` tinyint(4) NOT NULL COMMENT 'Udf function type',
  `arg_types` varchar(255) DEFAULT NULL COMMENT 'arguments types',
  `database` varchar(255) DEFAULT NULL COMMENT 'data base',
  `description` varchar(255) DEFAULT NULL,
  `resource_id` int(11) NOT NULL COMMENT 'resource id',
  `resource_name` varchar(255) NOT NULL COMMENT 'resource name',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_udfs
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_user`;
CREATE TABLE `t_ds_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'user id',
  `user_name` varchar(64) DEFAULT NULL COMMENT 'user name',
  `user_password` varchar(64) DEFAULT NULL COMMENT 'user password',
  `user_type` tinyint(4) DEFAULT NULL COMMENT 'user type, 0:administrator，1:ordinary user',
  `email` varchar(64) DEFAULT NULL COMMENT 'email',
  `phone` varchar(11) DEFAULT NULL COMMENT 'phone',
  `tenant_id` int(11) DEFAULT NULL COMMENT 'tenant id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `queue` varchar(64) DEFAULT NULL COMMENT 'queue',
  `state` tinyint(4) DEFAULT '1' COMMENT 'state 0:disable 1:enable',
  `time_zone` varchar(32) DEFAULT NULL COMMENT 'time zone',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_worker_group
-- ----------------------------
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
-- Records of t_ds_worker_group
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_version`;
CREATE TABLE `t_ds_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `version` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `version_UNIQUE` (`version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='version';

-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT INTO `t_ds_version` VALUES ('1', '3.0.0');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO `t_ds_alertgroup`(alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES ('1,2', 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39', '2018-11-29 10:20:39');

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO `t_ds_user`
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', '2018-03-27 15:48:50', '2018-10-24 17:40:22', null, 1, null);

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

INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(1, 'FixValue', NULL, NULL, NULL, '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(2, 'DailyAvg', 'select round(avg(statistics_value),2) as day_avg from t_ds_dq_task_statistics_value where data_time >=date_trunc(''DAY'', ${data_time}) and data_time < date_add(date_trunc(''day'', ${data_time}),1) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'day_range', 'day_range.day_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(3, 'WeeklyAvg', 'select round(avg(statistics_value),2) as week_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''WEEK'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'week_range', 'week_range.week_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(4, 'MonthlyAvg', 'select round(avg(statistics_value),2) as month_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''MONTH'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'month_range', 'month_range.month_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(5, 'Last7DayAvg', 'select round(avg(statistics_value),2) as last_7_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-7) and  data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_seven_days', 'last_seven_days.last_7_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(6, 'Last30DayAvg', 'select round(avg(statistics_value),2) as last_30_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-30) and  data_time < date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_thirty_days', 'last_thirty_days.last_30_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(7, 'SrcTableTotalRows', 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(8, 'TargetTableTotalRows', 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);

--
-- Table structure for table `t_ds_dq_execute_result`
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

INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(1, '$t(null_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(2, '$t(custom_sql)', 1, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(3, '$t(multi_table_accuracy)', 2, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(4, '$t(multi_table_value_comparison)', 3, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(5, '$t(field_length_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(6, '$t(uniqueness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(7, '$t(regexp_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(8, '$t(timeliness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(9, '$t(enumeration_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(10, '$t(table_count_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');

--
-- Table structure for table `t_ds_dq_rule_execute_sql`
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
VALUES(11, 1, 'SELECT * FROM ${src_table} WHERE (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${deadline}'', ''${datetime_format}'') <= 0) AND (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${begin_time}'', ''${datetime_format}'') >= 0) AND (${src_filter}) ', 'timeliness_items', 0, 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
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
-- Table structure for table `t_ds_dq_rule_input_entry`
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

INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(1, 'src_connector_type', 'select', '$t(src_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(2, 'src_datasource_id', 'select', '$t(src_datasource_id)', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(3, 'src_table', 'select', '$t(src_table)', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(4, 'src_filter', 'input', '$t(src_filter)', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(5, 'src_field', 'select', '$t(src_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(6, 'statistics_name', 'input', '$t(statistics_name)', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(7, 'check_type', 'select', '$t(check_type)', '0', '[{"label":"Expected - Actual","value":"0"},{"label":"Actual - Expected","value":"1"},{"label":"Actual / Expected","value":"2"},{"label":"(Expected - Actual) / Expected","value":"3"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(8, 'operator', 'select', '$t(operator)', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(9, 'threshold', 'input', '$t(threshold)', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(10, 'failure_strategy', 'select', '$t(failure_strategy)', '0', '[{"label":"Alert","value":"0"},{"label":"Block","value":"1"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(11, 'target_connector_type', 'select', '$t(target_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(12, 'target_datasource_id', 'select', '$t(target_datasource_id)', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(13, 'target_table', 'select', '$t(target_table)', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(14, 'target_filter', 'input', '$t(target_filter)', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(15, 'mapping_columns', 'group', '$t(mapping_columns)', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"src_field"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"operator"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"target_field"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(16, 'statistics_execute_sql', 'textarea', '$t(statistics_execute_sql)', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(17, 'comparison_name', 'input', '$t(comparison_name)', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(18, 'comparison_execute_sql', 'textarea', '$t(comparison_execute_sql)', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(19, 'comparison_type', 'select', '$t(comparison_type)', '', NULL, 'Please enter comparison title', 3, 0, 2, 1, 0, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(20, 'writer_connector_type', 'select', '$t(writer_connector_type)', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(21, 'writer_datasource_id', 'select', '$t(writer_datasource_id)', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(22, 'target_field', 'select', '$t(target_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(23, 'field_length', 'input', '$t(field_length)', NULL, NULL, 'Please enter length limit', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(24, 'logic_operator', 'select', '$t(logic_operator)', '=', '[{"label":"=","value":"="},{"label":"<","value":"<"},{"label":"<=","value":"<="},{"label":">","value":">"},{"label":">=","value":">="},{"label":"<>","value":"<>"}]', 'please select logic operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(25, 'regexp_pattern', 'input', '$t(regexp_pattern)', NULL, NULL, 'Please enter regexp pattern', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(26, 'deadline', 'input', '$t(deadline)', NULL, NULL, 'Please enter deadline', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(27, 'datetime_format', 'input', '$t(datetime_format)', NULL, NULL, 'Please enter datetime format', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(28, 'enum_list', 'input', '$t(enum_list)', NULL, NULL, 'Please enter enumeration', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(29, 'begin_time', 'input', '$t(begin_time)', NULL, NULL, 'Please enter begin time', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

--
-- Table structure for table `t_ds_dq_task_statistics_value`
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
    `create_time` datetime NULL,
    `update_time` datetime NULL,
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
-- Table structure for table `t_ds_relation_rule_input_entry`
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
VALUES(114, 8, 26, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(115, 8, 27, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(116, 8, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(117, 8, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(118, 8, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(119, 8, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(120, 8, 17, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(121, 8, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
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
INSERT INTO t_ds_relation_rule_input_entry
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(150, 8, 29, NULL, 7, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

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
  `owner` varchar(100) DEFAULT NULL,
  `pod_replicas` int(11) DEFAULT NULL,
  `pod_request_cpu` decimal(14,3) DEFAULT NULL,
  `pod_request_memory` int(11) DEFAULT NULL,
  `tag` varchar(100) DEFAULT NULL,
  `limits_cpu` decimal(14,3) DEFAULT NULL,
  `k8s` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `k8s_namespace_unique` (`namespace`,`k8s`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8;
