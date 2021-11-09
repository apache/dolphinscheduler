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

-- ac_escheduler_T_t_escheduler_process_definition_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_definition` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT 'tenant id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_instance` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT 'tenant id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
