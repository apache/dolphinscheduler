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
         ALTER TABLE t_ds_task_instance ALTER COLUMN app_link type text;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_C_app_link();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_C_app_link();


-- ac_dolphin_T_t_ds_resources_A_pid
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_pid() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='pid')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN pid int DEFAULT -1;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_pid();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_pid();

-- ac_dolphin_T_t_ds_resources_A_full_name
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_resources_A_full_name();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_full_name() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='full_name')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN full_name varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_full_name();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_full_name();

-- ac_dolphin_T_t_ds_resources_A_is_directory
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_resources_A_is_directory();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_is_directory() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='is_directory')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN is_directory boolean DEFAULT false;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_is_directory();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_is_directory();

-- ac_dolphin_T_t_ds_process_definition_A_resource_ids
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_process_definition_A_resource_ids();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_process_definition_A_resource_ids() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_definition'
          AND COLUMN_NAME ='resource_ids')
      THEN
         ALTER TABLE t_ds_process_definition ADD COLUMN resource_ids varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_process_definition_A_resource_ids();
DROP FUNCTION ac_dolphin_T_t_ds_process_definition_A_resource_ids();


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
         ALTER TABLE t_ds_process_instance ADD COLUMN worker_group varchar(64) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_process_instance_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_process_instance_A_worker_group();

-- dc_dolphin_T_t_ds_process_instance_D_worker_group_id
delimiter ;
DROP FUNCTION IF EXISTS dc_dolphin_T_t_ds_process_instance_D_worker_group_id();
delimiter d//
CREATE FUNCTION dc_dolphin_T_t_ds_process_instance_D_worker_group_id() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='worker_group_id')
      THEN
         ALTER TABLE t_ds_process_instance DROP COLUMN worker_group_id;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select dc_dolphin_T_t_ds_process_instance_D_worker_group_id();
DROP FUNCTION dc_dolphin_T_t_ds_process_instance_D_worker_group_id();


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
         ALTER TABLE t_ds_task_instance ADD COLUMN worker_group varchar(64) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_task_instance_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_task_instance_A_worker_group();

-- dc_dolphin_T_t_ds_task_instance_D_worker_group_id
delimiter ;
DROP FUNCTION IF EXISTS dc_dolphin_T_t_ds_task_instance_D_worker_group_id();
delimiter d//
CREATE FUNCTION dc_dolphin_T_t_ds_task_instance_D_worker_group_id() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='worker_group_id')
      THEN
         ALTER TABLE t_ds_task_instance DROP COLUMN worker_group_id;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select dc_dolphin_T_t_ds_task_instance_D_worker_group_id();
DROP FUNCTION dc_dolphin_T_t_ds_task_instance_D_worker_group_id();

-- ac_dolphin_T_t_ds_schedules_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_schedules_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_schedules_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_schedules'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_schedules ADD COLUMN worker_group varchar(64) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_schedules_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_schedules_A_worker_group();

-- dc_dolphin_T_t_ds_schedules_D_worker_group_id
delimiter ;
DROP FUNCTION IF EXISTS dc_dolphin_T_t_ds_schedules_D_worker_group_id();
delimiter d//
CREATE FUNCTION dc_dolphin_T_t_ds_schedules_D_worker_group_id() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_schedules'
          AND COLUMN_NAME ='worker_group_id')
      THEN
         ALTER TABLE t_ds_schedules DROP COLUMN worker_group_id;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select dc_dolphin_T_t_ds_schedules_D_worker_group_id();
DROP FUNCTION dc_dolphin_T_t_ds_schedules_D_worker_group_id();

-- ac_dolphin_T_t_ds_command_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_command_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_command_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_command'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_command ADD COLUMN worker_group varchar(64) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_command_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_command_A_worker_group();

-- dc_dolphin_T_t_ds_command_D_worker_group_id
delimiter ;
DROP FUNCTION IF EXISTS dc_dolphin_T_t_ds_command_D_worker_group_id();
delimiter d//
CREATE FUNCTION dc_dolphin_T_t_ds_command_D_worker_group_id() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_command'
          AND COLUMN_NAME ='worker_group_id')
      THEN
         ALTER TABLE t_ds_command DROP COLUMN worker_group_id;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select dc_dolphin_T_t_ds_command_D_worker_group_id();
DROP FUNCTION dc_dolphin_T_t_ds_command_D_worker_group_id();

-- ac_dolphin_T_t_ds_error_command_A_worker_group
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_error_command_A_worker_group();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_error_command_A_worker_group() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_error_command'
          AND COLUMN_NAME ='worker_group')
      THEN
         ALTER TABLE t_ds_error_command ADD COLUMN worker_group varchar(64) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_error_command_A_worker_group();
DROP FUNCTION ac_dolphin_T_t_ds_error_command_A_worker_group();

-- dc_dolphin_T_t_ds_error_command_D_worker_group_id
delimiter ;
DROP FUNCTION IF EXISTS dc_dolphin_T_t_ds_error_command_D_worker_group_id();
delimiter d//
CREATE FUNCTION dc_dolphin_T_t_ds_error_command_D_worker_group_id() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_error_command'
          AND COLUMN_NAME ='worker_group_id')
      THEN
         ALTER TABLE t_ds_error_command DROP COLUMN worker_group_id;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select dc_dolphin_T_t_ds_error_command_D_worker_group_id();
DROP FUNCTION dc_dolphin_T_t_ds_error_command_D_worker_group_id();


-- ac_dolphin_T_t_ds_user_A_state
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_user_A_state();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_user_A_state() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_user'
          AND COLUMN_NAME ='state')
      THEN
         ALTER TABLE t_ds_user ADD COLUMN state int DEFAULT 1;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_user_A_state();
DROP FUNCTION ac_dolphin_T_t_ds_user_A_state();