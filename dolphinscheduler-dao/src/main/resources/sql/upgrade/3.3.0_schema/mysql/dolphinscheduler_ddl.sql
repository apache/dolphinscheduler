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
-- modify_data_t_ds_audit_log_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_data_t_ds_audit_log_input_entry;
DROP PROCEDURE if EXISTS modify_data_t_ds_audit_log_input_entry;
delimiter d//
CREATE PROCEDURE modify_data_t_ds_audit_log_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_audit_log'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='resource_type')
   THEN
    ALTER TABLE `t_ds_audit_log`
    drop resource_type, drop operation, drop resource_id,
      add `object_id` bigint(20) DEFAULT NULL COMMENT 'object id',
      add `object_name` varchar(100) DEFAULT NULL COMMENT 'object id',
      add `object_type` varchar(100) NOT NULL COMMENT 'object type',
      add `operation_type` varchar(100) NOT NULL COMMENT 'operation type',
      add `description` varchar(100) DEFAULT NULL COMMENT 'api description',
      add `latency` int(11) DEFAULT NULL COMMENT 'api cost milliseconds',
      add `detail` varchar(100) DEFAULT NULL COMMENT 'object change detail',
      MODIFY COLUMN `time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT "operation time";
END IF;
END;
d//
delimiter ;
CALL modify_data_t_ds_audit_log_input_entry;
DROP PROCEDURE modify_data_t_ds_audit_log_input_entry;
