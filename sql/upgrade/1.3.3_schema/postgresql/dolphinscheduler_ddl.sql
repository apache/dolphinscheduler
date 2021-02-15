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
-- uc_dolphin_T_t_ds_task_instance_A_first_submit_time
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_first_submit_time() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='first_submit_time')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN first_submit_time timestamp DEFAULT NULL;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_first_submit_time();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_first_submit_time();


-- uc_dolphin_T_t_ds_task_instance_A_delay_time
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_delay_time() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='delay_time')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN delay_time int DEFAULT '0';
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_delay_time();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_delay_time();

-- uc_dolphin_T_t_ds_process_instance_A_var_pool
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_process_instance_A_var_pool() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_process_instance'
                            AND COLUMN_NAME ='var_pool')
      THEN
         ALTER TABLE t_ds_process_instance ADD COLUMN var_pool text;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_process_instance_A_var_pool();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_instance_A_var_pool();

-- uc_dolphin_T_t_ds_task_instance_A_var_pool
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_var_pool() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='var_pool')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN var_pool text;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_var_pool();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_var_pool();

-- uc_dolphin_T_t_ds_process_definition_A_modify_by
delimiter d//
CREATE OR REPLACE FUNCTION ct_dolphin_T_t_ds_process_definition_version() RETURNS void AS $$
BEGIN
CREATE TABLE IF NOT EXISTS t_ds_process_definition_version (
                                                 id int NOT NULL  ,
                                                 process_definition_id int NOT NULL  ,
                                                 version int DEFAULT NULL ,
                                                 process_definition_json text ,
                                                 description text ,
                                                 global_params text ,
                                                 locations text ,
                                                 connects text ,
                                                 receivers text ,
                                                 receivers_cc text ,
                                                 create_time timestamp DEFAULT NULL ,
                                                 timeout int DEFAULT '0' ,
                                                 resource_ids varchar(64),
                                                 PRIMARY KEY (id)
) ;
create index process_definition_id_and_version on t_ds_process_definition_version (process_definition_id,version);

END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT ct_dolphin_T_t_ds_process_definition_version();
DROP FUNCTION IF EXISTS ct_dolphin_T_t_ds_process_definition_version();




--  add t_ds_resources_un
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_resources_un() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (
                    SELECT 1 FROM information_schema.KEY_COLUMN_USAGE
                    WHERE  TABLE_NAME = 't_ds_resources'
                    AND CONSTRAINT_NAME = 't_ds_resources_un'
                )
    THEN
ALTER TABLE t_ds_resources ADD CONSTRAINT t_ds_resources_un UNIQUE (full_name,"type");
END IF;
END;
$$ LANGUAGE plpgsql;

SELECT uc_dolphin_T_t_ds_resources_un();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_resources_un();




