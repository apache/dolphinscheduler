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

-- t_ds_dq_rule_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_t_ds_dq_rule_input_entry;
DROP PROCEDURE if EXISTS modify_t_ds_dq_rule_input_entry;
delimiter d//
CREATE PROCEDURE modify_t_ds_dq_rule_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_dq_rule_input_entry'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='value')
   THEN
       ALTER TABLE `t_ds_dq_rule_input_entry`
       CHANGE COLUMN `value` `data` varchar(255) DEFAULT NULL,
       CHANGE COLUMN `value_type` `data_type` int(11) DEFAULT NULL;
   END IF;
END;
d//
delimiter ;
CALL modify_t_ds_dq_rule_input_entry;
DROP PROCEDURE modify_t_ds_dq_rule_input_entry;