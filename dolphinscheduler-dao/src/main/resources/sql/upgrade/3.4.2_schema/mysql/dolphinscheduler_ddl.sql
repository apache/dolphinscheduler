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

drop PROCEDURE if EXISTS add_task_logs_root_path_to_t_ds_task_instance;
delimiter d//
CREATE PROCEDURE add_task_logs_root_path_to_t_ds_task_instance()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_task_instance'
          AND TABLE_SCHEMA=(SELECT DATABASE())
          AND COLUMN_NAME='task_logs_root_path')
    THEN
        ALTER TABLE `t_ds_task_instance`
            ADD COLUMN `task_logs_root_path` varchar(1024) DEFAULT NULL COMMENT 'task logs root path' AFTER `execute_path`;
    END IF;
END;
d//
delimiter ;
call add_task_logs_root_path_to_t_ds_task_instance();
drop PROCEDURE if EXISTS add_task_logs_root_path_to_t_ds_task_instance;
