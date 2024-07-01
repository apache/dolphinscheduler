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

ALTER TABLE t_ds_process_definition DROP tenant_id;
ALTER TABLE t_ds_process_definition_log DROP tenant_id;
ALTER TABLE t_ds_process_instance DROP tenant_id;

SET FOREIGN_KEY_CHECKS = 0;
-- auto detect by atlas, see more detail in https://github.com/apache/dolphinscheduler/pull/14620
ALTER TABLE `QRTZ_BLOB_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL;
ALTER TABLE `QRTZ_CALENDARS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `CALENDAR_NAME` varchar(200) NOT NULL;
ALTER TABLE `QRTZ_CRON_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `CRON_EXPRESSION` varchar(120) NOT NULL, MODIFY COLUMN `TIME_ZONE_ID` varchar(80) NULL;
ALTER TABLE `QRTZ_FIRED_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `ENTRY_ID` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `INSTANCE_NAME` varchar(200) NOT NULL, MODIFY COLUMN `STATE` varchar(16) NOT NULL, MODIFY COLUMN `JOB_NAME` varchar(200) NULL, MODIFY COLUMN `JOB_GROUP` varchar(200) NULL, MODIFY COLUMN `IS_NONCONCURRENT` varchar(1) NULL, MODIFY COLUMN `REQUESTS_RECOVERY` varchar(1) NULL;
ALTER TABLE `QRTZ_JOB_DETAILS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `JOB_NAME` varchar(200) NOT NULL, MODIFY COLUMN `JOB_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `DESCRIPTION` varchar(250) NULL, MODIFY COLUMN `JOB_CLASS_NAME` varchar(250) NOT NULL, MODIFY COLUMN `IS_DURABLE` varchar(1) NOT NULL, MODIFY COLUMN `IS_NONCONCURRENT` varchar(1) NOT NULL, MODIFY COLUMN `IS_UPDATE_DATA` varchar(1) NOT NULL, MODIFY COLUMN `REQUESTS_RECOVERY` varchar(1) NOT NULL;
ALTER TABLE `QRTZ_LOCKS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `LOCK_NAME` varchar(40) NOT NULL;
ALTER TABLE `QRTZ_PAUSED_TRIGGER_GRPS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL;
ALTER TABLE `QRTZ_SCHEDULER_STATE` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `INSTANCE_NAME` varchar(200) NOT NULL;
ALTER TABLE `QRTZ_SIMPLE_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL;
ALTER TABLE `QRTZ_SIMPROP_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `STR_PROP_1` varchar(512) NULL, MODIFY COLUMN `STR_PROP_2` varchar(512) NULL, MODIFY COLUMN `STR_PROP_3` varchar(512) NULL, MODIFY COLUMN `BOOL_PROP_1` varchar(1) NULL, MODIFY COLUMN `BOOL_PROP_2` varchar(1) NULL;
ALTER TABLE `QRTZ_TRIGGERS` COLLATE utf8_bin, MODIFY COLUMN `SCHED_NAME` varchar(120) NOT NULL, MODIFY COLUMN `TRIGGER_NAME` varchar(200) NOT NULL, MODIFY COLUMN `TRIGGER_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `JOB_NAME` varchar(200) NOT NULL, MODIFY COLUMN `JOB_GROUP` varchar(200) NOT NULL, MODIFY COLUMN `DESCRIPTION` varchar(250) NULL, MODIFY COLUMN `TRIGGER_STATE` varchar(16) NOT NULL, MODIFY COLUMN `TRIGGER_TYPE` varchar(8) NOT NULL, MODIFY COLUMN `CALENDAR_NAME` varchar(200) NULL;
ALTER TABLE `t_ds_plugin_define` AUTO_INCREMENT 2;
ALTER TABLE `t_ds_process_instance` MODIFY COLUMN `state_history` text NULL COMMENT 'state history desc';
ALTER TABLE `t_ds_project` MODIFY COLUMN `description` varchar(255) NULL;
ALTER TABLE `t_ds_task_group` MODIFY COLUMN `description` varchar(255) NULL;
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `app_link` text NULL COMMENT 'yarn app id', MODIFY COLUMN `cache_key` varchar(200) NULL COMMENT 'cache_key', MODIFY COLUMN `executor_name` varchar(64) NULL;
ALTER TABLE `t_ds_worker_group` MODIFY COLUMN `description` text NULL COMMENT 'description';
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `cache_key` varchar(200) NULL COMMENT 'cache_key', MODIFY COLUMN `executor_name` varchar(64) NULL;
ALTER TABLE `t_ds_fav_task` MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id';
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `cache_key` varchar(200) NULL COMMENT 'cache_key', MODIFY COLUMN `executor_name` varchar(64) NULL;
SET FOREIGN_KEY_CHECKS = 1;
