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



drop procedure if exists add_column_if_not_exists;
delimiter d//
create procedure add_column_if_not_exists(target_table_name varchar(256), target_column varchar(256),
                                          add_statement varchar(256))
begin
    declare target_database varchar(256);
    select database() into target_database;
    IF EXISTS(SELECT *
              FROM information_schema.COLUMNS
              WHERE COLUMN_NAME = target_column
                AND TABLE_NAME = target_table_name
        ) THEN
        set @statement = concat('alter table ',target_table_name,' drop column ',target_column);
        PREPARE STMT FROM @statement;
        EXECUTE STMT;
    END IF;
    set @statement = concat(add_statement);
    PREPARE STMT FROM @statement;
    EXECUTE STMT;
end;
d//
delimiter ;

-- ALTER TABLE `t_ds_worker_group` ADD COLUMN `other_params_json` text DEFAULT NULL COMMENT 'other params json';
-- ALTER TABLE `t_ds_process_instance` ADD COLUMN `state_history` text DEFAULT NULL COMMENT 'state history desc' AFTER `state`;

call add_column_if_not_exists('t_ds_worker_group','other_params_json',"ALTER TABLE `t_ds_worker_group` ADD COLUMN `other_params_json` text DEFAULT NULL COMMENT 'other params json'");
call add_column_if_not_exists('t_ds_process_instance','state_history',"ALTER TABLE `t_ds_process_instance` ADD COLUMN `state_history` text DEFAULT NULL COMMENT 'state history desc' AFTER `state`");

drop procedure if exists add_column_if_not_exists;