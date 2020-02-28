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
-- ac_dolphin_T_t_ds_resources_A_pid
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_resources_A_pid;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_resources_A_pid()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_resources'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='pid')
   THEN
         ALTER TABLE t_ds_resources ADD `pid` int(11) DEFAULT -1 COMMENT 'parent id';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_resources_A_pid;
DROP PROCEDURE ac_dolphin_T_t_ds_resources_A_pid;

-- ac_dolphin_T_t_ds_resources_A_full_name
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_resources_A_full_name;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_resources_A_full_name()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_resources'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='full_name')
   THEN
         ALTER TABLE t_ds_resources ADD `full_name` varchar(255) DEFAULT NULL COMMENT 'full name';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_resources_A_full_name;
DROP PROCEDURE ac_dolphin_T_t_ds_resources_A_full_name;

-- ac_dolphin_T_t_ds_resources_A_pid
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_resources_is_directory;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_resources_is_directory()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_resources'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='is_directory')
   THEN
         ALTER TABLE t_ds_resources ADD `is_directory` tinyint(1) DEFAULT 0 COMMENT 'is directory';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_resources_is_directory;
DROP PROCEDURE ac_dolphin_T_t_ds_resources_is_directory;

-- ac_dolphin_T_t_ds_process_definition_A_resource_ids
drop PROCEDURE if EXISTS ac_dolphin_T_t_ds_process_definition_A_resource_ids;
delimiter d//
CREATE PROCEDURE ac_dolphin_T_t_ds_process_definition_A_resource_ids()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='resource_ids')
   THEN
         ALTER TABLE t_ds_process_definition ADD `resource_ids` varchar(255) DEFAULT NULL COMMENT 'resource ids';
       END IF;
 END;

d//

delimiter ;
CALL ac_dolphin_T_t_ds_process_definition_A_resource_ids;
DROP PROCEDURE ac_dolphin_T_t_ds_process_definition_A_resource_ids;