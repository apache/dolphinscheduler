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

-- Modify "t_ds_alertgroup" table
ALTER TABLE `t_ds_alertgroup` AUTO_INCREMENT 3;
-- Modify "t_ds_alert_plugin_instance" table
ALTER TABLE `t_ds_alert_plugin_instance`
    ADD COLUMN `instance_type` int NOT NULL DEFAULT 0, ADD COLUMN `warning_type` int NOT NULL DEFAULT 3;
-- Create "t_ds_listener_event" table
CREATE TABLE `t_ds_listener_event`
(
    `id`          int      NOT NULL AUTO_INCREMENT COMMENT "key",
    `content`     text NULL COMMENT "listener event json content",
    `sign`        char(64) NOT NULL DEFAULT "" COMMENT "sign=sha1(content)",
    `post_status` tinyint NOT NULL DEFAULT 0 COMMENT "0:wait running,1:success,2:failed,3:partial success",
    `event_type`  int NOT NULL COMMENT "listener event type",
    `log`         text NULL COMMENT "log",
    `create_time` datetime NULL COMMENT "create time",
    `update_time` datetime NULL COMMENT "update time",
    PRIMARY KEY (`id`),
    INDEX         `idx_sign` (`sign`),
    INDEX         `idx_status` (`post_status`)
) CHARSET utf8 COLLATE utf8_bin;

-- modify_data_t_ds_dq_rule_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_data_t_ds_dq_rule_input_entry;
DROP PROCEDURE if EXISTS modify_data_t_ds_dq_rule_input_entry;
delimiter d//
CREATE PROCEDURE modify_data_t_ds_dq_rule_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_dq_rule_input_entry'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='value')
   THEN
ALTER TABLE `t_ds_dq_rule_input_entry`
    CHANGE COLUMN `value` `data` varchar(255) DEFAULT NULL;
END IF;
END;
d//
delimiter ;
CALL modify_data_t_ds_dq_rule_input_entry;
DROP PROCEDURE modify_data_t_ds_dq_rule_input_entry;

-- modify_data_value_t_ds_dq_rule_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_data_value_t_ds_dq_rule_input_entry;
DROP PROCEDURE if EXISTS modify_data_value_t_ds_dq_rule_input_entry;
delimiter d//
CREATE PROCEDURE modify_data_value_t_ds_dq_rule_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_dq_rule_input_entry'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='value_type')
   THEN
ALTER TABLE `t_ds_dq_rule_input_entry`
    CHANGE COLUMN `value_type` `data_type` int(11) DEFAULT NULL;
END IF;
END;
d//
delimiter ;
CALL modify_data_value_t_ds_dq_rule_input_entry;
DROP PROCEDURE modify_data_value_t_ds_dq_rule_input_entry;

ALTER TABLE `t_ds_process_definition` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_process_definition_log` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_process_instance` MODIFY COLUMN `process_definition_version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_task_definition` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "task definition version";
ALTER TABLE `t_ds_task_definition_log` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "task definition version";
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_definition_version` int NOT NULL DEFAULT 1 COMMENT "task definition version";

-- t_ds_k8s_namespace
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS limits_cpu;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_limits_cpu;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_limits_cpu()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='limits_cpu')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN limits_cpu;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_limits_cpu;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_limits_cpu;
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS limits_memory;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_limits_memory;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_limits_memory()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='limits_memory')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN limits_memory;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_limits_memory;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_limits_memory;
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS pod_replicas;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_pod_replicas;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_pod_replicas()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='pod_replicas')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN pod_replicas;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_pod_replicas;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_pod_replicas;
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS pod_request_cpu;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_pod_request_cpu;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_pod_request_cpu()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='pod_request_cpu')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN pod_request_cpu;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_pod_request_cpu;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_pod_request_cpu;
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS pod_request_memory;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_pod_request_memory;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_pod_request_memory()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='pod_request_memory')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN pod_request_memory;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_pod_request_memory;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_pod_request_memory;

ALTER TABLE `t_ds_project_parameter` MODIFY COLUMN `param_value` text NOT NULL COMMENT 'project parameter value';

ALTER TABLE `t_ds_process_definition` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_process_definition_log` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_process_instance` MODIFY COLUMN `process_definition_version` int NOT NULL DEFAULT 1 COMMENT "process definition version";
ALTER TABLE `t_ds_task_definition` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "task definition version";
ALTER TABLE `t_ds_task_definition_log` MODIFY COLUMN `version` int NOT NULL DEFAULT 1 COMMENT "task definition version";
ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_definition_version` int NOT NULL DEFAULT 1 COMMENT "task definition version";

-- create idx_t_ds_task_group_queue_in_queue on t_ds_task_group_queue
DROP PROCEDURE IF EXISTS create_idx_t_ds_task_group_queue_in_queue;
delimiter d//
CREATE PROCEDURE create_idx_t_ds_task_group_queue_in_queue()
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO index_exists FROM information_schema.statistics WHERE table_schema = (SELECT DATABASE()) AND table_name = 't_ds_task_group_queue' AND index_name = 'idx_t_ds_task_group_queue_in_queue';
    IF index_exists = 0 THEN CREATE INDEX idx_t_ds_task_group_queue_in_queue ON t_ds_task_group_queue(in_queue);
END IF;
END;
d//
delimiter ;
CALL create_idx_t_ds_task_group_queue_in_queue;
DROP PROCEDURE create_idx_t_ds_task_group_queue_in_queue;