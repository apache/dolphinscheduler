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

-- ac_dolphin_T_t_ds_process_instance_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_process_instance_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_process_instance_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_process_instance ADD COLUMN worker_group varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_process_instance_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_process_instance_A_worker_group();


-- ac_dolphin_T_t_ds_task_instance_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_task_instance_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_task_instance_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN worker_group varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_task_instance_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_task_instance_A_worker_group();

-- ac_dolphin_T_t_ds_process_instance_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_process_instance_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_schedules_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_schedules'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_schedules ADD COLUMN worker_group varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_schedules_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_schedules_A_worker_group();

