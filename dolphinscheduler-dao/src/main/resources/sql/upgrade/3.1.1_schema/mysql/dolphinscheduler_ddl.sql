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
-- rename t_ds_fav_task task_name to task_type
drop procedure if exists modify_t_ds_fav_task_task_name;
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

-- alter table `t_ds_worker_group` add `description` varchar(256);
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

-- ALTER TABLE t_ds_worker_group ADD COLUMN description varchar(255) DEFAULT NULL COMMENT 'ds worker group description';
drop procedure if exists modify_t_ds_worker_group_description;
delimiter d//
CREATE PROCEDURE modify_t_ds_worker_group_description()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME='t_ds_worker_group'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND COLUMN_NAME='description')
    THEN
    alter table `t_ds_worker_group` add column `description` varchar(255) DEFAULT NULL COMMENT "ds worker group description";
ELSE
alter table `t_ds_worker_group` modify column `description` varchar(255) DEFAULT NULL COMMENT "ds worker group description";
END IF;
END;
d//
delimiter ;

call modify_t_ds_worker_group_description();
drop procedure if exists modify_t_ds_worker_group_description;
