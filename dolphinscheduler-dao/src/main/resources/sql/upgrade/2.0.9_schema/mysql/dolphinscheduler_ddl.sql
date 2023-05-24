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

drop PROCEDURE if EXISTS t_ds_process_definition_add_column;
delimiter d//
CREATE PROCEDURE t_ds_process_definition_add_column()
BEGIN
 	IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_NAME='t_ds_process_definition'
            AND TABLE_SCHEMA=(SELECT DATABASE())
            AND COLUMN_NAME='execution_type')
   THEN
ALTER TABLE t_ds_process_definition ADD COLUMN `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority';
END IF;
END;
 d//
 delimiter ;
CALL t_ds_process_definition_add_column;
DROP PROCEDURE t_ds_process_definition_add_column;


-- t_ds_process_definition_log_add_column
drop PROCEDURE if EXISTS t_ds_process_definition_log_add_column;
delimiter d//
CREATE PROCEDURE t_ds_process_definition_log_add_column()
BEGIN
 	IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_NAME='t_ds_process_definition_log'
            AND TABLE_SCHEMA=(SELECT DATABASE())
            AND COLUMN_NAME='execution_type')
   THEN
ALTER TABLE t_ds_process_definition_log ADD COLUMN `execution_type` tinyint(4) DEFAULT '0' COMMENT 'execution_type 0:parallel,1:serial wait,2:serial discard,3:serial priority';
END IF;
END;
 d//
 delimiter ;
CALL t_ds_process_definition_log_add_column;
DROP PROCEDURE t_ds_process_definition_log_add_column;


-- t_ds_process_instance_add_column
drop PROCEDURE if EXISTS t_ds_process_instance_add_column;
delimiter d//
CREATE PROCEDURE t_ds_process_instance_add_column()
BEGIN
 	IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_NAME='t_ds_process_instance'
            AND TABLE_SCHEMA=(SELECT DATABASE())
            AND COLUMN_NAME='next_process_instance_id')
   THEN
ALTER TABLE t_ds_process_instance ADD COLUMN `next_process_instance_id` int(11) DEFAULT '0' COMMENT 'serial queue next processInstanceId';
END IF;
END;
 d//
 delimiter ;
CALL t_ds_process_instance_add_column;
DROP PROCEDURE t_ds_process_instance_add_column;
