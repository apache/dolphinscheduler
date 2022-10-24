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
--- rename t_ds_fav_task task_name to task_type
delimiter d//
CREATE PROCEDURE modify_t_ds_fav_task_task_name()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_fav_task'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='task_name')
    THEN
SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
ALTER TABLE `t_ds_fav_task` change `task_name` `task_type` varchar(64) NOT NULL COMMENT 'favorite task type name';
END IF;
END;
d//
delimiter ;
CALL modify_t_ds_fav_task_task_name;
DROP PROCEDURE modify_t_ds_fav_task_task_name;
