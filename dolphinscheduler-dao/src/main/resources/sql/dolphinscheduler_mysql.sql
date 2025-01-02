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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_access_token
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alert
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert`;
CREATE TABLE `t_ds_alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `title` varchar(512) DEFAULT NULL COMMENT 'title',
  `sign` char(40) NOT NULL DEFAULT '' COMMENT 'sign=sha1(content)',
  `content` text COMMENT 'Message content (can be email, can be SMS. Mail is stored in JSON map, and SMS is string)',
  `alert_status` tinyint(4) DEFAULT '0' COMMENT '0:wait running,1:success,2:failed',
  `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 workflow is successfully, 2 workflow/task is failed',
  `log` text COMMENT 'log',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `project_code` bigint(20) DEFAULT NULL COMMENT 'project_code',
  `workflow_definition_code` bigint(20) DEFAULT NULL COMMENT 'workflow_definition_code',
  `workflow_instance_id` int(11) DEFAULT NULL COMMENT 'workflow_instance_id',
  `alert_type` int(11) DEFAULT NULL COMMENT 'alert_type',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`alert_status`) USING BTREE,
  KEY `idx_sign` (`sign`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_command`;
CREATE TABLE `t_ds_command` (
  `id`                        int(11)    NOT NULL AUTO_INCREMENT COMMENT 'key',
  `command_type`              tinyint(4) DEFAULT NULL COMMENT 'Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause workflow, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
  `workflow_definition_code`   bigint(20) NOT NULL COMMENT 'workflow definition code',
  `workflow_definition_version` int(11) DEFAULT '0' COMMENT 'workflow definition version',
  `workflow_instance_id`       int(11) DEFAULT '0' COMMENT 'workflow instance id',
  `command_param`             text COMMENT 'json command parameters',
  `task_depend_type`          tinyint(4) DEFAULT NULL COMMENT 'Node dependency type: 0 current node, 1 forward, 2 backward',
  `failure_strategy`          tinyint(4) DEFAULT '0' COMMENT 'Failed policy: 0 end, 1 continue',
  `warning_type`              tinyint(4) DEFAULT '0' COMMENT 'Alarm type: 0 is not sent, 1 workflow is sent successfully, 2 workflow is sent failed, 3 workflow is sent successfully and all failures are sent',
  `warning_group_id`          int(11) DEFAULT NULL COMMENT 'warning group',
  `schedule_time`             datetime DEFAULT NULL COMMENT 'schedule time',
  `start_time`                datetime DEFAULT NULL COMMENT 'start time',
  `executor_id`               int(11) DEFAULT NULL COMMENT 'executor id',
  `update_time`               datetime DEFAULT NULL COMMENT 'update time',
  `workflow_instance_priority` int(11) DEFAULT '2' COMMENT 'workflow instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group`              varchar(255)  COMMENT 'worker group',
  `tenant_code`               varchar(64) DEFAULT 'default' COMMENT 'tenant code',
  `environment_code`          bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `dry_run`                   tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
  `test_flag`                 tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run',
  PRIMARY KEY (`id`),
  KEY `priority_id_index` (`workflow_instance_priority`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  `workflow_definition_code` bigint(20) NOT NULL COMMENT 'workflow definition code',
  `workflow_definition_version` int(11) DEFAULT '0' COMMENT 'workflow definition version',
  `workflow_instance_id` int(11) DEFAULT '0' COMMENT 'workflow instance id: 0',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'scheduler time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `workflow_instance_priority` int(11) DEFAULT '2' COMMENT 'workflow instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(255)  COMMENT 'worker group',
  `tenant_code`  varchar(64) DEFAULT 'default' COMMENT 'tenant code',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `message` text COMMENT 'message',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
  `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_workflow_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_workflow_definition`;
CREATE TABLE `t_ds_workflow_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(255) DEFAULT NULL COMMENT 'workflow definition name',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'workflow definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'workflow definition release state：0:offline,1:online',
  `user_id` int(11) DEFAULT NULL COMMENT 'workflow definition creator id',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out, unit: minute',
  `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`,`code`),
  UNIQUE KEY `workflow_unique` (`name`,`project_code`) USING BTREE,
  KEY `idx_project_code` (`project_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_workflow_definition_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_workflow_definition_log`;
CREATE TABLE `t_ds_workflow_definition_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(255) DEFAULT NULL COMMENT 'workflow definition name',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'workflow definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'workflow definition release state：0:offline,1:online',
  `user_id` int(11) DEFAULT NULL COMMENT 'workflow definition creator id',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out,unit: minute',
  `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_code_version` (`code`,`version`) USING BTREE,
  KEY `idx_project_code` (`project_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_definition`;
CREATE TABLE `t_ds_task_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(255) DEFAULT NULL COMMENT 'task definition name',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'task definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
  `task_params` longtext COMMENT 'job custom parameters',
  `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
  `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
  `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker grouping',
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
  `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity',
  `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`,`code`),
  KEY `idx_project_code` (`project_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_definition_log`;
CREATE TABLE `t_ds_task_definition_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `name` varchar(255) DEFAULT NULL COMMENT 'task definition name',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT 'task definition version',
  `description` text COMMENT 'description',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `user_id` int(11) DEFAULT NULL COMMENT 'task definition creator id',
  `task_type` varchar(50) NOT NULL COMMENT 'task type',
  `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
  `task_params` longtext COMMENT 'job custom parameters',
  `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
  `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
  `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker grouping',
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
  `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity',
  `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `idx_code_version` (`code`,`version`),
  KEY `idx_project_code` (`project_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_workflow_task_relation
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_workflow_task_relation`;
CREATE TABLE `t_ds_workflow_task_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `name` varchar(255) DEFAULT NULL COMMENT 'relation name',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `workflow_definition_code` bigint(20) NOT NULL COMMENT 'workflow code',
  `workflow_definition_version` int(11) NOT NULL COMMENT 'workflow version',
  `pre_task_code` bigint(20) NOT NULL COMMENT 'pre task code',
  `pre_task_version` int(11) NOT NULL COMMENT 'pre task version',
  `post_task_code` bigint(20) NOT NULL COMMENT 'post task code',
  `post_task_version` int(11) NOT NULL COMMENT 'post task version',
  `condition_type` tinyint(2) DEFAULT NULL COMMENT 'condition type : 0 none, 1 judge 2 delay',
  `condition_params` text COMMENT 'condition params(json)',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`project_code`,`workflow_definition_code`),
  KEY `idx_pre_task_code_version` (`pre_task_code`,`pre_task_version`),
  KEY `idx_post_task_code_version` (`post_task_code`,`post_task_version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_workflow_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_workflow_task_relation_log`;
CREATE TABLE `t_ds_workflow_task_relation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'self-increasing id',
  `name` varchar(255) DEFAULT NULL COMMENT 'relation name',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `workflow_definition_code` bigint(20) NOT NULL COMMENT 'workflow code',
  `workflow_definition_version` int(11) NOT NULL COMMENT 'workflow version',
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
  KEY `idx_workflow_code_version` (`workflow_definition_code`,`workflow_definition_version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_workflow_instance`;
CREATE TABLE `t_ds_workflow_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'workflow instance name',
  `workflow_definition_code` bigint(20) NOT NULL COMMENT 'workflow definition code',
  `workflow_definition_version` int(11) NOT NULL DEFAULT '1' COMMENT 'workflow definition version',
  `project_code` bigint(20) DEFAULT NULL COMMENT 'project code',
  `state` tinyint(4) DEFAULT NULL COMMENT 'workflow instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `state_history` text DEFAULT NULL COMMENT 'state history desc',
  `recovery` tinyint(4) DEFAULT NULL COMMENT 'workflow instance failover flag：0:normal,1:failover instance',
  `start_time` datetime DEFAULT NULL COMMENT 'workflow instance start time',
  `end_time` datetime DEFAULT NULL COMMENT 'workflow instance end time',
  `run_times` int(11) DEFAULT NULL COMMENT 'workflow instance run times',
  `host` varchar(135) DEFAULT NULL COMMENT 'workflow instance host',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type. 0: only current node,1:before the node,2:later nodes',
  `max_try_times` tinyint(4) DEFAULT '0' COMMENT 'max try times',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy. 0:end the workflow when node failed,1:continue running the other nodes when node failed',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type. 0:no warning,1:warning if workflow success,2:warning if workflow failed,3:warning if success',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
  `command_start_time` datetime DEFAULT NULL COMMENT 'command start time',
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT '1' COMMENT 'flag',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_sub_workflow` int(11) DEFAULT '0' COMMENT 'flag, whether the workflow is sub workflow',
  `executor_id` int(11) NOT NULL COMMENT 'executor id',
  `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name',
  `history_cmd` text COMMENT 'history commands of workflow instance operation',
  `workflow_instance_priority` int(11) DEFAULT '2' COMMENT 'workflow instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_code` varchar(64) DEFAULT 'default' COMMENT 'tenant code',
  `var_pool` longtext COMMENT 'var_pool',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
  `next_workflow_instance_id` int(11) DEFAULT '0' COMMENT 'serial queue next workflowInstanceId',
  `restart_time` datetime DEFAULT NULL COMMENT 'workflow instance restart time',
  `test_flag`  tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run',
  PRIMARY KEY (`id`),
  KEY `workflow_instance_index` (`workflow_definition_code`,`id`) USING BTREE,
  KEY `start_time_index` (`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_project`;
CREATE TABLE `t_ds_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'project name',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `description` varchar(255) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE,
  UNIQUE KEY `unique_name`(`name`),
  UNIQUE KEY `unique_code`(`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_project
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project_parameter
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_project_parameter`;
CREATE TABLE `t_ds_project_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `param_name` varchar(255) NOT NULL COMMENT 'project parameter name',
  `param_value` text NOT NULL COMMENT 'project parameter value',
  `param_data_type` varchar(50) DEFAULT 'VARCHAR' COMMENT 'project parameter data type',
  `code` bigint(20) NOT NULL COMMENT 'encoding',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_project_parameter_name`(`project_code`, `param_name`),
  UNIQUE KEY `unique_project_parameter_code`(`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_project_parameter
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project_preference
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_project_preference`;
CREATE TABLE `t_ds_project_preference` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
   `code` bigint(20) NOT NULL COMMENT 'encoding',
   `project_code` bigint(20) NOT NULL COMMENT 'project code',
   `preferences` varchar(512) NOT NULL COMMENT 'project preferences',
   `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
   `state` int(11) DEFAULT '1' comment '1 means enabled, 0 means disabled',
   `create_time` datetime NOT NULL COMMENT 'create time',
   `update_time` datetime DEFAULT NULL COMMENT 'update time',
   PRIMARY KEY (`id`),
   UNIQUE KEY `unique_project_preference_project_code`(`project_code`),
   UNIQUE KEY `unique_project_preference_code`(`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_project_preference
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_queue_name`(`queue_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT IGNORE INTO `t_ds_queue` VALUES ('1', 'default', 'default', null, null);

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_relation_datasource_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_workflow_instance`;
CREATE TABLE `t_ds_relation_workflow_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `parent_workflow_instance_id` int(11) DEFAULT NULL COMMENT 'parent workflow instance id',
  `parent_task_instance_id` int(11) DEFAULT NULL COMMENT 'parent workflow instance id',
  `workflow_instance_id` int(11) DEFAULT NULL COMMENT 'child workflow instance id',
  PRIMARY KEY (`id`),
  KEY `idx_parent_workflow_task` (`parent_workflow_instance_id`,`parent_task_instance_id`) ,
  KEY `idx_workflow_instance_id` (`workflow_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  UNIQUE KEY uniq_uid_pid(user_id,project_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_relation_project_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_resources_user
-- ----------------------------
-- Deprecated
DROP TABLE IF EXISTS `t_ds_relation_resources_user`;
CREATE TABLE `t_ds_relation_resources_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `resources_id` int(11) DEFAULT NULL COMMENT 'resource id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_resources
-- ----------------------------
-- Deprecated
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_resources
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_schedules
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_schedules`;
CREATE TABLE `t_ds_schedules` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `workflow_definition_code` bigint(20) NOT NULL COMMENT 'workflow definition code',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `timezone_id` varchar(40) DEFAULT NULL COMMENT 'schedule timezone id',
  `crontab` varchar(255) NOT NULL COMMENT 'crontab description',
  `failure_strategy` tinyint(4) NOT NULL COMMENT 'failure strategy. 0:end,1:continue',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `release_state` tinyint(4) NOT NULL COMMENT 'release state. 0:offline,1:online ',
  `warning_type` tinyint(4) NOT NULL COMMENT 'Alarm type: 0 is not sent, 1 workflow is sent successfully, 2 workflow is sent failed, 3 workflow is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `workflow_instance_priority` int(11) DEFAULT '2' COMMENT 'workflow instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(255) DEFAULT '' COMMENT 'worker group id',
  `tenant_code`  varchar(64) DEFAULT 'default' COMMENT 'tenant code',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
  `task_code` bigint(20) NOT NULL COMMENT 'task definition code',
  `task_definition_version` int(11) NOT NULL DEFAULT '1' COMMENT 'task definition version',
  `workflow_instance_id` int(11) DEFAULT NULL COMMENT 'workflow instance id',
  `workflow_instance_name` varchar(255) DEFAULT NULL COMMENT 'workflow instance name',
  `project_code` bigint(20) DEFAULT NULL COMMENT 'project code',
  `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
  `start_time` datetime DEFAULT NULL COMMENT 'task start time',
  `end_time` datetime DEFAULT NULL COMMENT 'task end time',
  `host` varchar(135) DEFAULT NULL COMMENT 'host of task running on',
  `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
  `log_path` longtext DEFAULT NULL COMMENT 'task log path',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
  `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
  `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
  `app_link` text COMMENT 'yarn app id',
  `task_params` longtext COMMENT 'job custom parameters',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `retry_interval` int(4) DEFAULT NULL COMMENT 'retry interval when task failed ',
  `max_retry_times` int(2) DEFAULT NULL COMMENT 'max retry times',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT 'task instance priority:0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group id',
  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
  `environment_config` text COMMENT 'this config contains many environment variables config',
  `executor_id` int(11) DEFAULT NULL,
  `executor_name` varchar(64) DEFAULT NULL,
  `first_submit_time` datetime DEFAULT NULL COMMENT 'task first submit time',
  `delay_time` int(4) DEFAULT '0' COMMENT 'task delay execution time',
  `var_pool` longtext COMMENT 'var_pool',
  `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
  `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
  `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity',
  `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity',
  `test_flag`  tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run',
  PRIMARY KEY (`id`),
  KEY `workflow_instance_id` (`workflow_instance_id`) USING BTREE,
  KEY `idx_code_version` (`task_code`, `task_definition_version`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_task_instance_context
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_instance_context`;
CREATE TABLE `t_ds_task_instance_context` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `task_instance_id` int(11) NOT NULL,
    `context` text NOT NULL,
    `context_type` varchar(200) NOT NULL COMMENT 'context type',
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `task_instance_id` (`task_instance_id`,`context_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_tenant_code`(`tenant_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------

INSERT IGNORE INTO `t_ds_tenant`
VALUES ('-1', 'default', 'default tenant', '1', current_timestamp, current_timestamp);

-- ----------------------------
-- Table structure for t_ds_udfs
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_udfs`;
CREATE TABLE `t_ds_udfs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `func_name` varchar(255) NOT NULL COMMENT 'UDF function name',
  `class_name` varchar(255) NOT NULL COMMENT 'class of udf',
  `type` tinyint(4) NOT NULL COMMENT 'Udf function type',
  `arg_types` varchar(255) DEFAULT NULL COMMENT 'arguments types',
  `database` varchar(255) DEFAULT NULL COMMENT 'data base',
  `description` varchar(255) DEFAULT NULL,
  `resource_id` int(11) NOT NULL COMMENT 'resource id',
  `resource_name` varchar(255) NOT NULL COMMENT 'resource name',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_func_name`(`func_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  `tenant_id` int(11) DEFAULT -1 COMMENT 'tenant id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `queue` varchar(64) DEFAULT NULL COMMENT 'queue',
  `state` tinyint(4) DEFAULT '1' COMMENT 'state 0:disable 1:enable',
  `time_zone` varchar(32) DEFAULT NULL COMMENT 'time zone',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  `description` text NULL DEFAULT NULL COMMENT 'description',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Records of t_ds_worker_group
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_version`;
CREATE TABLE `t_ds_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `version` varchar(63) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `version_UNIQUE` (`version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE = utf8_bin COMMENT='version';

-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT IGNORE INTO `t_ds_version` VALUES ('1', '3.3.0');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT IGNORE INTO `t_ds_alertgroup`(alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES (NULL, 1, 'default admin warning group', 'default admin warning group', current_timestamp, current_timestamp);

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT IGNORE INTO `t_ds_user`
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '-1', current_timestamp, current_timestamp, null, 1, null);

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_plugin_define`;
CREATE TABLE `t_ds_plugin_define` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(255) NOT NULL COMMENT 'the name of plugin eg: email',
  `plugin_type` varchar(63) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
  `plugin_params` text COMMENT 'plugin params',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
  `instance_name` varchar(255) DEFAULT NULL COMMENT 'alert instance name',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE = utf8_bin;


-- ----------------------------
-- Table structure for t_ds_relation_project_worker_group
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_project_worker_group`;
CREATE TABLE `t_ds_relation_project_worker_group` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `project_code` bigint(20) NOT NULL COMMENT 'project code',
    `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY unique_project_worker_group(project_code,worker_group)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;


-- ----------------------------
-- Table structure for t_ds_environment
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_environment`;
CREATE TABLE `t_ds_environment` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `code` bigint(20)  DEFAULT NULL COMMENT 'encoding',
  `name` varchar(255) NOT NULL COMMENT 'environment name',
  `config` text NULL DEFAULT NULL COMMENT 'this config contains many environment variables config',
  `description` text NULL DEFAULT NULL COMMENT 'the details',
  `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `environment_name_unique` (`name`),
  UNIQUE KEY `environment_code_unique` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_task_group_queue
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_group_queue`;
CREATE TABLE `t_ds_task_group_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT'key',
  `task_id` int(11) DEFAULT NULL COMMENT 'taskintanceid',
  `task_name` varchar(255) DEFAULT NULL COMMENT 'TaskInstance name',
  `group_id`  int(11) DEFAULT NULL COMMENT 'taskGroup id',
  `workflow_instance_id` int(11) DEFAULT NULL COMMENT 'workflow instance id',
  `priority` int(8) DEFAULT '0' COMMENT 'priority',
  `status` tinyint(4) DEFAULT '-1' COMMENT '-1: waiting  1: running  2: finished',
  `force_start` tinyint(4) DEFAULT '0' COMMENT 'is force start 0 NO ,1 YES',
  `in_queue` tinyint(4) DEFAULT '0' COMMENT 'ready to get the queue by other task finish 0 NO ,1 YES',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_t_ds_task_group_queue_in_queue` (`in_queue`),
  PRIMARY KEY( `id` )
)ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_task_group
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_group`;
CREATE TABLE `t_ds_task_group` (
   `id`  int(11)  NOT NULL AUTO_INCREMENT COMMENT'key',
   `name` varchar(255) DEFAULT NULL COMMENT 'task_group name',
   `description` varchar(255) DEFAULT NULL,
   `group_size` int (11) NOT NULL COMMENT'group size',
   `use_size` int (11) DEFAULT '0' COMMENT 'used size',
   `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
   `project_code` bigint(20) DEFAULT 0 COMMENT 'project code',
   `status` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY(`id`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_audit_log`;
CREATE TABLE `t_ds_audit_log` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `model_id` bigint(20) DEFAULT NULL COMMENT 'model id',
  `model_name` varchar(100) DEFAULT NULL COMMENT 'model name',
  `model_type` varchar(100) NOT NULL COMMENT 'model type',
  `operation_type` varchar(100) NOT NULL COMMENT 'operation type',
  `description` varchar(100) DEFAULT NULL COMMENT 'api description',
  `latency` int(11) DEFAULT NULL COMMENT 'api cost milliseconds',
  `detail` varchar(100) DEFAULT NULL COMMENT 'object change detail',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'operation time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT= 1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_k8s
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_k8s`;
CREATE TABLE `t_ds_k8s` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `k8s_name` varchar(255) DEFAULT NULL,
  `k8s_config` text DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_k8s_namespace
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_k8s_namespace`;
CREATE TABLE `t_ds_k8s_namespace` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` bigint(20) NOT NULL DEFAULT '0',
  `namespace` varchar(255) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `cluster_code` bigint(20) NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `k8s_namespace_unique` (`namespace`,`cluster_code`)
) ENGINE= INNODB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT= 1 DEFAULT CHARSET= utf8 COLLATE = utf8_bin;

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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;


-- ----------------------------
-- Table structure for t_ds_cluster
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_cluster`;
CREATE TABLE `t_ds_cluster`(
   `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
   `code`        bigint(20) DEFAULT NULL COMMENT 'encoding',
   `name`        varchar(255) NOT NULL COMMENT 'cluster name',
   `config`      text NULL DEFAULT NULL COMMENT 'this config contains many cluster variables config',
   `description` text NULL DEFAULT NULL COMMENT 'the details',
   `operator`    int(11) DEFAULT NULL COMMENT 'operator user id',
   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`),
   UNIQUE KEY `cluster_name_unique` (`name`),
   UNIQUE KEY `cluster_code_unique` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

-- ----------------------------
-- Table structure for t_ds_fav_task
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_fav_task`;
CREATE TABLE `t_ds_fav_task`
(
    `id`        bigint      NOT NULL AUTO_INCREMENT COMMENT 'id',
    `task_type` varchar(64) NOT NULL COMMENT 'favorite task type name',
    `user_id`   int         NOT NULL COMMENT 'user id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8 COLLATE = utf8_bin;


DROP TABLE IF EXISTS `t_ds_relation_sub_workflow`;
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

-- ----------------------------
-- Table structure for t_ds_workflow_task_lineage
-- ----------------------------
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
