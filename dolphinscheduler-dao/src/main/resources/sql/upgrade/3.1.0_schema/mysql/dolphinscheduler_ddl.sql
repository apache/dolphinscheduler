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

-- t_ds_k8s_namespace
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS online_job_num;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_code;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_code()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='online_job_num')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN online_job_num;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_code;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_code;
-- ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS k8s;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_col_k8s;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_col_k8s()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='k8s')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP COLUMN k8s;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_col_k8s;
DROP PROCEDURE drop_t_ds_k8s_namespace_col_k8s;
-- ALTER TABLE t_ds_k8s_namespace DROP IF EXISTS UNIQUE KEY k8s_namespace_unique;
drop PROCEDURE if EXISTS drop_t_ds_k8s_namespace_uk_k8s_namespace_unique;
delimiter d//
CREATE PROCEDURE drop_t_ds_k8s_namespace_uk_k8s_namespace_unique()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='k8s_namespace_unique')
    THEN
ALTER TABLE t_ds_k8s_namespace DROP INDEX k8s_namespace_unique;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_k8s_namespace_uk_k8s_namespace_unique;
DROP PROCEDURE drop_t_ds_k8s_namespace_uk_k8s_namespace_unique;
-- ALTER TABLE t_ds_k8s_namespace ADD COLUMN IF NOT EXISTS code bigint(20) NOT NULL DEFAULT '0';
drop PROCEDURE if EXISTS add_t_ds_k8s_namespace_col_code;
delimiter d//
CREATE PROCEDURE add_t_ds_k8s_namespace_col_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='code')
    THEN
ALTER TABLE t_ds_k8s_namespace ADD COLUMN code bigint(20) NOT NULL DEFAULT '0';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_k8s_namespace_col_code;
DROP PROCEDURE add_t_ds_k8s_namespace_col_code;
-- ALTER TABLE t_ds_k8s_namespace ADD COLUMN IF NOT EXISTS cluster_code bigint(20) NOT NULL DEFAULT '0';
drop PROCEDURE if EXISTS add_t_ds_k8s_namespace_col_cluster_code;
delimiter d//
CREATE PROCEDURE add_t_ds_k8s_namespace_col_cluster_code()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='cluster_code')
    THEN
ALTER TABLE t_ds_k8s_namespace ADD COLUMN cluster_code bigint(20) NOT NULL DEFAULT '0';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_k8s_namespace_col_cluster_code;
DROP PROCEDURE add_t_ds_k8s_namespace_col_cluster_code;
-- ALTER TABLE t_ds_k8s_namespace ADD IF NOT EXISTS UNIQUE KEY k8s_namespace_unique(namespace, cluster_code);
drop PROCEDURE if EXISTS add_t_ds_k8s_namespace_uk_k8s_namespace_unique;
delimiter d//
CREATE PROCEDURE add_t_ds_k8s_namespace_uk_k8s_namespace_unique()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_k8s_namespace'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='k8s_namespace_unique')
    THEN
ALTER TABLE t_ds_k8s_namespace ADD UNIQUE KEY k8s_namespace_unique(namespace, cluster_code);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_k8s_namespace_uk_k8s_namespace_unique;
DROP PROCEDURE add_t_ds_k8s_namespace_uk_k8s_namespace_unique;

-- t_ds_task_definition
-- ALTER TABLE `t_ds_task_definition` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `task_group_priority`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_col_cpu_quota;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_col_cpu_quota()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='cpu_quota')
    THEN
ALTER TABLE `t_ds_task_definition` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `task_group_priority`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_col_cpu_quota;
DROP PROCEDURE add_t_ds_task_definition_col_cpu_quota;
-- ALTER TABLE `t_ds_task_definition` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_col_memory_max;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_col_memory_max()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='memory_max')
    THEN
ALTER TABLE `t_ds_task_definition` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_col_memory_max;
DROP PROCEDURE add_t_ds_task_definition_col_memory_max;

-- t_ds_task_definition_log
-- ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `operate_time`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_col_cpu_quota;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_col_cpu_quota()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='cpu_quota')
    THEN
ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `operate_time`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_col_cpu_quota;
DROP PROCEDURE add_t_ds_task_definition_log_col_cpu_quota;
-- ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_col_memory_max;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_col_memory_max()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='memory_max')
    THEN
ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_col_memory_max;
DROP PROCEDURE add_t_ds_task_definition_log_col_memory_max;

-- t_ds_task_instance
-- ALTER TABLE `t_ds_task_instance` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `dry_run`;
drop PROCEDURE if EXISTS add_t_ds_task_instance_col_cpu_quota;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_col_cpu_quota()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='cpu_quota')
    THEN
ALTER TABLE `t_ds_task_instance` ADD COLUMN `cpu_quota` int(11) DEFAULT '-1' NOT NULL COMMENT 'cpuQuota(%): -1:Infinity' AFTER `dry_run`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_col_cpu_quota;
DROP PROCEDURE add_t_ds_task_instance_col_cpu_quota;
-- ALTER TABLE `t_ds_task_instance` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
drop PROCEDURE if EXISTS add_t_ds_task_instance_col_memory_max;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_col_memory_max()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='memory_max')
    THEN
ALTER TABLE `t_ds_task_instance` ADD COLUMN `memory_max` int(11) DEFAULT '-1' NOT NULL COMMENT 'MemoryMax(MB): -1:Infinity' AFTER `cpu_quota`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_col_memory_max;
DROP PROCEDURE add_t_ds_task_instance_col_memory_max;

-- t_ds_relation_process_instance
-- ALTER TABLE `t_ds_relation_process_instance` ADD KEY `idx_parent_process_task`( `parent_process_instance_id`, `parent_task_instance_id` );
drop PROCEDURE if EXISTS add_t_ds_relation_process_instance_idx_parent_process_task;
delimiter d//
CREATE PROCEDURE add_t_ds_relation_process_instance_idx_parent_process_task()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_relation_process_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_parent_process_task')
    THEN
ALTER TABLE `t_ds_relation_process_instance` ADD KEY `idx_parent_process_task`( `parent_process_instance_id`, `parent_task_instance_id` );
END IF;
END;
d//
delimiter ;
CALL add_t_ds_relation_process_instance_idx_parent_process_task;
DROP PROCEDURE add_t_ds_relation_process_instance_idx_parent_process_task;
-- ALTER TABLE `t_ds_relation_process_instance` ADD KEY `idx_process_instance_id`(`process_instance_id`);
drop PROCEDURE if EXISTS add_t_ds_relation_process_instance_idx_process_instance_id;
delimiter d//
CREATE PROCEDURE add_t_ds_relation_process_instance_idx_process_instance_id()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_relation_process_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='idx_process_instance_id')
    THEN
ALTER TABLE `t_ds_relation_process_instance` ADD KEY `idx_process_instance_id`(`process_instance_id`);
END IF;
END;
d//
delimiter ;
CALL add_t_ds_relation_process_instance_idx_process_instance_id;
DROP PROCEDURE add_t_ds_relation_process_instance_idx_process_instance_id;

-- ----------------------------
-- Table structure for t_ds_cluster
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_cluster`;
CREATE TABLE `t_ds_cluster` (
    `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `code` bigint(20)  DEFAULT NULL COMMENT 'encoding',
    `name` varchar(100) NOT NULL COMMENT 'cluster name',
    `config` text NULL DEFAULT NULL COMMENT 'this config contains many cluster variables config',
    `description` text NULL DEFAULT NULL COMMENT 'the details',
    `operator` int(11) DEFAULT NULL COMMENT 'operator user id',
    `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `cluster_name_unique` (`name`),
    UNIQUE KEY `cluster_code_unique` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ALTER TABLE `t_ds_task_definition` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_col_task_execute_type;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_col_task_execute_type()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_execute_type')
    THEN
ALTER TABLE `t_ds_task_definition` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_col_task_execute_type;
DROP PROCEDURE add_t_ds_task_definition_col_task_execute_type;

-- ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
drop PROCEDURE if EXISTS add_t_ds_task_definition_log_col_task_execute_type;
delimiter d//
CREATE PROCEDURE add_t_ds_task_definition_log_col_task_execute_type()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_execute_type')
    THEN
ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_definition_log_col_task_execute_type;
DROP PROCEDURE add_t_ds_task_definition_log_col_task_execute_type;

-- ALTER TABLE `t_ds_task_instance` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
drop PROCEDURE if EXISTS add_t_ds_task_instance_col_task_execute_type;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_col_task_execute_type()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_execute_type')
    THEN
ALTER TABLE `t_ds_task_instance` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_col_task_execute_type;
DROP PROCEDURE add_t_ds_task_instance_col_task_execute_type;

-- ALTER TABLE `t_ds_task_instance` DROP FOREIGN KEY foreign_key_instance_id;
drop PROCEDURE if EXISTS drop_t_ds_task_instance_key_foreign_key_instance_id;
delimiter d//
CREATE PROCEDURE drop_t_ds_task_instance_key_foreign_key_instance_id()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='foreign_key_instance_id')
    THEN
ALTER TABLE `t_ds_task_instance` DROP FOREIGN KEY foreign_key_instance_id;
END IF;
END;
d//
delimiter ;
CALL drop_t_ds_task_instance_key_foreign_key_instance_id;
DROP PROCEDURE drop_t_ds_task_instance_key_foreign_key_instance_id;

-- alter table `t_ds_project` modify `description` varchar(255);
drop PROCEDURE if EXISTS modify_t_ds_project_col_description;
delimiter d//
CREATE PROCEDURE modify_t_ds_project_col_description()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_project'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='description')
    THEN
SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
alter table `t_ds_project` modify column `description` varchar(255);
END IF;
END;
d//
delimiter ;
CALL modify_t_ds_project_col_description;
DROP PROCEDURE modify_t_ds_project_col_description;

-- alter table `t_ds_task_group` modify `description` varchar(255);
drop PROCEDURE if EXISTS modify_t_ds_task_group_col_description;
delimiter d//
CREATE PROCEDURE modify_t_ds_task_group_col_description()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_project'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='description')
    THEN
SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
alter table `t_ds_task_group` modify column `description` varchar(255);
END IF;
END;
d//
delimiter ;
CALL modify_t_ds_task_group_col_description;
DROP PROCEDURE modify_t_ds_task_group_col_description;

alter table t_ds_process_instance alter column process_instance_priority set default 2;
alter table t_ds_schedules alter column process_instance_priority set default 2;
alter table t_ds_command alter column process_instance_priority set default 2;
alter table t_ds_error_command alter column process_instance_priority set default 2;

alter table t_ds_task_definition_log alter column task_priority set default 2;
alter table t_ds_task_definition alter column task_priority set default 2;

-- alter table `t_ds_worker_group` add `other_params_json` text;
-- alter table `t_ds_process_instance` add `state_history` text;
drop procedure if exists add_column_safety;
delimiter d//
create procedure add_column_safety(target_table_name varchar(256), target_column varchar(256),
                                   target_column_type varchar(256), sths_else varchar(256))
begin
    declare target_database varchar(256);
select database() into target_database;
IF EXISTS(SELECT *
              FROM information_schema.COLUMNS
              WHERE COLUMN_NAME = target_column
                AND TABLE_NAME = target_table_name
        )
    THEN
        set @statement =
                concat('alter table ', target_table_name, ' change column ', target_column, ' ', target_column, ' ',
                       target_column_type, ' ',
                       sths_else);
PREPARE STMT_c FROM @statement;
EXECUTE STMT_c;
ELSE
        set @statement =
                concat('alter table ', target_table_name, ' add column ', target_column, ' ', target_column_type, ' ',
                       sths_else);
PREPARE STMT_a FROM @statement;
EXECUTE STMT_a;
END IF;
end;
d//
delimiter ;

call add_column_safety('t_ds_worker_group','other_params_json', 'text' , "DEFAULT NULL COMMENT 'other params json'");
call add_column_safety('t_ds_process_instance','state_history', 'text' , "DEFAULT NULL COMMENT 'state history desc' AFTER `state`");

drop procedure if exists add_column_safety;