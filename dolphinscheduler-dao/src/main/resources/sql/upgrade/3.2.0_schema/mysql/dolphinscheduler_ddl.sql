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

-- uc_dolphin_T_t_ds_command_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_command_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_command_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_command ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_command_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_command_R_test_flag;

-- uc_dolphin_T_t_ds_error_command_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_error_command_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_error_command_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_error_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_error_command ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_error_command_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_error_command_R_test_flag;

-- uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_datasource'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
           and NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_datasource'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='bind_test_id')
   THEN
ALTER TABLE t_ds_datasource ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 testDataSource';
ALTER TABLE t_ds_datasource ADD `bind_test_id` int DEFAULT null COMMENT 'bind testDataSource id';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id;
DROP PROCEDURE uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id;

-- uc_dolphin_T_t_ds_process_instance_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_instance_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_instance_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_process_instance ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_instance_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_process_instance_R_test_flag;

-- uc_dolphin_T_t_ds_task_instance_R_test_flag
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_test_flag;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_test_flag()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='test_flag')
   THEN
ALTER TABLE t_ds_task_instance ADD `test_flag` tinyint(4) DEFAULT null COMMENT 'test flag：0 normal, 1 test run';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_test_flag;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_test_flag;

delimiter d//
CREATE TABLE `t_ds_trigger_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `trigger_type` int(11) NOT NULL DEFAULT '0' COMMENT '0 process 1 task',
    `trigger_code` bigint(20) NOT NULL,
    `job_id` bigint(20) NOT NULL,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `t_ds_trigger_relation_trigger_code_IDX` (`trigger_code`),
    UNIQUE KEY `t_ds_trigger_relation_UN` (`trigger_type`,`job_id`,`trigger_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
d//
delimiter ;

-- uc_dolphin_T_t_ds_task_definition_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_definition_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_definition_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_definition ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_task_definition_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_definition_R_is_cache;


-- uc_dolphin_T_t_ds_task_definition_log_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_definition_log_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_definition_log_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_definition_log'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_definition_log ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_definition_log_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_definition_log_R_is_cache;


-- uc_dolphin_T_t_ds_task_instance_R_is_cache
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_is_cache;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_is_cache()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_cache')
   THEN
ALTER TABLE t_ds_task_instance ADD `is_cache` tinyint(2) DEFAULT '0' COMMENT '0 not available, 1 available';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_is_cache;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_is_cache;

-- uc_dolphin_T_t_ds_task_instance_R_cache_key
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_task_instance_R_cache_key;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_task_instance_R_cache_key()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='cache_key')
   THEN
ALTER TABLE t_ds_task_instance ADD `cache_key` varchar(255) DEFAULT null COMMENT 'cache key';
END IF;
END;

d//
delimiter ;
CALL uc_dolphin_T_t_ds_task_instance_R_cache_key;
DROP PROCEDURE uc_dolphin_T_t_ds_task_instance_R_cache_key;


-- ALTER TABLE `t_ds_task_instance` ADD KEY `cache_key`( `cache_key`);
drop PROCEDURE if EXISTS add_t_ds_task_instance_idx_cache_key;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_idx_cache_key()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_task_instance'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='cache_key')
    THEN
ALTER TABLE `t_ds_task_instance` ADD KEY `cache_key`( `cache_key` );
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_idx_cache_key;
DROP PROCEDURE add_t_ds_task_instance_idx_cache_key;

-- ALTER TABLE `t_ds_process_instance` ADD column `project_code`, `process_definition_name`, `executor_name`, `tenant_code`;
drop PROCEDURE if EXISTS add_t_ds_process_instance_add_project_code;
delimiter d//
CREATE PROCEDURE add_t_ds_process_instance_add_project_code()
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='project_code')
   THEN
ALTER TABLE t_ds_process_instance ADD `project_code` bigint(20) DEFAULT NULL COMMENT 'project code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='executor_name')
   THEN
ALTER TABLE t_ds_process_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='tenant_code')
   THEN
ALTER TABLE t_ds_process_instance ADD `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_process_instance_add_project_code;
DROP PROCEDURE add_t_ds_process_instance_add_project_code;

-- ALTER TABLE `t_ds_task_instance` ADD column `project_code`, `process_definition_name`, `executor_name`, `tenant_code`;
drop PROCEDURE if EXISTS add_t_ds_task_instance_add_project_code;
delimiter d//
CREATE PROCEDURE add_t_ds_task_instance_add_project_code()
BEGIN
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='process_instance_name')
   THEN
ALTER TABLE t_ds_task_instance ADD `process_instance_name` varchar(255) DEFAULT NULL COMMENT 'process instance name';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='project_code')
   THEN
ALTER TABLE t_ds_task_instance ADD `project_code` bigint(20) DEFAULT NULL COMMENT 'project code';
END IF;
   IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='executor_name')
   THEN
ALTER TABLE t_ds_task_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
END;
d//
delimiter ;
CALL add_t_ds_task_instance_add_project_code;
DROP PROCEDURE add_t_ds_task_instance_add_project_code;
