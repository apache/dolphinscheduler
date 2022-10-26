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
call add_column_safety('t_ds_worker_group','description', 'varchar(255)' , "DEFAULT NULL COMMENT 'ds worker group description'");
drop procedure if exists add_column_safety;