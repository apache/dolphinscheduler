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

-- uc_dolphin_T_t_ds_worker_group_R_ip_list
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_worker_group_R_ip_list;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_worker_group_R_ip_list()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_worker_group'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='ip_list')
    THEN
        ALTER TABLE t_ds_worker_group CHANGE COLUMN `ip_list` `addr_list` text;
        ALTER TABLE t_ds_worker_group MODIFY COLUMN `name` varchar(255) NOT NULL;
        ALTER TABLE t_ds_worker_group ADD UNIQUE KEY `name_unique` (`name`);
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_worker_group_R_ip_list;
DROP PROCEDURE uc_dolphin_T_t_ds_worker_group_R_ip_list;

-- uc_dolphin_T_qrtz_fired_triggers_R_entry_id
drop PROCEDURE if EXISTS uc_dolphin_T_qrtz_fired_triggers_R_entry_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_qrtz_fired_triggers_R_entry_id()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='QRTZ_FIRED_TRIGGERS'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='entry_id')
    THEN
        ALTER TABLE QRTZ_FIRED_TRIGGERS MODIFY COLUMN `entry_id` varchar(200);
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_qrtz_fired_triggers_R_entry_id;
DROP PROCEDURE uc_dolphin_T_qrtz_fired_triggers_R_entry_id;
-- create task group table
drop PROCEDURE if EXISTS create_t_ds_task_group;
delimiter d//
CREATE PROCEDURE create_t_ds_task_group()
BEGIN
DROP TABLE IF EXISTS t_ds_task_group;
CREATE TABLE `t_ds_task_group` (
                                   `id` INT ( 11 ) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                   `name` VARCHAR ( 100 ) DEFAULT NULL COMMENT 'task_group name',
                                   `description` VARCHAR ( 200 ) DEFAULT NULL,
                                   `group_size` INT ( 11 ) NOT NULL COMMENT '作业组大小',
                                   `use_size` INT ( 11 ) DEFAULT '0' COMMENT '已使用作业组大小',
                                   `user_id` INT ( 11 ) DEFAULT NULL COMMENT 'creator id',
                                   `project_id` INT ( 11 ) DEFAULT NULL COMMENT 'project id',
                                   `status` TINYINT ( 4 ) DEFAULT '1' COMMENT '0 not available, 1 available',
                                   `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                   `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                   PRIMARY KEY ( `id` )
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8;
END;

d//

delimiter ;
CALL create_t_ds_task_group;
DROP PROCEDURE create_t_ds_task_group;

-- create task group queue table
drop PROCEDURE if EXISTS create_task_group_queue;
delimiter d//
CREATE PROCEDURE create_task_group_queue()
BEGIN
DROP TABLE IF EXISTS t_ds_task_group_queue;
CREATE TABLE `t_ds_task_group_queue` (
                                         `id` INT ( 11 ) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                         `task_id` INT ( 11 ) DEFAULT NULL COMMENT 'taskIntanceid',
                                         `task_name` VARCHAR ( 100 ) DEFAULT NULL COMMENT 'TaskInstance name',
                                         `group_id` INT ( 11 ) DEFAULT NULL COMMENT 'taskGroup id',
                                         `process_id` INT ( 11 ) DEFAULT NULL COMMENT 'processInstace id',
                                         `priority` INT ( 8 ) DEFAULT '0' COMMENT '优先级',
                                         `status` TINYINT ( 4 ) DEFAULT '-1' COMMENT '-1排队  1正在执行  6失败  7成功',
                                         `create_time` datetime DEFAULT NULL COMMENT 'create time',
                                         `update_time` datetime DEFAULT NULL COMMENT 'update time',
                                         PRIMARY KEY ( `id` )
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8;
END;

d//

delimiter ;
CALL create_task_group_queue;
DROP PROCEDURE create_task_group_queue;


delimiter d//
CREATE PROCEDURE alter_t_ds_task_instance1()
BEGIN
ALTER TABLE t_ds_task_instance ADD COLUMN task_group_id int(11) DEFAULT -1;
END;

d//

delimiter ;
CALL alter_t_ds_task_instance1;
DROP PROCEDURE alter_t_ds_task_instance1;