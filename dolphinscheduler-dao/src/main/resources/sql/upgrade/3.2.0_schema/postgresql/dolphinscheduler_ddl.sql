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
-- uc_dolphin_T_t_ds_command_R_test_flag
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_command_R_test_flag();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_command_R_test_flag() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_command'
          AND COLUMN_NAME ='test_flag')
      THEN
ALTER TABLE t_ds_command alter column test_flag type int DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_command_R_test_flag();
DROP FUNCTION uc_dolphin_T_t_ds_command_R_test_flag();

-- uc_dolphin_T_t_ds_error_command_R_test_flag
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_error_command_R_test_flag();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_error_command_R_test_flag() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_error_command'
          AND COLUMN_NAME ='test_flag')
      THEN
ALTER TABLE t_ds_error_command alter column test_flag type int DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_error_command_R_test_flag();
DROP FUNCTION uc_dolphin_T_t_ds_error_command_R_test_flag();

-- uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_datasource'
          AND COLUMN_NAME ='test_flag')
      THEN
ALTER TABLE t_ds_datasource alter column test_flag type int DEFAULT NULL;
ALTER TABLE t_ds_datasource alter column bind_test_id type int DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id();
DROP FUNCTION uc_dolphin_T_t_ds_datasource_R_test_flag_bind_test_id();

-- uc_dolphin_T_t_ds_process_instance_R_test_flag
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_instance_R_test_flag();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_process_instance_R_test_flag() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='test_flag')
      THEN
ALTER TABLE t_ds_process_instance alter column test_flag type int DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_process_instance_R_test_flag();
DROP FUNCTION uc_dolphin_T_t_ds_process_instance_R_test_flag();

-- uc_dolphin_T_t_ds_task_instance_R_test_flag
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_R_test_flag();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_task_instance_R_test_flag() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='test_flag')
      THEN
ALTER TABLE t_ds_task_instance alter column test_flag type int DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_task_instance_R_test_flag();
DROP FUNCTION uc_dolphin_T_t_ds_task_instance_R_test_flag();