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
-- uc_dolphin_T_t_ds_process_definition_A_modify_by
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_A_modify_by;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_A_modify_by()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='modify_by')
   THEN
         ALTER TABLE t_ds_process_definition ADD `modify_by` varchar(36) DEFAULT '' COMMENT 'modify user';
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_A_modify_by;
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_A_modify_by;

-- ac_dolphin_T_t_ds_process_instance_A_worker_group
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_process_instance_A_worker_group;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_process_instance_A_worker_group()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group')
   THEN
         ALTER TABLE t_ds_process_instance ADD `worker_group` varchar(255) DEFAULT '' COMMENT 'worker group';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_process_instance_A_worker_group;
DROP PROCEDURE ac_dolphin_T_t_ds_process_instance_A_worker_group;

-- dc_dolphin_T_t_ds_process_instance_D_worker_group_id
drop PROCEDURE if EXISTS dc_dolphin_T_t_ds_process_instance_D_worker_group_id;
delimiter d//
CREATE PROCEDURE dc_dolphin_T_t_ds_process_instance_D_worker_group_id()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group_id')
   THEN
         ALTER TABLE t_ds_process_instance DROP COLUMN worker_group_id;
       END IF;
 END;

d//

delimiter ;
CALL dc_dolphin_T_t_ds_process_instance_D_worker_group_id;
DROP PROCEDURE dc_dolphin_T_t_ds_process_instance_D_worker_group_id;

-- ac_dolphin_T_t_ds_task_instance_A_worker_group
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_task_instance_A_worker_group;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_task_instance_A_worker_group()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group')
   THEN
         ALTER TABLE t_ds_task_instance ADD `worker_group` varchar(255) DEFAULT '' COMMENT 'worker group';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_task_instance_A_worker_group;
DROP PROCEDURE ac_dolphin_T_t_ds_task_instance_A_worker_group;

-- dc_dolphin_T_t_ds_task_instance_D_worker_group_id
drop PROCEDURE if EXISTS dc_dolphin_T_t_ds_task_instance_D_worker_group_id;
delimiter d//
CREATE PROCEDURE dc_dolphin_T_t_ds_task_instance_D_worker_group_id()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group_id')
   THEN
         ALTER TABLE t_ds_task_instance DROP COLUMN worker_group_id;
       END IF;
 END;

d//

delimiter ;
CALL dc_dolphin_T_t_ds_task_instance_D_worker_group_id;
DROP PROCEDURE dc_dolphin_T_t_ds_task_instance_D_worker_group_id;

-- ac_dolphin_T_t_ds_schedules_A_worker_group
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_schedules_A_worker_group;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_schedules_A_worker_group()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_schedules'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group')
   THEN
         ALTER TABLE t_ds_schedules ADD `worker_group` varchar(255) DEFAULT '' COMMENT 'worker group';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_schedules_A_worker_group;
DROP PROCEDURE ac_dolphin_T_t_ds_schedules_A_worker_group;

-- dc_dolphin_T_t_ds_schedules_D_worker_group_id
drop PROCEDURE if EXISTS dc_dolphin_T_t_ds_schedules_D_worker_group_id;
delimiter d//
CREATE PROCEDURE dc_dolphin_T_t_ds_schedules_D_worker_group_id()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_schedules'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='worker_group_id')
   THEN
         ALTER TABLE t_ds_schedules DROP COLUMN worker_group_id;
       END IF;
 END;

d//

delimiter ;
CALL dc_dolphin_T_t_ds_schedules_D_worker_group_id;
DROP PROCEDURE dc_dolphin_T_t_ds_schedules_D_worker_group_id;