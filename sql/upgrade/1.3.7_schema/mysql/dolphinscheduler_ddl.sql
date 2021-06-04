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

-- uc_dolphin_T_t_ds_datasource_R_note
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_datasource_R_note;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_datasource_R_note()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_datasource'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='note')
    THEN
        ALTER TABLE t_ds_datasource MODIFY COLUMN `note` varchar(255) DEFAULT NULL COMMENT 'description';
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_datasource_R_note;
DROP PROCEDURE uc_dolphin_T_t_ds_datasource_R_note;

-- uc_dolphin_T_t_ds_resources_R_description
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_resources_R_description;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_resources_R_description()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_resources'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='description')
    THEN
        ALTER TABLE t_ds_resources MODIFY COLUMN `description` varchar(255) DEFAULT NULL;
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_resources_R_description;
DROP PROCEDURE uc_dolphin_T_t_ds_resources_R_description;

-- uc_dolphin_T_t_ds_schedules_R_crontab
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_schedules_R_crontab;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_schedules_R_crontab()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_schedules'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='crontab')
    THEN
        ALTER TABLE t_ds_schedules MODIFY COLUMN `crontab` varchar(255) NOT NULL COMMENT 'crontab description';
        ALTER TABLE t_ds_schedules MODIFY COLUMN `worker_group` varchar(64) DEFAULT '' COMMENT 'worker group id';
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_schedules_R_crontab;
DROP PROCEDURE uc_dolphin_T_t_ds_schedules_R_crontab;

-- uc_dolphin_T_t_ds_tenant_R_description
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_tenant_R_description;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_tenant_R_description()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_tenant'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='description')
    THEN
        ALTER TABLE t_ds_tenant MODIFY COLUMN `description` varchar(255) DEFAULT NULL;
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_tenant_R_description;
DROP PROCEDURE uc_dolphin_T_t_ds_tenant_R_description;

-- uc_dolphin_T_t_ds_worker_group_R_name
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_worker_group_R_name;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_worker_group_R_name()
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_worker_group'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME ='name')
    THEN
        ALTER TABLE t_ds_worker_group MODIFY COLUMN `name` varchar(255) NOT NULL COMMENT 'worker group name';
    END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_worker_group_R_name;
DROP PROCEDURE uc_dolphin_T_t_ds_worker_group_R_name;
