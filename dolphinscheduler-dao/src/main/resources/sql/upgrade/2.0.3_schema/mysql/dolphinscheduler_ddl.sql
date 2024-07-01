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

drop procedure if exists re_add_index;
delimiter d//
create procedure re_add_index(target_table_name varchar(256),
                              target_index_type varchar(8), target_index_name varchar(256),
                              target_columns varchar(512), using_str varchar(256))
begin
    declare target_database varchar(256);
    select database() into target_database;
    IF EXISTS(SELECT *
              FROM information_schema.statistics
              WHERE table_schema = target_database
                AND table_name = target_table_name
                AND index_name = target_index_name) THEN
        set @statement = concat('drop index ', target_index_name, ' on ', target_table_name);
        PREPARE STMT FROM @statement;
        EXECUTE STMT;
    END IF;
    set @statement =
            concat('alter table ', target_table_name, ' add ', target_index_type, ' ', target_index_name,
                   '(', target_columns,
                   ') ', using_str);
    PREPARE STMT FROM @statement;
    EXECUTE STMT;
end;
d//
delimiter ;

ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_params` longtext COMMENT 'job custom parameters' AFTER `app_link`;

call re_add_index('t_ds_process_task_relation','KEY','idx_code', '`project_code`, `process_definition_code`', 'USING BTREE');
call re_add_index('t_ds_process_task_relation_log','KEY','idx_process_code_version','`process_definition_code`,`process_definition_version`', 'USING BTREE');
call re_add_index('t_ds_task_definition_log','INDEX','idx_code_version','`code`,`version`', 'USING BTREE');

drop procedure if exists re_add_index;