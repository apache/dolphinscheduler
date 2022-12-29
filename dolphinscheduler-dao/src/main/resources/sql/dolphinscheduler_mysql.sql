# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.36)
# Database: dolphinscheduler
# Generation Time: 2022-12-28 08:28:23 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table QRTZ_BLOB_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table QRTZ_CALENDARS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `QRTZ_CALENDARS`;

CREATE TABLE `QRTZ_CALENDARS` (
                                  `SCHED_NAME` varchar(120) NOT NULL,
                                  `CALENDAR_NAME` varchar(200) NOT NULL,
                                  `CALENDAR` blob NOT NULL,
                                  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table QRTZ_CRON_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table QRTZ_FIRED_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table QRTZ_JOB_DETAILS
# ------------------------------------------------------------

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



# Dump of table QRTZ_LOCKS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `QRTZ_LOCKS`;

CREATE TABLE `QRTZ_LOCKS` (
                              `SCHED_NAME` varchar(120) NOT NULL,
                              `LOCK_NAME` varchar(40) NOT NULL,
                              PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `QRTZ_LOCKS` WRITE;
/*!40000 ALTER TABLE `QRTZ_LOCKS` DISABLE KEYS */;

INSERT INTO `QRTZ_LOCKS` (`SCHED_NAME`, `LOCK_NAME`)
VALUES
    ('DolphinScheduler','STATE_ACCESS'),
    ('DolphinScheduler','TRIGGER_ACCESS');

/*!40000 ALTER TABLE `QRTZ_LOCKS` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table QRTZ_PAUSED_TRIGGER_GRPS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;

CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
                                            `SCHED_NAME` varchar(120) NOT NULL,
                                            `TRIGGER_GROUP` varchar(200) NOT NULL,
                                            PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table QRTZ_SCHEDULER_STATE
# ------------------------------------------------------------

DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;

CREATE TABLE `QRTZ_SCHEDULER_STATE` (
                                        `SCHED_NAME` varchar(120) NOT NULL,
                                        `INSTANCE_NAME` varchar(200) NOT NULL,
                                        `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
                                        `CHECKIN_INTERVAL` bigint(13) NOT NULL,
                                        PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `QRTZ_SCHEDULER_STATE` WRITE;
/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` DISABLE KEYS */;

INSERT INTO `QRTZ_SCHEDULER_STATE` (`SCHED_NAME`, `INSTANCE_NAME`, `LAST_CHECKIN_TIME`, `CHECKIN_INTERVAL`)
VALUES
    ('DolphinScheduler','mxqdeMacBook-Pro.local1672197253973',1672216103538,5000);

/*!40000 ALTER TABLE `QRTZ_SCHEDULER_STATE` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table QRTZ_SIMPLE_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table QRTZ_SIMPROP_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table QRTZ_TRIGGERS
# ------------------------------------------------------------

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



# Dump of table t_ds_access_token
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_access_token`;

CREATE TABLE `t_ds_access_token` (
                                     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                     `user_id` int(11) DEFAULT NULL COMMENT 'user id',
                                     `token` varchar(64) DEFAULT NULL COMMENT 'token',
                                     `expire_time` datetime DEFAULT NULL COMMENT 'end time of token ',
                                     `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                     `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_alert
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_alert`;

CREATE TABLE `t_ds_alert` (
                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                              `title` varchar(64) DEFAULT NULL COMMENT 'title',
                              `sign` char(40) NOT NULL DEFAULT '' COMMENT 'sign=sha1(content)',
                              `content` text COMMENT 'Message content (can be email, can be SMS. Mail is stored in JSON map, and SMS is string)',
                              `alert_status` tinyint(4) DEFAULT '0' COMMENT '0:wait running,1:success,2:failed',
                              `warning_type` tinyint(4) DEFAULT '2' COMMENT '1 process is successfully, 2 process/task is failed',
                              `log` text COMMENT 'log',
                              `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
                              `create_time` datetime DEFAULT NULL COMMENT 'create time',
                              `update_time` datetime DEFAULT NULL COMMENT 'update time',
                              `project_code` bigint(20) DEFAULT NULL COMMENT 'project_code',
                              `process_definition_code` bigint(20) DEFAULT NULL COMMENT 'process_definition_code',
                              `process_instance_id` int(11) DEFAULT NULL COMMENT 'process_instance_id',
                              `alert_type` int(11) DEFAULT NULL COMMENT 'alert_type',
                              PRIMARY KEY (`id`),
                              KEY `idx_status` (`alert_status`) USING BTREE,
                              KEY `idx_sign` (`sign`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_alert` WRITE;
/*!40000 ALTER TABLE `t_ds_alert` DISABLE KEYS */;

INSERT INTO `t_ds_alert` (`id`, `title`, `sign`, `content`, `alert_status`, `warning_type`, `log`, `alertgroup_id`, `create_time`, `update_time`, `project_code`, `process_definition_code`, `process_instance_id`, `alert_type`)
VALUES
    (1,'start process success','df2f41b09006943007f5f596fe0af7d21f33d8c3','[{\"projectCode\":8024502754912,\"projectName\":\"test_project\",\"owner\":\"admin\",\"processId\":1,\"processDefinitionCode\":8025037222752,\"processName\":\"test_process-1-20221227154518363\",\"processType\":\"START_PROCESS\",\"processState\":\"SUCCESS\",\"recovery\":\"NO\",\"runTimes\":1,\"processStartTime\":\"2022-12-27 15:45:18\",\"processEndTime\":\"2022-12-27 15:45:19\",\"processHost\":\"10.10.31.24:5678\"}]',1,1,'[{\"status\":\"true\",\"message\":\"email send success.\"}]',2,'2022-12-27 01:45:20','2022-12-27 01:45:24',8024502754912,8025037222752,1,1),
    (2,NULL,'',NULL,1,2,'[{\"status\":\"true\",\"message\":\"no need to close alert\"}]',2,'2022-12-27 01:45:20','2022-12-27 01:45:29',8024502754912,8025037222752,1,8),
    (3,'start process success','6d25d9fe7ce134a9031abc725da5719dc958d78c','[{\"projectCode\":8024502754912,\"projectName\":\"test_project\",\"owner\":\"admin\",\"processId\":3,\"processDefinitionCode\":8025037222752,\"processName\":\"test_process-2-20221228101727312\",\"processType\":\"START_PROCESS\",\"processState\":\"SUCCESS\",\"recovery\":\"NO\",\"runTimes\":1,\"processStartTime\":\"2022-12-28 10:17:27\",\"processEndTime\":\"2022-12-28 10:17:29\",\"processHost\":\"192.168.228.1:5678\"}]',1,1,'[{\"status\":\"true\",\"message\":\"email send success.\"}]',2,'2022-12-27 20:17:29','2022-12-27 20:17:34',8024502754912,8025037222752,3,1),
    (4,NULL,'',NULL,1,2,'[{\"status\":\"true\",\"message\":\"no need to close alert\"}]',2,'2022-12-27 20:17:29','2022-12-27 20:17:39',8024502754912,8025037222752,3,8);

/*!40000 ALTER TABLE `t_ds_alert` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_alert_plugin_instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_alert_plugin_instance`;

CREATE TABLE `t_ds_alert_plugin_instance` (
                                              `id` int(11) NOT NULL AUTO_INCREMENT,
                                              `plugin_define_id` int(11) NOT NULL,
                                              `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
                                              `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                              `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              `instance_name` varchar(200) DEFAULT NULL COMMENT 'alert instance name',
                                              `tenant_id` int(11) NOT NULL DEFAULT '-1',
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_alert_plugin_instance` WRITE;
/*!40000 ALTER TABLE `t_ds_alert_plugin_instance` DISABLE KEYS */;

INSERT INTO `t_ds_alert_plugin_instance` (`id`, `plugin_define_id`, `plugin_instance_params`, `create_time`, `update_time`, `instance_name`, `tenant_id`)
VALUES
    (1,36,'{\"User\":\"moxq2@chinatelecom.cn\",\"WarningType\":\"all\",\"enableSmtpAuth\":\"true\",\"receiverCcs\":null,\"starttlsEnable\":\"true\",\"serverPort\":\"465\",\"serverHost\":\"smtp.chinatelecom.cn\",\"sslEnable\":\"true\",\"receivers\":\"moxq2@chinatelecom.cn\",\"sender\":\"moxq2@chinatelecom.cn\",\"smtpSslTrust\":\"*\",\"showType\":\"table\",\"Password\":\"7vA##k9n(JNZG5aZ\"}','2022-12-27 01:43:44','2022-12-27 01:43:44','test_email',1);

/*!40000 ALTER TABLE `t_ds_alert_plugin_instance` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_alert_send_status
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_alert_send_status`;

CREATE TABLE `t_ds_alert_send_status` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT,
                                          `alert_id` int(11) NOT NULL,
                                          `alert_plugin_instance_id` int(11) NOT NULL,
                                          `send_status` tinyint(4) DEFAULT '0',
                                          `log` text,
                                          `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `alert_send_status_unique` (`alert_id`,`alert_plugin_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_alertgroup
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_alertgroup`;

CREATE TABLE `t_ds_alertgroup` (
                                   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                   `alert_instance_ids` varchar(255) DEFAULT NULL COMMENT 'alert instance ids',
                                   `create_user_id` int(11) DEFAULT NULL COMMENT 'create user id',
                                   `group_name` varchar(255) DEFAULT NULL COMMENT 'group name',
                                   `description` varchar(255) DEFAULT NULL,
                                   `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                   `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                   `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `t_ds_alertgroup_name_un` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_alertgroup` WRITE;
/*!40000 ALTER TABLE `t_ds_alertgroup` DISABLE KEYS */;

INSERT INTO `t_ds_alertgroup` (`id`, `alert_instance_ids`, `create_user_id`, `group_name`, `description`, `create_time`, `update_time`, `tenant_id`)
VALUES
    (1,NULL,1,'default admin warning group','default admin warning group','2022-12-27 11:22:39','2022-12-27 11:22:39',0),
    (2,'1',2,'test_alert_group','test_alert_group','2022-12-27 01:44:52','2022-12-27 01:44:52',1);

/*!40000 ALTER TABLE `t_ds_alertgroup` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_audit_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_audit_log`;

CREATE TABLE `t_ds_audit_log` (
                                  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                  `user_id` int(11) NOT NULL COMMENT 'user id',
                                  `resource_type` int(11) NOT NULL COMMENT 'resource type',
                                  `operation` int(11) NOT NULL COMMENT 'operation',
                                  `time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
                                  `resource_id` int(11) DEFAULT NULL COMMENT 'resource id',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_cluster
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_cluster`;

CREATE TABLE `t_ds_cluster` (
                                `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                `code` bigint(20) DEFAULT NULL COMMENT 'encoding',
                                `name` varchar(100) NOT NULL COMMENT 'cluster name',
                                `config` text COMMENT 'this config contains many cluster variables config',
                                `description` text COMMENT 'the details',
                                `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
                                `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `cluster_name_unique` (`name`),
                                UNIQUE KEY `cluster_code_unique` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_command
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_command`;

CREATE TABLE `t_ds_command` (
                                `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                `command_type` tinyint(4) DEFAULT NULL COMMENT 'Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
                                `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
                                `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
                                `process_instance_id` int(11) DEFAULT '0' COMMENT 'process instance id',
                                `command_param` text COMMENT 'json command parameters',
                                `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'Node dependency type: 0 current node, 1 forward, 2 backward',
                                `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'Failed policy: 0 end, 1 continue',
                                `warning_type` tinyint(4) DEFAULT '0' COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
                                `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group',
                                `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
                                `start_time` datetime DEFAULT NULL COMMENT 'start time',
                                `executor_id` int(11) DEFAULT NULL COMMENT 'executor id',
                                `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                `process_instance_priority` int(11) DEFAULT '2' COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
                                `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group',
                                `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
                                `test_flag` tinyint(4) DEFAULT NULL COMMENT 'test flag：0 normal, 1 test run',
                                PRIMARY KEY (`id`),
                                KEY `priority_id_index` (`process_instance_priority`,`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_datasource
# ------------------------------------------------------------

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
                                   `test_flag` tinyint(4) DEFAULT NULL COMMENT 'test flag：0 normal, 1 testDataSource',
                                   `bind_test_id` int(11) DEFAULT NULL COMMENT 'bind testDataSource id',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `t_ds_datasource_name_un` (`name`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_dq_comparison_type
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_dq_comparison_type`;

CREATE TABLE `t_ds_dq_comparison_type` (
                                           `id` int(11) NOT NULL AUTO_INCREMENT,
                                           `type` varchar(100) NOT NULL,
                                           `execute_sql` text,
                                           `output_table` varchar(100) DEFAULT NULL,
                                           `name` varchar(100) DEFAULT NULL,
                                           `create_time` datetime DEFAULT NULL,
                                           `update_time` datetime DEFAULT NULL,
                                           `is_inner_source` tinyint(1) DEFAULT '0',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_dq_comparison_type` WRITE;
/*!40000 ALTER TABLE `t_ds_dq_comparison_type` DISABLE KEYS */;

INSERT INTO `t_ds_dq_comparison_type` (`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES
    (1,'FixValue',NULL,NULL,NULL,'2022-12-27 11:22:39','2022-12-27 11:22:39',0),
    (2,'DailyAvg','select round(avg(statistics_value),2) as day_avg from t_ds_dq_task_statistics_value where data_time >=date_trunc(\'DAY\', ${data_time}) and data_time < date_add(date_trunc(\'day\', ${data_time}),1) and unique_code = ${unique_code} and statistics_name = \'${statistics_name}\'','day_range','day_range.day_avg','2022-12-27 11:22:39','2022-12-27 11:22:39',1),
    (3,'WeeklyAvg','select round(avg(statistics_value),2) as week_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(\'WEEK\', ${data_time}) and data_time <date_trunc(\'day\', ${data_time}) and unique_code = ${unique_code} and statistics_name = \'${statistics_name}\'','week_range','week_range.week_avg','2022-12-27 11:22:39','2022-12-27 11:22:39',1),
    (4,'MonthlyAvg','select round(avg(statistics_value),2) as month_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(\'MONTH\', ${data_time}) and data_time <date_trunc(\'day\', ${data_time}) and unique_code = ${unique_code} and statistics_name = \'${statistics_name}\'','month_range','month_range.month_avg','2022-12-27 11:22:39','2022-12-27 11:22:39',1),
    (5,'Last7DayAvg','select round(avg(statistics_value),2) as last_7_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(\'day\', ${data_time}),-7) and  data_time <date_trunc(\'day\', ${data_time}) and unique_code = ${unique_code} and statistics_name = \'${statistics_name}\'','last_seven_days','last_seven_days.last_7_avg','2022-12-27 11:22:39','2022-12-27 11:22:39',1),
    (6,'Last30DayAvg','select round(avg(statistics_value),2) as last_30_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(\'day\', ${data_time}),-30) and  data_time < date_trunc(\'day\', ${data_time}) and unique_code = ${unique_code} and statistics_name = \'${statistics_name}\'','last_thirty_days','last_thirty_days.last_30_avg','2022-12-27 11:22:39','2022-12-27 11:22:39',1),
    (7,'SrcTableTotalRows','SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})','total_count','total_count.total','2022-12-27 11:22:39','2022-12-27 11:22:39',0),
    (8,'TargetTableTotalRows','SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})','total_count','total_count.total','2022-12-27 11:22:39','2022-12-27 11:22:39',0);

/*!40000 ALTER TABLE `t_ds_dq_comparison_type` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_dq_execute_result
# ------------------------------------------------------------

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
                                          `error_output_path` text,
                                          `create_time` datetime DEFAULT NULL,
                                          `update_time` datetime DEFAULT NULL,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_dq_rule
# ------------------------------------------------------------

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

LOCK TABLES `t_ds_dq_rule` WRITE;
/*!40000 ALTER TABLE `t_ds_dq_rule` DISABLE KEYS */;

INSERT INTO `t_ds_dq_rule` (`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES
    (1,'$t(null_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (2,'$t(custom_sql)',1,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (3,'$t(multi_table_accuracy)',2,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (4,'$t(multi_table_value_comparison)',3,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (5,'$t(field_length_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (6,'$t(uniqueness_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (7,'$t(regexp_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (8,'$t(timeliness_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (9,'$t(enumeration_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (10,'$t(table_count_check)',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39');

/*!40000 ALTER TABLE `t_ds_dq_rule` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_dq_rule_execute_sql
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_dq_rule_execute_sql`;

CREATE TABLE `t_ds_dq_rule_execute_sql` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `index` int(11) DEFAULT NULL,
                                            `sql` text,
                                            `table_alias` varchar(255) DEFAULT NULL,
                                            `type` int(11) DEFAULT NULL,
                                            `is_error_output_sql` tinyint(1) DEFAULT '0',
                                            `create_time` datetime DEFAULT NULL,
                                            `update_time` datetime DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_dq_rule_execute_sql` WRITE;
/*!40000 ALTER TABLE `t_ds_dq_rule_execute_sql` DISABLE KEYS */;

INSERT INTO `t_ds_dq_rule_execute_sql` (`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES
    (1,1,'SELECT COUNT(*) AS nulls FROM null_items','null_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (2,1,'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})','total_count',2,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (3,1,'SELECT COUNT(*) AS miss from miss_items','miss_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (4,1,'SELECT COUNT(*) AS valids FROM invalid_length_items','invalid_length_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (5,1,'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})','total_count',2,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (6,1,'SELECT ${src_field} FROM ${src_table} group by ${src_field} having count(*) > 1','duplicate_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (7,1,'SELECT COUNT(*) AS duplicates FROM duplicate_items','duplicate_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (8,1,'SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}','miss_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (9,1,'SELECT * FROM ${src_table} WHERE (${src_field} not regexp \'${regexp_pattern}\') AND (${src_filter}) ','regexp_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (10,1,'SELECT COUNT(*) AS regexps FROM regexp_items','regexp_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (11,1,'SELECT * FROM ${src_table} WHERE (to_unix_timestamp(${src_field}, \'${datetime_format}\')-to_unix_timestamp(\'${deadline}\', \'${datetime_format}\') <= 0) AND (to_unix_timestamp(${src_field}, \'${datetime_format}\')-to_unix_timestamp(\'${begin_time}\', \'${datetime_format}\') >= 0) AND (${src_filter}) ','timeliness_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (12,1,'SELECT COUNT(*) AS timeliness FROM timeliness_items','timeliness_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (13,1,'SELECT * FROM ${src_table} where (${src_field} not in ( ${enum_list} ) or ${src_field} is null) AND (${src_filter}) ','enum_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (14,1,'SELECT COUNT(*) AS enums FROM enum_items','enum_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (15,1,'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})','table_count',1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (16,1,'SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = \'\') AND (${src_filter})','null_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (17,1,'SELECT * FROM ${src_table} WHERE (length(${src_field}) ${logic_operator} ${field_length}) AND (${src_filter})','invalid_length_items',0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39');

/*!40000 ALTER TABLE `t_ds_dq_rule_execute_sql` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_dq_rule_input_entry
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;

CREATE TABLE `t_ds_dq_rule_input_entry` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `field` varchar(255) DEFAULT NULL,
                                            `type` varchar(255) DEFAULT NULL,
                                            `title` varchar(255) DEFAULT NULL,
                                            `value` varchar(255) DEFAULT NULL,
                                            `options` text,
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

LOCK TABLES `t_ds_dq_rule_input_entry` WRITE;
/*!40000 ALTER TABLE `t_ds_dq_rule_input_entry` DISABLE KEYS */;

INSERT INTO `t_ds_dq_rule_input_entry` (`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES
    (1,'src_connector_type','select','$t(src_connector_type)','','[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]','please select source connector type',2,2,0,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (2,'src_datasource_id','select','$t(src_datasource_id)','',NULL,'please select source datasource id',1,2,0,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (3,'src_table','select','$t(src_table)',NULL,NULL,'Please enter source table name',0,0,0,1,1,1,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (4,'src_filter','input','$t(src_filter)',NULL,NULL,'Please enter filter expression',0,3,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (5,'src_field','select','$t(src_field)',NULL,NULL,'Please enter column, only single column is supported',0,0,0,1,1,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (6,'statistics_name','input','$t(statistics_name)',NULL,NULL,'Please enter statistics name, the alias in statistics execute sql',0,0,1,0,0,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (7,'check_type','select','$t(check_type)','0','[{\"label\":\"Expected - Actual\",\"value\":\"0\"},{\"label\":\"Actual - Expected\",\"value\":\"1\"},{\"label\":\"Actual / Expected\",\"value\":\"2\"},{\"label\":\"(Expected - Actual) / Expected\",\"value\":\"3\"}]','please select check type',0,0,3,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (8,'operator','select','$t(operator)','0','[{\"label\":\"=\",\"value\":\"0\"},{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},{\"label\":\"!=\",\"value\":\"5\"}]','please select operator',0,0,3,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (9,'threshold','input','$t(threshold)',NULL,NULL,'Please enter threshold, number is needed',0,2,3,1,1,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (10,'failure_strategy','select','$t(failure_strategy)','0','[{\"label\":\"Alert\",\"value\":\"0\"},{\"label\":\"Block\",\"value\":\"1\"}]','please select failure strategy',0,0,3,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (11,'target_connector_type','select','$t(target_connector_type)','','[{\"label\":\"HIVE\",\"value\":\"HIVE\"},{\"label\":\"JDBC\",\"value\":\"JDBC\"}]','Please select target connector type',2,0,0,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (12,'target_datasource_id','select','$t(target_datasource_id)','',NULL,'Please select target datasource',1,2,0,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (13,'target_table','select','$t(target_table)',NULL,NULL,'Please enter target table',0,0,0,1,1,1,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (14,'target_filter','input','$t(target_filter)',NULL,NULL,'Please enter target filter expression',0,3,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (15,'mapping_columns','group','$t(mapping_columns)',NULL,'[{\"field\":\"src_field\",\"props\":{\"placeholder\":\"Please input src field\",\"rows\":0,\"disabled\":false,\"size\":\"small\"},\"type\":\"input\",\"title\":\"src_field\"},{\"field\":\"operator\",\"props\":{\"placeholder\":\"Please input operator\",\"rows\":0,\"disabled\":false,\"size\":\"small\"},\"type\":\"input\",\"title\":\"operator\"},{\"field\":\"target_field\",\"props\":{\"placeholder\":\"Please input target field\",\"rows\":0,\"disabled\":false,\"size\":\"small\"},\"type\":\"input\",\"title\":\"target_field\"}]','please enter mapping columns',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (16,'statistics_execute_sql','textarea','$t(statistics_execute_sql)',NULL,NULL,'Please enter statistics execute sql',0,3,0,1,1,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (17,'comparison_name','input','$t(comparison_name)',NULL,NULL,'Please enter comparison name, the alias in comparison execute sql',0,0,0,0,0,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (18,'comparison_execute_sql','textarea','$t(comparison_execute_sql)',NULL,NULL,'Please enter comparison execute sql',0,3,0,1,1,0,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (19,'comparison_type','select','$t(comparison_type)','',NULL,'Please enter comparison title',3,0,2,1,0,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (20,'writer_connector_type','select','$t(writer_connector_type)','','[{\"label\":\"MYSQL\",\"value\":\"0\"},{\"label\":\"POSTGRESQL\",\"value\":\"1\"}]','please select writer connector type',0,2,0,1,1,1,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (21,'writer_datasource_id','select','$t(writer_datasource_id)','',NULL,'please select writer datasource id',1,2,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (22,'target_field','select','$t(target_field)',NULL,NULL,'Please enter column, only single column is supported',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (23,'field_length','input','$t(field_length)',NULL,NULL,'Please enter length limit',0,3,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (24,'logic_operator','select','$t(logic_operator)','=','[{\"label\":\"=\",\"value\":\"=\"},{\"label\":\"<\",\"value\":\"<\"},{\"label\":\"<=\",\"value\":\"<=\"},{\"label\":\">\",\"value\":\">\"},{\"label\":\">=\",\"value\":\">=\"},{\"label\":\"<>\",\"value\":\"<>\"}]','please select logic operator',0,0,3,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (25,'regexp_pattern','input','$t(regexp_pattern)',NULL,NULL,'Please enter regexp pattern',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (26,'deadline','input','$t(deadline)',NULL,NULL,'Please enter deadline',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (27,'datetime_format','input','$t(datetime_format)',NULL,NULL,'Please enter datetime format',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (28,'enum_list','input','$t(enum_list)',NULL,NULL,'Please enter enumeration',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (29,'begin_time','input','$t(begin_time)',NULL,NULL,'Please enter begin time',0,0,0,1,1,0,0,'2022-12-27 11:22:39','2022-12-27 11:22:39');

/*!40000 ALTER TABLE `t_ds_dq_rule_input_entry` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_dq_task_statistics_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_dq_task_statistics_value`;

CREATE TABLE `t_ds_dq_task_statistics_value` (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                                 `process_definition_id` int(11) DEFAULT NULL,
                                                 `task_instance_id` int(11) DEFAULT NULL,
                                                 `rule_id` int(11) NOT NULL,
                                                 `unique_code` varchar(255) DEFAULT NULL,
                                                 `statistics_name` varchar(255) DEFAULT NULL,
                                                 `statistics_value` double DEFAULT NULL,
                                                 `data_time` datetime DEFAULT NULL,
                                                 `create_time` datetime DEFAULT NULL,
                                                 `update_time` datetime DEFAULT NULL,
                                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_environment
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_environment`;

CREATE TABLE `t_ds_environment` (
                                    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                    `code` bigint(20) DEFAULT NULL COMMENT 'encoding',
                                    `name` varchar(100) NOT NULL COMMENT 'environment name',
                                    `config` text COMMENT 'this config contains many environment variables config',
                                    `description` text COMMENT 'the details',
                                    `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
                                    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `environment_name_unique` (`name`),
                                    UNIQUE KEY `environment_code_unique` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_environment_worker_group_relation
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_error_command
# ------------------------------------------------------------

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
                                      `process_instance_priority` int(11) DEFAULT '2' COMMENT 'process instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
                                      `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group',
                                      `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                      `message` text COMMENT 'message',
                                      `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag: 0 normal, 1 dry run',
                                      `test_flag` tinyint(4) DEFAULT NULL COMMENT 'test flag：0 normal, 1 test run',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;



# Dump of table t_ds_fav_task
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_fav_task`;

CREATE TABLE `t_ds_fav_task` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                 `task_type` varchar(64) NOT NULL COMMENT 'favorite task type name',
                                 `user_id` int(11) NOT NULL COMMENT 'user id',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_k8s
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_k8s`;

CREATE TABLE `t_ds_k8s` (
                            `id` int(11) NOT NULL AUTO_INCREMENT,
                            `k8s_name` varchar(100) DEFAULT NULL,
                            `k8s_config` text,
                            `create_time` datetime DEFAULT NULL COMMENT 'create time',
                            `update_time` datetime DEFAULT NULL COMMENT 'update time',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_k8s_namespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_k8s_namespace`;

CREATE TABLE `t_ds_k8s_namespace` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT,
                                      `code` bigint(20) NOT NULL DEFAULT '0',
                                      `limits_memory` int(11) DEFAULT NULL,
                                      `namespace` varchar(100) DEFAULT NULL,
                                      `user_id` int(11) DEFAULT NULL,
                                      `pod_replicas` int(11) DEFAULT NULL,
                                      `pod_request_cpu` decimal(14,3) DEFAULT NULL,
                                      `pod_request_memory` int(11) DEFAULT NULL,
                                      `limits_cpu` decimal(14,3) DEFAULT NULL,
                                      `cluster_code` bigint(20) NOT NULL DEFAULT '0',
                                      `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                      `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `k8s_namespace_unique` (`namespace`,`cluster_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_plugin_define
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_plugin_define`;

CREATE TABLE `t_ds_plugin_define` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT,
                                      `plugin_name` varchar(100) NOT NULL COMMENT 'the name of plugin eg: email',
                                      `plugin_type` varchar(100) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
                                      `plugin_params` text COMMENT 'plugin params',
                                      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_plugin_define` WRITE;
/*!40000 ALTER TABLE `t_ds_plugin_define` DISABLE KEYS */;

INSERT INTO `t_ds_plugin_define` (`id`, `plugin_name`, `plugin_type`, `plugin_params`, `create_time`, `update_time`)
VALUES
    (2,'JAVA','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (3,'JUPYTER','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (4,'SPARK','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (5,'FLINK_STREAM','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (6,'PYTHON','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (7,'DATASYNC','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (8,'CHUNJUN','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (9,'PIGEON','task','[{\"props\":null,\"field\":\"targetJobName\",\"name\":\"targetJobName\",\"type\":\"input\",\"title\":\"targetJobName\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (10,'PROCEDURE','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (11,'SHELL','task','[{\"props\":null,\"field\":\"name\",\"name\":\"$t(\'Node name\')\",\"type\":\"input\",\"title\":\"$t(\'Node name\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"runFlag\",\"name\":\"RUN_FLAG\",\"type\":\"radio\",\"title\":\"RUN_FLAG\",\"value\":null,\"validate\":null,\"emit\":null,\"options\":[{\"label\":\"NORMAL\",\"value\":\"NORMAL\",\"disabled\":false},{\"label\":\"FORBIDDEN\",\"value\":\"FORBIDDEN\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (12,'MR','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (13,'SQOOP','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (14,'PYTORCH','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (15,'K8S','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (16,'SEATUNNEL','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (17,'SAGEMAKER','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (18,'HTTP','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (19,'EMR','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (20,'DMS','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (21,'DATA_QUALITY','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (22,'KUBEFLOW','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (23,'SQL','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (24,'DVC','task','[{\"props\":null,\"field\":\"name\",\"name\":\"$t(\'Node name\')\",\"type\":\"input\",\"title\":\"$t(\'Node name\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"runFlag\",\"name\":\"RUN_FLAG\",\"type\":\"radio\",\"title\":\"RUN_FLAG\",\"value\":null,\"validate\":null,\"emit\":null,\"options\":[{\"label\":\"NORMAL\",\"value\":\"NORMAL\",\"disabled\":false},{\"label\":\"FORBIDDEN\",\"value\":\"FORBIDDEN\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (25,'DATAX','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (26,'ZEPPELIN','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (27,'DINKY','task','[]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (28,'MLFLOW','task','[{\"props\":null,\"field\":\"name\",\"name\":\"$t(\'Node name\')\",\"type\":\"input\",\"title\":\"$t(\'Node name\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"runFlag\",\"name\":\"RUN_FLAG\",\"type\":\"radio\",\"title\":\"RUN_FLAG\",\"value\":null,\"validate\":null,\"emit\":null,\"options\":[{\"label\":\"NORMAL\",\"value\":\"NORMAL\",\"disabled\":false},{\"label\":\"FORBIDDEN\",\"value\":\"FORBIDDEN\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (29,'OPENMLDB','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (30,'LINKIS','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (31,'FLINK','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (32,'HIVECLI','task','null','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (33,'Script','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入调用脚本时传入的自定义参数\",\"size\":\"small\"},\"field\":\"userParams\",\"name\":\"$t(\'userParams\')\",\"type\":\"input\",\"title\":\"$t(\'userParams\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入alert-server机器的脚本的绝对路径，并确保文件有权接入\",\"size\":\"small\"},\"field\":\"path\",\"name\":\"$t(\'scriptPath\')\",\"type\":\"input\",\"title\":\"$t(\'scriptPath\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"type\",\"name\":\"$t(\'scriptType\')\",\"type\":\"radio\",\"title\":\"$t(\'scriptType\')\",\"value\":\"SHELL\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"SHELL\",\"value\":\"SHELL\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (34,'Telegram','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入webhook的url\",\"size\":\"small\"},\"field\":\"webHook\",\"name\":\"$t(\'webHook\')\",\"type\":\"input\",\"title\":\"$t(\'webHook\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入bot的接入token\",\"size\":\"small\"},\"field\":\"botToken\",\"name\":\"botToken\",\"type\":\"input\",\"title\":\"botToken\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入telegram的频道chat id\",\"size\":\"small\"},\"field\":\"chatId\",\"name\":\"chatId\",\"type\":\"input\",\"title\":\"chatId\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"field\":\"parseMode\",\"name\":\"parseMode\",\"props\":{\"disabled\":null,\"placeholder\":null,\"size\":\"small\"},\"type\":\"select\",\"title\":\"parseMode\",\"value\":\"Txt\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"Txt\",\"value\":\"Txt\",\"disabled\":false},{\"label\":\"Markdown\",\"value\":\"Markdown\",\"disabled\":false},{\"label\":\"MarkdownV2\",\"value\":\"MarkdownV2\",\"disabled\":false},{\"label\":\"Html\",\"value\":\"Html\",\"disabled\":false}]},{\"props\":null,\"field\":\"IsEnableProxy\",\"name\":\"$t(\'isEnableProxy\')\",\"type\":\"radio\",\"title\":\"$t(\'isEnableProxy\')\",\"value\":\"false\",\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"Proxy\",\"name\":\"$t(\'proxy\')\",\"type\":\"input\",\"title\":\"$t(\'proxy\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Port\",\"name\":\"$t(\'port\')\",\"type\":\"input\",\"title\":\"$t(\'port\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"User\",\"name\":\"$t(\'user\')\",\"type\":\"input\",\"title\":\"$t(\'user\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"field\":\"Password\",\"name\":\"$t(\'password\')\",\"props\":{\"disabled\":null,\"placeholder\":\"if enable use authentication, you need input password\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t(\'password\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (35,'WeChat','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入corp id\",\"size\":\"small\"},\"field\":\"corpId\",\"name\":\"$t(\'corpId\')\",\"type\":\"input\",\"title\":\"$t(\'corpId\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入secret\",\"size\":\"small\"},\"field\":\"secret\",\"name\":\"$t(\'secret\')\",\"type\":\"input\",\"title\":\"$t(\'secret\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"使用`|`来分割userId或使用`@all`来提到所有人\",\"size\":\"small\"},\"field\":\"users\",\"name\":\"$t(\'users\')\",\"type\":\"input\",\"title\":\"$t(\'users\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入agent id或chat id\",\"size\":\"small\"},\"field\":\"agentId/chatId\",\"name\":\"$t(\'agentId/chatId\')\",\"type\":\"input\",\"title\":\"$t(\'agentId/chatId\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"sendType\",\"name\":\"send.type\",\"type\":\"radio\",\"title\":\"send.type\",\"value\":\"APP/应用\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"APP/应用\",\"value\":\"APP/应用\",\"disabled\":false},{\"label\":\"GROUP CHAT/群聊\",\"value\":\"GROUP CHAT/群聊\",\"disabled\":false}]},{\"props\":null,\"field\":\"showType\",\"name\":\"$t(\'showType\')\",\"type\":\"radio\",\"title\":\"$t(\'showType\')\",\"value\":\"markdown\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"markdown\",\"value\":\"markdown\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (36,'Email','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入收件人\",\"size\":\"small\"},\"field\":\"receivers\",\"name\":\"$t(\'receivers\')\",\"type\":\"input\",\"title\":\"$t(\'receivers\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"receiverCcs\",\"name\":\"$t(\'receiverCcs\')\",\"type\":\"input\",\"title\":\"$t(\'receiverCcs\')\",\"value\":null,\"validate\":null,\"emit\":null},{\"props\":null,\"field\":\"serverHost\",\"name\":\"mail.smtp.host\",\"type\":\"input\",\"title\":\"mail.smtp.host\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"serverPort\",\"name\":\"mail.smtp.port\",\"type\":\"input\",\"title\":\"mail.smtp.port\",\"value\":\"25\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"sender\",\"name\":\"$t(\'mailSender\')\",\"type\":\"input\",\"title\":\"$t(\'mailSender\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"enableSmtpAuth\",\"name\":\"mail.smtp.auth\",\"type\":\"radio\",\"title\":\"mail.smtp.auth\",\"value\":\"true\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"如果开启鉴权校验，则需要输入账号\",\"size\":\"small\"},\"field\":\"User\",\"name\":\"$t(\'mailUser\')\",\"type\":\"input\",\"title\":\"$t(\'mailUser\')\",\"value\":null,\"validate\":null,\"emit\":null},{\"field\":\"Password\",\"name\":\"$t(\'mailPasswd\')\",\"props\":{\"disabled\":null,\"placeholder\":\"如果开启鉴权校验，则需要输入密码\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t(\'mailPasswd\')\",\"value\":null,\"validate\":null,\"emit\":null},{\"props\":null,\"field\":\"starttlsEnable\",\"name\":\"mail.smtp.starttls.enable\",\"type\":\"radio\",\"title\":\"mail.smtp.starttls.enable\",\"value\":\"false\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"sslEnable\",\"name\":\"mail.smtp.ssl.enable\",\"type\":\"radio\",\"title\":\"mail.smtp.ssl.enable\",\"value\":\"false\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"smtpSslTrust\",\"name\":\"mail.smtp.ssl.trust\",\"type\":\"input\",\"title\":\"mail.smtp.ssl.trust\",\"value\":\"*\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"showType\",\"name\":\"$t(\'showType\')\",\"type\":\"radio\",\"title\":\"$t(\'showType\')\",\"value\":\"table\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"table\",\"value\":\"table\",\"disabled\":false},{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},{\"label\":\"attachment\",\"value\":\"attachment\",\"disabled\":false},{\"label\":\"table attachment\",\"value\":\"table attachment\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (37,'Slack','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入webhook的url\",\"size\":\"small\"},\"field\":\"webHook\",\"name\":\"$t(\'webhook\')\",\"type\":\"input\",\"title\":\"$t(\'webhook\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入bot的名称\",\"size\":\"small\"},\"field\":\"username\",\"name\":\"$t(\'Username\')\",\"type\":\"input\",\"title\":\"$t(\'Username\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (38,'Feishu','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":null,\"field\":\"WebHook\",\"name\":\"$t(\'webhook\')\",\"type\":\"input\",\"title\":\"$t(\'webhook\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"IsEnableProxy\",\"name\":\"$t(\'isEnableProxy\')\",\"type\":\"radio\",\"title\":\"$t(\'isEnableProxy\')\",\"value\":\"true\",\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"Proxy\",\"name\":\"$t(\'proxy\')\",\"type\":\"input\",\"title\":\"$t(\'proxy\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Port\",\"name\":\"$t(\'port\')\",\"type\":\"input\",\"title\":\"$t(\'port\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"User\",\"name\":\"$t(\'user\')\",\"type\":\"input\",\"title\":\"$t(\'user\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"field\":\"Password\",\"name\":\"$t(\'password\')\",\"props\":{\"disabled\":null,\"placeholder\":\"如果开启鉴权校验，则需要输入密码\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t(\'password\')\",\"value\":null,\"validate\":null,\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (39,'Http','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入请求的URL\",\"size\":\"small\"},\"field\":\"url\",\"name\":\"$t(\'url\')\",\"type\":\"input\",\"title\":\"$t(\'url\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入HTTP请求类型POST或GET\",\"size\":\"small\"},\"field\":\"requestType\",\"name\":\"$t(\'requestType\')\",\"type\":\"input\",\"title\":\"$t(\'requestType\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入JSON格式的请求头\",\"size\":\"small\"},\"field\":\"headerParams\",\"name\":\"$t(\'headerParams\')\",\"type\":\"input\",\"title\":\"$t(\'headerParams\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入JSON格式的请求体\",\"size\":\"small\"},\"field\":\"bodyParams\",\"name\":\"$t(\'bodyParams\')\",\"type\":\"input\",\"title\":\"$t(\'bodyParams\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入告警信息的内容字段名称\",\"size\":\"small\"},\"field\":\"contentField\",\"name\":\"$t(\'contentField\')\",\"type\":\"input\",\"title\":\"$t(\'contentField\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (40,'DingTalk','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":null,\"field\":\"WebHook\",\"name\":\"$t(\'webhook\')\",\"type\":\"input\",\"title\":\"$t(\'webhook\')\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Keyword\",\"name\":\"$t(\'keyword\')\",\"type\":\"input\",\"title\":\"$t(\'keyword\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Secret\",\"name\":\"$t(\'secret\')\",\"type\":\"input\",\"title\":\"$t(\'secret\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"MsgType\",\"name\":\"$t(\'msgType\')\",\"type\":\"radio\",\"title\":\"$t(\'msgType\')\",\"value\":\"text\",\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"text\",\"value\":\"text\",\"disabled\":false},{\"label\":\"markdown\",\"value\":\"markdown\",\"disabled\":false}]},{\"props\":null,\"field\":\"AtMobiles\",\"name\":\"$t(\'atMobiles\')\",\"type\":\"input\",\"title\":\"$t(\'atMobiles\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"AtUserIds\",\"name\":\"$t(\'atUserIds\')\",\"type\":\"input\",\"title\":\"$t(\'atUserIds\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"IsAtAll\",\"name\":\"$t(\'isAtAll\')\",\"type\":\"radio\",\"title\":\"$t(\'isAtAll\')\",\"value\":\"false\",\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"IsEnableProxy\",\"name\":\"$t(\'isEnableProxy\')\",\"type\":\"radio\",\"title\":\"$t(\'isEnableProxy\')\",\"value\":\"false\",\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"YES\",\"value\":\"true\",\"disabled\":false},{\"label\":\"NO\",\"value\":\"false\",\"disabled\":false}]},{\"props\":null,\"field\":\"Proxy\",\"name\":\"$t(\'proxy\')\",\"type\":\"input\",\"title\":\"$t(\'proxy\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Port\",\"name\":\"$t(\'port\')\",\"type\":\"input\",\"title\":\"$t(\'port\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"User\",\"name\":\"$t(\'user\')\",\"type\":\"input\",\"title\":\"$t(\'user\')\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"field\":\"Password\",\"name\":\"$t(\'password\')\",\"props\":{\"disabled\":null,\"placeholder\":\"如果开启鉴权校验，则需要输入密码\",\"size\":\"small\"},\"type\":\"input\",\"title\":\"$t(\'password\')\",\"value\":null,\"validate\":null,\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (41,'WebexTeams','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入bot的接入token\",\"size\":\"small\"},\"field\":\"BotAccessToken\",\"name\":\"botAccessToken\",\"type\":\"input\",\"title\":\"botAccessToken\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入告警信息发送的room ID\",\"size\":\"small\"},\"field\":\"RoomId\",\"name\":\"roomId\",\"type\":\"input\",\"title\":\"roomId\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入告警信息接收人的person ID\",\"size\":\"small\"},\"field\":\"ToPersonId\",\"name\":\"toPersonId\",\"type\":\"input\",\"title\":\"toPersonId\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"请输入告警信息接收人的email地址\",\"size\":\"small\"},\"field\":\"ToPersonEmail\",\"name\":\"toPersonEmail\",\"type\":\"input\",\"title\":\"toPersonEmail\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":{\"disabled\":null,\"type\":null,\"maxlength\":null,\"minlength\":null,\"clearable\":null,\"prefixIcon\":null,\"suffixIcon\":null,\"rows\":null,\"autosize\":null,\"autocomplete\":null,\"name\":null,\"readonly\":null,\"max\":null,\"min\":null,\"step\":null,\"resize\":null,\"autofocus\":null,\"form\":null,\"label\":null,\"tabindex\":null,\"validateEvent\":null,\"showPassword\":null,\"placeholder\":\"使用 `, `来分割多个email，来指出在房间中要@的人\",\"size\":\"small\"},\"field\":\"AtSomeoneInRoom\",\"name\":\"atSomeoneInRoom\",\"type\":\"input\",\"title\":\"atSomeoneInRoom\",\"value\":null,\"validate\":[{\"required\":false,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null},{\"props\":null,\"field\":\"Destination\",\"name\":\"destination\",\"type\":\"radio\",\"title\":\"destination\",\"value\":\"roomId\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"roomId\",\"value\":\"roomId\",\"disabled\":false},{\"label\":\"personEmail\",\"value\":\"personEmail\",\"disabled\":false},{\"label\":\"personId\",\"value\":\"personId\",\"disabled\":false}]}]','2022-12-26 21:26:13','2022-12-26 21:26:13'),
    (42,'PagerDuty','alert','[{\"props\":null,\"field\":\"WarningType\",\"name\":\"warningType\",\"type\":\"radio\",\"title\":\"warningType\",\"value\":\"all\",\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null,\"options\":[{\"label\":\"success\",\"value\":\"success\",\"disabled\":false},{\"label\":\"failure\",\"value\":\"failure\",\"disabled\":false},{\"label\":\"all\",\"value\":\"all\",\"disabled\":false}]},{\"props\":null,\"field\":\"IntegrationKey\",\"name\":\"integrationKey\",\"type\":\"input\",\"title\":\"integrationKey\",\"value\":null,\"validate\":[{\"required\":true,\"message\":null,\"type\":\"string\",\"trigger\":\"blur\",\"min\":null,\"max\":null}],\"emit\":null}]','2022-12-26 21:26:13','2022-12-26 21:26:13');

/*!40000 ALTER TABLE `t_ds_plugin_define` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_process_definition
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_process_definition` WRITE;
/*!40000 ALTER TABLE `t_ds_process_definition` DISABLE KEYS */;

INSERT INTO `t_ds_process_definition` (`id`, `code`, `name`, `version`, `description`, `project_code`, `release_state`, `user_id`, `global_params`, `flag`, `locations`, `warning_group_id`, `timeout`, `tenant_id`, `execution_type`, `create_time`, `update_time`)
VALUES
    (1,8025037222752,'test_process',2,'testprocess',8024502754912,0,2,'[]',1,'[{\"taskCode\":8024963455584,\"x\":89,\"y\":61},{\"taskCode\":8033675329888,\"x\":181,\"y\":190}]',NULL,0,1,0,'2022-12-27 01:26:43','2022-12-27 20:12:21'),
    (3,8035390562272,'test_process',1,'',8035382740320,0,4,'[]',1,'[{\"taskCode\":8035383664480,\"x\":89,\"y\":51}]',NULL,0,2,0,'2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_process_definition` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_process_definition_log
# ------------------------------------------------------------

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
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uniq_idx_code_version` (`code`,`version`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_process_definition_log` WRITE;
/*!40000 ALTER TABLE `t_ds_process_definition_log` DISABLE KEYS */;

INSERT INTO `t_ds_process_definition_log` (`id`, `code`, `name`, `version`, `description`, `project_code`, `release_state`, `user_id`, `global_params`, `flag`, `locations`, `warning_group_id`, `timeout`, `tenant_id`, `execution_type`, `operator`, `operate_time`, `create_time`, `update_time`)
VALUES
    (1,8025037222752,'test_process',1,'testprocess',8024502754912,0,2,'[]',1,'[{\"taskCode\":8024963455584,\"x\":89,\"y\":61}]',NULL,0,1,0,2,'2022-12-27 01:26:43','2022-12-27 01:26:43','2022-12-27 01:26:43'),
    (2,8025037222752,'test_process',2,'testprocess',8024502754912,0,2,'[]',1,'[{\"taskCode\":8024963455584,\"x\":89,\"y\":61},{\"taskCode\":8033675329888,\"x\":181,\"y\":190}]',NULL,0,1,0,3,'2022-12-27 20:12:21','2022-12-27 01:26:43','2022-12-27 20:12:21'),
    (3,8035390562272,'test_process',1,'',8035382740320,0,4,'[]',1,'[{\"taskCode\":8035383664480,\"x\":89,\"y\":51}]',NULL,0,2,0,4,'2022-12-27 23:54:49','2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_process_definition_log` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_process_instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_process_instance`;

CREATE TABLE `t_ds_process_instance` (
                                         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                         `name` varchar(255) DEFAULT NULL COMMENT 'process instance name',
                                         `process_definition_code` bigint(20) NOT NULL COMMENT 'process definition code',
                                         `process_definition_version` int(11) DEFAULT '0' COMMENT 'process definition version',
                                         `state` tinyint(4) DEFAULT NULL COMMENT 'process instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
                                         `state_history` text COMMENT 'state history desc',
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
                                         `process_instance_priority` int(11) DEFAULT '2' COMMENT 'process instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
                                         `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
                                         `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                         `timeout` int(11) DEFAULT '0' COMMENT 'time out',
                                         `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
                                         `var_pool` longtext COMMENT 'var_pool',
                                         `dry_run` tinyint(4) DEFAULT '0' COMMENT 'dry run flag：0 normal, 1 dry run',
                                         `next_process_instance_id` int(11) DEFAULT '0' COMMENT 'serial queue next processInstanceId',
                                         `restart_time` datetime DEFAULT NULL COMMENT 'process instance restart time',
                                         `test_flag` tinyint(4) DEFAULT NULL COMMENT 'test flag：0 normal, 1 test run',
                                         PRIMARY KEY (`id`),
                                         KEY `process_instance_index` (`process_definition_code`,`id`) USING BTREE,
                                         KEY `start_time_index` (`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_process_instance` WRITE;
/*!40000 ALTER TABLE `t_ds_process_instance` DISABLE KEYS */;

INSERT INTO `t_ds_process_instance` (`id`, `name`, `process_definition_code`, `process_definition_version`, `state`, `state_history`, `recovery`, `start_time`, `end_time`, `run_times`, `host`, `command_type`, `command_param`, `task_depend_type`, `max_try_times`, `failure_strategy`, `warning_type`, `warning_group_id`, `schedule_time`, `command_start_time`, `global_params`, `flag`, `update_time`, `is_sub_process`, `executor_id`, `history_cmd`, `process_instance_priority`, `worker_group`, `environment_code`, `timeout`, `tenant_id`, `var_pool`, `dry_run`, `next_process_instance_id`, `restart_time`, `test_flag`)
VALUES
    (1,'test_process-1-20221227154518363',8025037222752,1,7,'[{\"time\":\"2022-12-27 15:45:18\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"init running\"},{\"time\":\"2022-12-27 15:45:18\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"start a new process\"},{\"time\":\"2022-12-27 15:45:19\",\"state\":\"SUCCESS\",\"desc\":\"update by workflow executor\"}]',0,'2022-12-27 01:45:18','2022-12-27 01:45:20',1,'10.10.31.24:5678',0,'{}',2,0,1,3,2,NULL,'2022-12-27 01:45:18',NULL,1,'2022-12-27 15:45:19',0,2,'START_PROCESS',2,'default',-1,0,1,'[]',0,0,'2022-12-27 01:45:18',0),
    (2,'test_process-2-20221228101235743',8025037222752,2,7,'[{\"time\":\"2022-12-28 10:12:35\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"init running\"},{\"time\":\"2022-12-28 10:12:35\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"start a new process\"},{\"time\":\"2022-12-28 10:12:38\",\"state\":\"SUCCESS\",\"desc\":\"update by workflow executor\"}]',0,'2022-12-27 20:12:36','2022-12-27 20:12:38',1,'192.168.228.1:5678',0,'{}',2,0,1,3,0,NULL,'2022-12-27 20:12:35',NULL,1,'2022-12-28 10:12:38',0,3,'START_PROCESS',2,'default',-1,0,1,'[]',0,0,'2022-12-27 20:12:36',0),
    (3,'test_process-2-20221228101727312',8025037222752,2,7,'[{\"time\":\"2022-12-28 10:17:27\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"init running\"},{\"time\":\"2022-12-28 10:17:27\",\"state\":\"RUNNING_EXECUTION\",\"desc\":\"start a new process\"},{\"time\":\"2022-12-28 10:17:29\",\"state\":\"SUCCESS\",\"desc\":\"update by workflow executor\"}]',0,'2022-12-27 20:17:27','2022-12-27 20:17:29',1,'192.168.228.1:5678',0,'{}',2,0,1,3,2,NULL,'2022-12-27 20:17:26',NULL,1,'2022-12-28 10:17:29',0,2,'START_PROCESS',2,'test_work_group',-1,0,1,'[]',0,0,'2022-12-27 20:17:27',0);

/*!40000 ALTER TABLE `t_ds_process_instance` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_process_task_relation
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_process_task_relation` WRITE;
/*!40000 ALTER TABLE `t_ds_process_task_relation` DISABLE KEYS */;

INSERT INTO `t_ds_process_task_relation` (`id`, `name`, `project_code`, `process_definition_code`, `process_definition_version`, `pre_task_code`, `pre_task_version`, `post_task_code`, `post_task_version`, `condition_type`, `condition_params`, `create_time`, `update_time`)
VALUES
    (2,'',8024502754912,8025037222752,2,0,0,8024963455584,1,0,'{}','2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (3,'',8024502754912,8025037222752,2,8024963455584,1,8033675329888,1,0,'{}','2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (4,'',8035382740320,8035390562272,1,0,0,8035383664480,1,0,'{}','2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_process_task_relation` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_process_task_relation_log
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_process_task_relation_log` WRITE;
/*!40000 ALTER TABLE `t_ds_process_task_relation_log` DISABLE KEYS */;

INSERT INTO `t_ds_process_task_relation_log` (`id`, `name`, `project_code`, `process_definition_code`, `process_definition_version`, `pre_task_code`, `pre_task_version`, `post_task_code`, `post_task_version`, `condition_type`, `condition_params`, `operator`, `operate_time`, `create_time`, `update_time`)
VALUES
    (1,'',8024502754912,8025037222752,1,0,0,8024963455584,1,0,'{}',2,'2022-12-27 01:26:43','2022-12-27 01:26:43','2022-12-27 01:26:43'),
    (2,'',8024502754912,8025037222752,2,0,0,8024963455584,1,0,'{}',3,'2022-12-27 20:12:21','2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (3,'',8024502754912,8025037222752,2,8024963455584,1,8033675329888,1,0,'{}',3,'2022-12-27 20:12:21','2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (4,'',8035382740320,8035390562272,1,0,0,8035383664480,1,0,'{}',4,'2022-12-27 23:54:49','2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_process_task_relation_log` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_project
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_project`;

CREATE TABLE `t_ds_project` (
                                `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                `name` varchar(100) DEFAULT NULL COMMENT 'project name',
                                `code` bigint(20) NOT NULL COMMENT 'encoding',
                                `description` varchar(255) DEFAULT NULL,
                                `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
                                `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
                                `create_time` datetime NOT NULL COMMENT 'create time',
                                `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                `tenant_id` int(11) DEFAULT '-1' COMMENT 'creator id',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `unique_code` (`code`),
                                UNIQUE KEY `unique_name` (`name`),
                                KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_project` WRITE;
/*!40000 ALTER TABLE `t_ds_project` DISABLE KEYS */;

INSERT INTO `t_ds_project` (`id`, `name`, `code`, `description`, `user_id`, `flag`, `create_time`, `update_time`, `tenant_id`)
VALUES
    (2,'test_project',8024502754912,'test',1,1,'2022-12-27 00:17:08','2022-12-27 00:17:08',1),
    (3,'test_project2',8035382740320,'',4,1,'2022-12-27 23:53:48','2022-12-27 23:53:48',2);

/*!40000 ALTER TABLE `t_ds_project` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_queue
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_queue`;

CREATE TABLE `t_ds_queue` (
                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                              `queue_name` varchar(64) DEFAULT NULL COMMENT 'queue name',
                              `queue` varchar(64) DEFAULT NULL COMMENT 'yarn queue name',
                              `create_time` datetime DEFAULT NULL COMMENT 'create time',
                              `update_time` datetime DEFAULT NULL COMMENT 'update time',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `unique_queue_name` (`queue_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_queue` WRITE;
/*!40000 ALTER TABLE `t_ds_queue` DISABLE KEYS */;

INSERT INTO `t_ds_queue` (`id`, `queue_name`, `queue`, `create_time`, `update_time`)
VALUES
    (1,'default','default',NULL,NULL);

/*!40000 ALTER TABLE `t_ds_queue` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_relation_datasource_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_datasource_user`;

CREATE TABLE `t_ds_relation_datasource_user` (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                                 `user_id` int(11) NOT NULL COMMENT 'user id',
                                                 `datasource_id` int(11) DEFAULT NULL COMMENT 'data source id',
                                                 `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
                                                 `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                                 `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_namespace_user
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_process_instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_process_instance`;

CREATE TABLE `t_ds_relation_process_instance` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                                  `parent_process_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
                                                  `parent_task_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
                                                  `process_instance_id` int(11) DEFAULT NULL COMMENT 'child process instance id',
                                                  PRIMARY KEY (`id`),
                                                  KEY `idx_parent_process_task` (`parent_process_instance_id`,`parent_task_instance_id`),
                                                  KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_project_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_project_user`;

CREATE TABLE `t_ds_relation_project_user` (
                                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                              `user_id` int(11) NOT NULL COMMENT 'user id',
                                              `project_id` int(11) DEFAULT NULL COMMENT 'project id',
                                              `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
                                              `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                              `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uniq_uid_pid` (`user_id`,`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_resources_task
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_resources_task`;

CREATE TABLE `t_ds_relation_resources_task` (
                                                `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                                `task_id` int(11) DEFAULT NULL COMMENT 'task id',
                                                `full_name` varchar(255) DEFAULT NULL,
                                                `type` tinyint(4) DEFAULT NULL COMMENT 'resource type,0:FILE,1:UDF',
                                                PRIMARY KEY (`id`),
                                                UNIQUE KEY `t_ds_relation_resources_task_un` (`task_id`,`full_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_resources_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_resources_user`;

CREATE TABLE `t_ds_relation_resources_user` (
                                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                                `user_id` int(11) NOT NULL COMMENT 'user id',
                                                `resources_id` int(11) DEFAULT NULL COMMENT 'resource id',
                                                `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
                                                `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                                `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_relation_rule_execute_sql
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;

CREATE TABLE `t_ds_relation_rule_execute_sql` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `execute_sql_id` int(11) DEFAULT NULL,
                                                  `create_time` datetime DEFAULT NULL,
                                                  `update_time` datetime DEFAULT NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_relation_rule_execute_sql` WRITE;
/*!40000 ALTER TABLE `t_ds_relation_rule_execute_sql` DISABLE KEYS */;

INSERT INTO `t_ds_relation_rule_execute_sql` (`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES
    (1,1,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (2,3,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (3,5,4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (4,3,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (5,6,6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (6,6,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (7,7,9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (8,7,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (9,8,11,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (10,8,12,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (11,9,13,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (12,9,14,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (13,10,15,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (14,1,16,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (15,5,17,'2022-12-27 11:22:39','2022-12-27 11:22:39');

/*!40000 ALTER TABLE `t_ds_relation_rule_execute_sql` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_relation_rule_input_entry
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_rule_input_entry`;

CREATE TABLE `t_ds_relation_rule_input_entry` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `rule_input_entry_id` int(11) DEFAULT NULL,
                                                  `values_map` text,
                                                  `index` int(11) DEFAULT NULL,
                                                  `create_time` datetime DEFAULT NULL,
                                                  `update_time` datetime DEFAULT NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_relation_rule_input_entry` WRITE;
/*!40000 ALTER TABLE `t_ds_relation_rule_input_entry` DISABLE KEYS */;

INSERT INTO `t_ds_relation_rule_input_entry` (`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES
    (1,1,1,NULL,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (2,1,2,NULL,2,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (3,1,3,NULL,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (4,1,4,NULL,4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (5,1,5,NULL,5,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (6,1,6,'{\"statistics_name\":\"null_count.nulls\"}',6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (7,1,7,NULL,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (8,1,8,NULL,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (9,1,9,NULL,9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (10,1,10,NULL,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (11,1,17,'',11,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (12,1,19,NULL,12,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (13,2,1,NULL,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (14,2,2,NULL,2,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (15,2,3,NULL,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (16,2,6,'{\"is_show\":\"true\",\"can_edit\":\"true\"}',4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (17,2,16,NULL,5,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (18,2,4,NULL,6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (19,2,7,NULL,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (20,2,8,NULL,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (21,2,9,NULL,9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (22,2,10,NULL,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (24,2,19,NULL,12,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (25,3,1,NULL,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (26,3,2,NULL,2,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (27,3,3,NULL,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (28,3,4,NULL,4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (29,3,11,NULL,5,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (30,3,12,NULL,6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (31,3,13,NULL,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (32,3,14,NULL,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (33,3,15,NULL,9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (34,3,7,NULL,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (35,3,8,NULL,11,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (36,3,9,NULL,12,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (37,3,10,NULL,13,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (38,3,17,'{\"comparison_name\":\"total_count.total\"}',14,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (39,3,19,NULL,15,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (40,4,1,NULL,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (41,4,2,NULL,2,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (42,4,3,NULL,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (43,4,6,'{\"is_show\":\"true\",\"can_edit\":\"true\"}',4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (44,4,16,NULL,5,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (45,4,11,NULL,6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (46,4,12,NULL,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (47,4,13,NULL,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (48,4,17,'{\"is_show\":\"true\",\"can_edit\":\"true\"}',9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (49,4,18,NULL,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (50,4,7,NULL,11,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (51,4,8,NULL,12,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (52,4,9,NULL,13,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (53,4,10,NULL,14,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (62,3,6,'{\"statistics_name\":\"miss_count.miss\"}',18,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (63,5,1,NULL,1,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (64,5,2,NULL,2,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (65,5,3,NULL,3,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (66,5,4,NULL,4,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (67,5,5,NULL,5,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (68,5,6,'{\"statistics_name\":\"invalid_length_count.valids\"}',6,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (69,5,24,NULL,7,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (70,5,23,NULL,8,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (71,5,7,NULL,9,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (72,5,8,NULL,10,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (73,5,9,NULL,11,'2022-12-27 11:22:39','2022-12-27 11:22:39'),
    (74,5,10,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (75,5,17,'',13,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (76,5,19,NULL,14,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (79,6,1,NULL,1,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (80,6,2,NULL,2,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (81,6,3,NULL,3,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (82,6,4,NULL,4,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (83,6,5,NULL,5,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (84,6,6,'{\"statistics_name\":\"duplicate_count.duplicates\"}',6,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (85,6,7,NULL,7,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (86,6,8,NULL,8,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (87,6,9,NULL,9,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (88,6,10,NULL,10,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (89,6,17,'',11,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (90,6,19,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (93,7,1,NULL,1,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (94,7,2,NULL,2,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (95,7,3,NULL,3,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (96,7,4,NULL,4,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (97,7,5,NULL,5,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (98,7,6,'{\"statistics_name\":\"regexp_count.regexps\"}',6,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (99,7,25,NULL,5,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (100,7,7,NULL,7,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (101,7,8,NULL,8,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (102,7,9,NULL,9,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (103,7,10,NULL,10,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (104,7,17,NULL,11,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (105,7,19,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (108,8,1,NULL,1,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (109,8,2,NULL,2,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (110,8,3,NULL,3,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (111,8,4,NULL,4,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (112,8,5,NULL,5,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (113,8,6,'{\"statistics_name\":\"timeliness_count.timeliness\"}',6,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (114,8,26,NULL,8,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (115,8,27,NULL,9,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (116,8,7,NULL,10,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (117,8,8,NULL,11,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (118,8,9,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (119,8,10,NULL,13,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (120,8,17,NULL,14,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (121,8,19,NULL,15,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (124,9,1,NULL,1,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (125,9,2,NULL,2,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (126,9,3,NULL,3,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (127,9,4,NULL,4,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (128,9,5,NULL,5,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (129,9,6,'{\"statistics_name\":\"enum_count.enums\"}',6,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (130,9,28,NULL,7,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (131,9,7,NULL,8,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (132,9,8,NULL,9,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (133,9,9,NULL,10,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (134,9,10,NULL,11,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (135,9,17,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (136,9,19,NULL,13,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (139,10,1,NULL,1,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (140,10,2,NULL,2,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (141,10,3,NULL,3,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (142,10,4,NULL,4,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (143,10,6,'{\"statistics_name\":\"table_count.total\"}',6,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (144,10,7,NULL,7,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (145,10,8,NULL,8,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (146,10,9,NULL,9,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (147,10,10,NULL,10,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (148,10,17,NULL,11,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (149,10,19,NULL,12,'2022-12-27 11:22:40','2022-12-27 11:22:40'),
    (150,8,29,NULL,7,'2022-12-27 11:22:40','2022-12-27 11:22:40');

/*!40000 ALTER TABLE `t_ds_relation_rule_input_entry` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_relation_udfs_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_relation_udfs_user`;

CREATE TABLE `t_ds_relation_udfs_user` (
                                           `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                           `user_id` int(11) NOT NULL COMMENT 'userid',
                                           `udf_id` int(11) DEFAULT NULL COMMENT 'udf id',
                                           `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
                                           `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                           `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_resources
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_schedules
# ------------------------------------------------------------

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
                                  `process_instance_priority` int(11) DEFAULT '2' COMMENT 'process instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
                                  `worker_group` varchar(64) DEFAULT '' COMMENT 'worker group id',
                                  `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                  `create_time` datetime NOT NULL COMMENT 'create time',
                                  `update_time` datetime NOT NULL COMMENT 'update time',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_session
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_session`;

CREATE TABLE `t_ds_session` (
                                `id` varchar(64) NOT NULL COMMENT 'key',
                                `user_id` int(11) DEFAULT NULL COMMENT 'user id',
                                `ip` varchar(45) DEFAULT NULL COMMENT 'ip',
                                `last_login_time` datetime DEFAULT NULL COMMENT 'last login time',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_session` WRITE;
/*!40000 ALTER TABLE `t_ds_session` DISABLE KEYS */;

INSERT INTO `t_ds_session` (`id`, `user_id`, `ip`, `last_login_time`)
VALUES
    ('9360a989-5c80-4117-8c83-588daa12fae6',3,'127.0.0.1','2022-12-28 00:14:54'),
    ('f4914362-a208-46a0-aebe-6baf0d16324c',1,'127.0.0.1','2022-12-27 20:14:30');

/*!40000 ALTER TABLE `t_ds_session` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_task_definition
# ------------------------------------------------------------

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
                                        `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
                                        `task_params` longtext COMMENT 'job custom parameters',
                                        `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
                                        `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available',
                                        `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
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
                                        `cpu_quota` int(11) NOT NULL DEFAULT '-1' COMMENT 'cpuQuota(%): -1:Infinity',
                                        `memory_max` int(11) NOT NULL DEFAULT '-1' COMMENT 'MemoryMax(MB): -1:Infinity',
                                        `create_time` datetime NOT NULL COMMENT 'create time',
                                        `update_time` datetime NOT NULL COMMENT 'update time',
                                        PRIMARY KEY (`id`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_task_definition` WRITE;
/*!40000 ALTER TABLE `t_ds_task_definition` DISABLE KEYS */;

INSERT INTO `t_ds_task_definition` (`id`, `code`, `name`, `version`, `description`, `project_code`, `user_id`, `task_type`, `task_execute_type`, `task_params`, `flag`, `is_cache`, `task_priority`, `worker_group`, `environment_code`, `fail_retry_times`, `fail_retry_interval`, `timeout_flag`, `timeout_notify_strategy`, `timeout`, `delay_time`, `resource_ids`, `task_group_id`, `task_group_priority`, `cpu_quota`, `memory_max`, `create_time`, `update_time`)
VALUES
    (1,8024963455584,'test_shell',1,'test shell',8024502754912,2,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'hello world\'\",\"resourceList\":[]}',1,0,2,'default',-1,0,1,0,NULL,0,0,NULL,0,0,-1,-1,'2022-12-27 01:26:43','2022-12-27 01:26:43'),
    (2,8033675329888,'test_shell2',1,'',8024502754912,3,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'test shell2\'\",\"resourceList\":[]}',1,0,2,'default',-1,0,1,0,NULL,0,0,NULL,0,0,-1,-1,'2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (3,8035383664480,'test_shell',1,'',8035382740320,4,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'hellow\'\",\"resourceList\":[]}',1,0,2,'test_work_group2',-1,0,1,0,NULL,0,0,NULL,0,0,-1,-1,'2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_task_definition` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_task_definition_log
# ------------------------------------------------------------

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
                                            `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
                                            `task_params` longtext COMMENT 'job custom parameters',
                                            `flag` tinyint(2) DEFAULT NULL COMMENT '0 not available, 1 available',
                                            `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available',
                                            `task_priority` tinyint(4) DEFAULT '2' COMMENT 'job priority',
                                            `worker_group` varchar(200) DEFAULT NULL COMMENT 'worker grouping',
                                            `environment_code` bigint(20) DEFAULT '-1' COMMENT 'environment code',
                                            `fail_retry_times` int(11) DEFAULT NULL COMMENT 'number of failed retries',
                                            `fail_retry_interval` int(11) DEFAULT NULL COMMENT 'failed retry interval',
                                            `timeout_flag` tinyint(2) DEFAULT '0' COMMENT 'timeout flag:0 close, 1 open',
                                            `timeout_notify_strategy` tinyint(4) DEFAULT NULL COMMENT 'timeout notification policy: 0 warning, 1 fail',
                                            `timeout` int(11) DEFAULT '0' COMMENT 'timeout length,unit: minute',
                                            `delay_time` int(11) DEFAULT '0' COMMENT 'delay execution time,unit: minute',
                                            `resource_ids` text COMMENT 'resource id, separated by comma',
                                            `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
                                            `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id',
                                            `task_group_priority` tinyint(4) DEFAULT '0' COMMENT 'task group priority',
                                            `operate_time` datetime DEFAULT NULL COMMENT 'operate time',
                                            `cpu_quota` int(11) NOT NULL DEFAULT '-1' COMMENT 'cpuQuota(%): -1:Infinity',
                                            `memory_max` int(11) NOT NULL DEFAULT '-1' COMMENT 'MemoryMax(MB): -1:Infinity',
                                            `create_time` datetime NOT NULL COMMENT 'create time',
                                            `update_time` datetime NOT NULL COMMENT 'update time',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_code_version` (`code`,`version`),
                                            KEY `idx_project_code` (`project_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_task_definition_log` WRITE;
/*!40000 ALTER TABLE `t_ds_task_definition_log` DISABLE KEYS */;

INSERT INTO `t_ds_task_definition_log` (`id`, `code`, `name`, `version`, `description`, `project_code`, `user_id`, `task_type`, `task_execute_type`, `task_params`, `flag`, `is_cache`, `task_priority`, `worker_group`, `environment_code`, `fail_retry_times`, `fail_retry_interval`, `timeout_flag`, `timeout_notify_strategy`, `timeout`, `delay_time`, `resource_ids`, `operator`, `task_group_id`, `task_group_priority`, `operate_time`, `cpu_quota`, `memory_max`, `create_time`, `update_time`)
VALUES
    (1,8024963455584,'test_shell',1,'test shell',8024502754912,2,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'hello world\'\",\"resourceList\":[]}',1,0,2,'default',-1,0,1,0,NULL,0,0,NULL,2,0,0,'2022-12-27 01:26:43',-1,-1,'2022-12-27 01:26:43','2022-12-27 01:26:43'),
    (2,8033675329888,'test_shell2',1,'',8024502754912,3,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'test shell2\'\",\"resourceList\":[]}',1,0,2,'default',-1,0,1,0,NULL,0,0,NULL,3,0,0,'2022-12-27 20:12:21',-1,-1,'2022-12-27 20:12:21','2022-12-27 20:12:21'),
    (3,8035383664480,'test_shell',1,'',8035382740320,4,'SHELL',0,'{\"localParams\":[],\"rawScript\":\"echo \'hellow\'\",\"resourceList\":[]}',1,0,2,'test_work_group2',-1,0,1,0,NULL,0,0,NULL,4,0,0,'2022-12-27 23:54:49',-1,-1,'2022-12-27 23:54:49','2022-12-27 23:54:49');

/*!40000 ALTER TABLE `t_ds_task_definition_log` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_task_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_task_group`;

CREATE TABLE `t_ds_task_group` (
                                   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                   `name` varchar(100) DEFAULT NULL COMMENT 'task_group name',
                                   `description` varchar(255) DEFAULT NULL,
                                   `group_size` int(11) NOT NULL COMMENT 'group size',
                                   `use_size` int(11) DEFAULT '0' COMMENT 'used size',
                                   `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
                                   `project_code` bigint(20) DEFAULT '0' COMMENT 'project code',
                                   `status` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
                                   `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_task_group_queue
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_task_group_queue`;

CREATE TABLE `t_ds_task_group_queue` (
                                         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                         `task_id` int(11) DEFAULT NULL COMMENT 'taskintanceid',
                                         `task_name` varchar(100) DEFAULT NULL COMMENT 'TaskInstance name',
                                         `group_id` int(11) DEFAULT NULL COMMENT 'taskGroup id',
                                         `process_id` int(11) DEFAULT NULL COMMENT 'processInstace id',
                                         `priority` int(8) DEFAULT '0' COMMENT 'priority',
                                         `status` tinyint(4) DEFAULT '-1' COMMENT '-1: waiting  1: running  2: finished',
                                         `force_start` tinyint(4) DEFAULT '0' COMMENT 'is force start 0 NO ,1 YES',
                                         `in_queue` tinyint(4) DEFAULT '0' COMMENT 'ready to get the queue by other task finish 0 NO ,1 YES',
                                         `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                         `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table t_ds_task_instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_task_instance`;

CREATE TABLE `t_ds_task_instance` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                      `name` varchar(255) DEFAULT NULL COMMENT 'task name',
                                      `task_type` varchar(50) NOT NULL COMMENT 'task type',
                                      `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream',
                                      `task_code` bigint(20) NOT NULL COMMENT 'task definition code',
                                      `task_definition_version` int(11) DEFAULT '0' COMMENT 'task definition version',
                                      `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
                                      `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
                                      `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
                                      `start_time` datetime DEFAULT NULL COMMENT 'task start time',
                                      `end_time` datetime DEFAULT NULL COMMENT 'task end time',
                                      `host` varchar(135) DEFAULT NULL COMMENT 'host of task running on',
                                      `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
                                      `log_path` longtext COMMENT 'task log path',
                                      `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
                                      `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
                                      `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
                                      `app_link` text COMMENT 'yarn app id',
                                      `task_params` longtext COMMENT 'job custom parameters',
                                      `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
                                      `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available',
                                      `cache_key` varchar(200) DEFAULT NULL COMMENT 'cache_key',
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
                                      `cpu_quota` int(11) NOT NULL DEFAULT '-1' COMMENT 'cpuQuota(%): -1:Infinity',
                                      `memory_max` int(11) NOT NULL DEFAULT '-1' COMMENT 'MemoryMax(MB): -1:Infinity',
                                      `test_flag` tinyint(4) DEFAULT NULL COMMENT 'test flag：0 normal, 1 test run',
                                      PRIMARY KEY (`id`),
                                      KEY `process_instance_id` (`process_instance_id`) USING BTREE,
                                      KEY `idx_code_version` (`task_code`,`task_definition_version`) USING BTREE,
                                      KEY `idx_cache_key` (`cache_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_task_instance` WRITE;
/*!40000 ALTER TABLE `t_ds_task_instance` DISABLE KEYS */;

INSERT INTO `t_ds_task_instance` (`id`, `name`, `task_type`, `task_execute_type`, `task_code`, `task_definition_version`, `process_instance_id`, `state`, `submit_time`, `start_time`, `end_time`, `host`, `execute_path`, `log_path`, `alert_flag`, `retry_times`, `pid`, `app_link`, `task_params`, `flag`, `is_cache`, `cache_key`, `retry_interval`, `max_retry_times`, `task_instance_priority`, `worker_group`, `environment_code`, `environment_config`, `executor_id`, `first_submit_time`, `delay_time`, `var_pool`, `task_group_id`, `dry_run`, `cpu_quota`, `memory_max`, `test_flag`)
VALUES
    (1,'test_shell','SHELL',0,8024963455584,1,1,7,'2022-12-27 01:45:19','2022-12-27 01:45:19','2022-12-27 01:45:19','10.10.31.24:1234','/tmp/dolphinscheduler/exec/process/mxq/8024502754912/8025037222752_1/1/1','/Users/mxq/Desktop/work_dir/project/dolphinscheduler/logs/20221227/8025037222752_1-1-1.log',0,0,29204,NULL,'{\"localParams\":[],\"rawScript\":\"echo \'hello world\'\",\"resourceList\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}',1,0,NULL,1,0,2,'default',-1,NULL,2,'2022-12-27 01:45:19',0,'[]',0,0,-1,-1,0),
    (2,'test_shell','SHELL',0,8024963455584,1,2,7,'2022-12-27 20:12:36','2022-12-27 20:12:36','2022-12-27 20:12:36','192.168.228.1:1234','/tmp/dolphinscheduler/exec/process/mxq/8024502754912/8025037222752_2/2/2','/Users/mxq/Desktop/work_dir/project/dolphinscheduler/logs/20221228/8025037222752_2-2-2.log',0,0,33237,NULL,'{\"localParams\":[],\"rawScript\":\"echo \'hello world\'\",\"resourceList\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}',1,0,NULL,1,0,2,'default',-1,NULL,3,'2022-12-27 20:12:36',0,'[]',0,0,-1,-1,0),
    (3,'test_shell2','SHELL',0,8033675329888,1,2,7,'2022-12-27 20:12:37','2022-12-27 20:12:37','2022-12-27 20:12:37','192.168.228.1:1234','/tmp/dolphinscheduler/exec/process/mxq/8024502754912/8025037222752_2/2/3','/Users/mxq/Desktop/work_dir/project/dolphinscheduler/logs/20221228/8025037222752_2-2-3.log',0,0,33245,NULL,'{\"localParams\":[],\"rawScript\":\"echo \'test shell2\'\",\"resourceList\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}',1,0,NULL,1,0,2,'default',-1,NULL,3,'2022-12-27 20:12:37',0,'[]',0,0,-1,-1,0),
    (4,'test_shell','SHELL',0,8024963455584,1,3,7,'2022-12-27 20:17:27','2022-12-27 20:17:27','2022-12-27 20:17:27','192.168.228.1:1234','/tmp/dolphinscheduler/exec/process/mxq/8024502754912/8025037222752_2/3/4','/Users/mxq/Desktop/work_dir/project/dolphinscheduler/logs/20221228/8025037222752_2-3-4.log',0,0,33355,NULL,'{\"localParams\":[],\"rawScript\":\"echo \'hello world\'\",\"resourceList\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}',1,0,NULL,1,0,2,'test_work_group',-1,NULL,2,'2022-12-27 20:17:27',0,'[]',0,0,-1,-1,0),
    (5,'test_shell2','SHELL',0,8033675329888,1,3,7,'2022-12-27 20:17:28','2022-12-27 20:17:28','2022-12-27 20:17:28','192.168.228.1:1234','/tmp/dolphinscheduler/exec/process/mxq/8024502754912/8025037222752_2/3/5','/Users/mxq/Desktop/work_dir/project/dolphinscheduler/logs/20221228/8025037222752_2-3-5.log',0,0,33363,NULL,'{\"localParams\":[],\"rawScript\":\"echo \'test shell2\'\",\"resourceList\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}',1,0,NULL,1,0,2,'test_work_group',-1,NULL,2,'2022-12-27 20:17:28',0,'[]',0,0,-1,-1,0);

/*!40000 ALTER TABLE `t_ds_task_instance` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_tenant
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_tenant`;

CREATE TABLE `t_ds_tenant` (
                               `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                               `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code',
                               `description` varchar(255) DEFAULT NULL,
                               `queue_id` int(11) DEFAULT NULL COMMENT 'queue id',
                               `create_time` datetime DEFAULT NULL COMMENT 'create time',
                               `update_time` datetime DEFAULT NULL COMMENT 'update time',
                               `project_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'queue id',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `unique_tenant_code` (`tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_tenant` WRITE;
/*!40000 ALTER TABLE `t_ds_tenant` DISABLE KEYS */;

INSERT INTO `t_ds_tenant` (`id`, `tenant_code`, `description`, `queue_id`, `create_time`, `update_time`, `project_id`)
VALUES
    (1,'mxq','',1,'2022-12-26 23:46:46','2022-12-26 23:46:46',-1),
    (2,'mxq2','',1,'2022-12-27 21:05:41','2022-12-27 21:05:41',-1);

/*!40000 ALTER TABLE `t_ds_tenant` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_udfs
# ------------------------------------------------------------

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
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `unique_func_name` (`func_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_udfs` WRITE;
/*!40000 ALTER TABLE `t_ds_udfs` DISABLE KEYS */;

INSERT INTO `t_ds_udfs` (`id`, `user_id`, `func_name`, `class_name`, `type`, `arg_types`, `database`, `description`, `resource_id`, `resource_name`, `create_time`, `update_time`)
VALUES
    (1,2,'test','com.tydl',0,NULL,NULL,NULL,-1,'dolphinscheduler/mxq/udfs/commons-io-2.11.0.jar','2022-12-27 20:46:09','2022-12-27 20:46:09');

/*!40000 ALTER TABLE `t_ds_udfs` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_user
# ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_user` WRITE;
/*!40000 ALTER TABLE `t_ds_user` DISABLE KEYS */;

INSERT INTO `t_ds_user` (`id`, `user_name`, `user_password`, `user_type`, `email`, `phone`, `tenant_id`, `create_time`, `update_time`, `queue`, `state`, `time_zone`)
VALUES
    (1,'admin','7ad2410b2f4c074479a8937a28a22b8f',0,'xxx@qq.com','',0,'2022-12-27 11:22:39','2022-12-27 11:22:39',NULL,1,NULL),
    (2,'test_user','7ad2410b2f4c074479a8937a28a22b8f',2,'578038303@qq.com','17665109562',1,'2022-12-27 01:10:38','2022-12-27 01:10:38','default',1,NULL),
    (3,'general_user','7ad2410b2f4c074479a8937a28a22b8f',1,'578038303@qq.com','17665109562',1,'2022-12-27 02:00:22','2022-12-27 02:00:22','default',1,NULL),
    (4,'general_user2','7ad2410b2f4c074479a8937a28a22b8f',1,'578038303@qq.com','',2,'2022-12-27 23:52:15','2022-12-27 23:53:05','default',1,NULL);

/*!40000 ALTER TABLE `t_ds_user` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_version
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_version`;

CREATE TABLE `t_ds_version` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `version` varchar(200) NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `version_UNIQUE` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='version';

LOCK TABLES `t_ds_version` WRITE;
/*!40000 ALTER TABLE `t_ds_version` DISABLE KEYS */;

INSERT INTO `t_ds_version` (`id`, `version`)
VALUES
    (1,'2.0.2');

/*!40000 ALTER TABLE `t_ds_version` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_ds_worker_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_ds_worker_group`;

CREATE TABLE `t_ds_worker_group` (
                                     `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                     `name` varchar(255) NOT NULL COMMENT 'worker group name',
                                     `addr_list` text COMMENT 'worker addr list. split by [,]',
                                     `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                     `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                     `description` text COMMENT 'description',
                                     `other_params_json` text COMMENT 'other params json',
                                     `tenant_id` int(11) NOT NULL,
                                     `tenant_code` varchar(256) NOT NULL DEFAULT '',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `t_ds_worker_group` WRITE;
/*!40000 ALTER TABLE `t_ds_worker_group` DISABLE KEYS */;

INSERT INTO `t_ds_worker_group` (`id`, `name`, `addr_list`, `create_time`, `update_time`, `description`, `other_params_json`, `tenant_id`, `tenant_code`)
VALUES
    (1,'test_work_group','192.168.228.1:1234','2022-12-27 20:15:52','2022-12-27 20:15:52','',NULL,1,'mxq'),
    (2,'test_work_group2','192.168.228.1:1234','2022-12-27 21:05:59','2022-12-27 21:05:59','',NULL,2,'mxq2');

/*!40000 ALTER TABLE `t_ds_worker_group` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
