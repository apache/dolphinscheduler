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
-- uc_dolphin_T_t_ds_process_definition_A_modify_by
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_process_definition_A_modify_by() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_process_definition'
                            AND COLUMN_NAME ='modify_by')
      THEN
         ALTER TABLE t_ds_process_definition ADD COLUMN modify_by varchar(36) DEFAULT '';
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_process_definition_A_modify_by();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_definition_A_modify_by();

-- uc_dolphin_T_t_ds_task_instance_A_executor_id
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_executor_id() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='executor_id')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN executor_id int DEFAULT NULL;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_executor_id();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_executor_id();

-- uc_dolphin_T_t_ds_task_instance_C_app_link
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_C_app_link() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='app_link')
      THEN
         ALTER TABLE t_ds_task_instance ALTER COLUMN app_link character varying(5999);
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_C_app_link();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_C_app_link();
