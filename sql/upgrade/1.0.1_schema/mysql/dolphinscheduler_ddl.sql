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
-- ac_escheduler_T_t_escheduler_queue_C_create_time
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_queue_C_create_time;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_queue_C_create_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_queue'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='create_time')
   THEN
         ALTER TABLE t_escheduler_queue ADD COLUMN create_time datetime DEFAULT NULL COMMENT 'create time' AFTER queue;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_queue_C_create_time;
DROP PROCEDURE ac_escheduler_T_t_escheduler_queue_C_create_time;


-- ac_escheduler_T_t_escheduler_queue_C_update_time
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_queue_C_update_time;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_queue_C_update_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_queue'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='update_time')
   THEN
         ALTER TABLE t_escheduler_queue ADD COLUMN update_time datetime DEFAULT NULL COMMENT 'update time' AFTER create_time;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_queue_C_update_time;
DROP PROCEDURE ac_escheduler_T_t_escheduler_queue_C_update_time;
