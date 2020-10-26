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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  `show_type` tinyint(4) DEFAULT NULL COMMENT 'send email type,0:TABLE,1:TEXT',
  `content` text COMMENT 'Message content (can be email, can be SMS. Mail is stored in JSON map, and SMS is string)',
  `alert_type` tinyint(4) DEFAULT NULL COMMENT '0:email,1:sms',
  `alert_status` tinyint(4) DEFAULT '0' COMMENT '0:wait running,1:success,2:failed',
  `log` text COMMENT 'log',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `receivers` text COMMENT 'receivers',
  `receivers_cc` text COMMENT 'cc',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alert
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alertgroup`;
CREATE TABLE `t_ds_alertgroup` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `group_name` varchar(255) DEFAULT NULL COMMENT 'group name',
  `group_type` tinyint(4) DEFAULT NULL COMMENT 'Group type (message 0, SMS 1...)',
  `description` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_command`;
CREATE TABLE `t_ds_command` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'Node dependency type: 0 current node, 1 forward, 2 backward',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'Failed policy: 0 end, 1 continue',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group',
  `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `executor_id` int(11) DEFAULT NULL COMMENT 'executor id',
  `dependence` varchar(255) DEFAULT NULL COMMENT 'dependence',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64)  COMMENT 'worker group',
  PRIMARY KEY (`id`)
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
  `note` varchar(256) DEFAULT NULL COMMENT 'description',
  `type` tinyint(4) NOT NULL COMMENT 'data source type: 0:mysql,1:postgresql,2:hive,3:spark',
  `user_id` int(11) NOT NULL COMMENT 'the creator id',
  `connection_params` text NOT NULL COMMENT 'json connection params',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
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
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'scheduler time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `dependence` text COMMENT 'dependence',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64)  COMMENT 'worker group',
  `message` text COMMENT 'message',
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
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'process definition name',
  `version` int(11) DEFAULT NULL COMMENT 'process definition version',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online',
  `project_id` int(11) DEFAULT NULL COMMENT 'project id',
  `user_id` int(11) DEFAULT NULL COMMENT 'process definition creator id',
  `process_definition_json` longtext COMMENT 'process definition json content',
  `description` text,
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `connects` text COMMENT 'Node connection information',
  `receivers` text COMMENT 'receivers',
  `receivers_cc` text COMMENT 'cc',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `modify_by` varchar(255) DEFAULT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `process_definition_unique` (`name`,`project_id`),
  KEY `process_definition_index` (`project_id`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_definition
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_instance`;
CREATE TABLE `t_ds_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'process instance name',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `state` tinyint(4) DEFAULT NULL COMMENT 'process instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `recovery` tinyint(4) DEFAULT NULL COMMENT 'process instance failover flag：0:normal,1:failover instance',
  `start_time` datetime DEFAULT NULL COMMENT 'process instance start time',
  `end_time` datetime DEFAULT NULL COMMENT 'process instance end time',
  `run_times` int(11) DEFAULT NULL COMMENT 'process instance run times',
  `host` varchar(45) DEFAULT NULL COMMENT 'process instance host',
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
  `process_instance_json` longtext COMMENT 'process instance json(copy的process definition 的json)',
  `flag` tinyint(4) DEFAULT '1' COMMENT 'flag',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_sub_process` int(11) DEFAULT '0' COMMENT 'flag, whether the process is sub process',
  `executor_id` int(11) NOT NULL COMMENT 'executor id',
  `locations` text COMMENT 'Node location information',
  `connects` text COMMENT 'Node connection information',
  `history_cmd` text COMMENT 'history commands of process instance operation',
  `dependence_schedule_times` text COMMENT 'depend schedule fire time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  PRIMARY KEY (`id`),
  KEY `process_instance_index` (`process_definition_id`,`id`) USING BTREE,
  KEY `start_time_index` (`start_time`) USING BTREE
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
  `description` varchar(200) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
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
-- Records of t_ds_relation_udfs_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_user_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_user_alertgroup`;
CREATE TABLE `t_ds_relation_user_alertgroup` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_user_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_resources
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_resources`;
CREATE TABLE `t_ds_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `alias` varchar(64) DEFAULT NULL COMMENT 'alias',
  `file_name` varchar(64) DEFAULT NULL COMMENT 'file name',
  `description` varchar(256) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `type` tinyint(4) DEFAULT NULL COMMENT 'resource type,0:FILE，1:UDF',
  `size` bigint(20) DEFAULT NULL COMMENT 'resource size',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `pid` int(11) DEFAULT NULL,
  `full_name` varchar(64) DEFAULT NULL,
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
  `process_definition_id` int(11) NOT NULL COMMENT 'process definition id',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `crontab` varchar(256) NOT NULL COMMENT 'crontab description',
  `failure_strategy` tinyint(4) NOT NULL COMMENT 'failure strategy. 0:end,1:continue',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `release_state` tinyint(4) NOT NULL COMMENT 'release state. 0:offline,1:online ',
  `warning_type` tinyint(4) NOT NULL COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(256) DEFAULT '' COMMENT 'worker group id',
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
  `task_type` varchar(64) DEFAULT NULL COMMENT 'task type',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
  `task_json` longtext COMMENT 'task content json',
  `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
  `start_time` datetime DEFAULT NULL COMMENT 'task start time',
  `end_time` datetime DEFAULT NULL COMMENT 'task end time',
  `host` varchar(45) DEFAULT NULL COMMENT 'host of task running on',
  `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'task log path',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
  `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
  `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
  `app_link` varchar(255) DEFAULT NULL COMMENT 'yarn app id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `retry_interval` int(4) DEFAULT NULL COMMENT 'retry interval when task failed ',
  `max_retry_times` int(2) DEFAULT NULL COMMENT 'max retry times',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT 'task instance priority:0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `executor_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `process_instance_id` (`process_instance_id`) USING BTREE,
  KEY `task_instance_index` (`process_definition_id`,`process_instance_id`) USING BTREE,
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
  `tenant_name` varchar(64) DEFAULT NULL COMMENT 'tenant name',
  `description` varchar(256) DEFAULT NULL,
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_user
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
INSERT INTO `t_ds_version` VALUES ('1', '1.3.3');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO `t_ds_alertgroup` VALUES ('1', 'default admin warning group', '0', 'default admin warning group', '2018-11-29 10:20:39', '2018-11-29 10:20:39');

-- ----------------------------
-- Records of t_ds_relation_user_alertgroup
-- ----------------------------
INSERT INTO `t_ds_relation_user_alertgroup` VALUES ('1', '1', '1', '2018-11-29 10:22:33', '2018-11-29 10:22:33');

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO `t_ds_user` VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', '2018-03-27 15:48:50', '2018-10-24 17:40:22', null);
