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


-- uc_dolphin_T_t_ds_process_instance_A_restart_time
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_instance_A_restart_time;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_instance_A_restart_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='restart_time')
   THEN
         ALTER TABLE t_ds_process_instance ADD COLUMN `restart_time` datetime DEFAULT NULL COMMENT 'process instance restart time';
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_instance_A_restart_time();
DROP PROCEDURE uc_dolphin_T_t_ds_process_instance_A_restart_time;


-- uc_dolphin_T_t_ds_process_task_relation_A_pc_pd_index
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_task_relation_A_pc_pd_index;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_task_relation_A_pc_pd_index()
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
           WHERE TABLE_NAME='t_ds_process_task_relation'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND INDEX_NAME ='project_code_process_definition_code_index')
   THEN
ALTER TABLE `t_ds_process_task_relation` ADD KEY `project_code_process_definition_code_index`(`project_code`,`process_definition_code`) USING BTREE;
END IF;
END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_task_relation_A_pc_pd_index();
DROP PROCEDURE uc_dolphin_T_t_ds_process_task_relation_A_pc_pd_index;