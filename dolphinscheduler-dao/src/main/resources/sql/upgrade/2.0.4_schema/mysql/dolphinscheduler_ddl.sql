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

/************************************
 * Procedure
 ************************************/
delimiter d//

DROP PROCEDURE IF EXISTS create_index_if_not_exists d//
CREATE PROCEDURE create_index_if_not_exists (
    IN tableName varchar(128),
    IN indexName varchar(128),
    IN indexColumns varchar(128)
)
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND table_name = tableName AND index_name = indexName)
    THEN
        SET @sqlstmt = CONCAT('ALTER TABLE `', tableName , '` ADD KEY `', indexName, '` (', indexColumns, ') USING BTREE');
        PREPARE stmt FROM @sqlstmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END;

d//
delimiter ;

/************************************
 * DDL
 ************************************/
call create_index_if_not_exists('t_ds_task_instance', 'idx_code_version', 'task_code, task_definition_version');
call create_index_if_not_exists('t_ds_task_definition_log', 'idx_project_code', 'project_code');