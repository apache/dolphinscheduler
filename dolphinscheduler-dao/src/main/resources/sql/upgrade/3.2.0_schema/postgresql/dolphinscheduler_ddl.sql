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

delimiter d//
DROP TABLE IF EXISTS t_ds_trigger_relation;
CREATE TABLE t_ds_trigger_relation (
    id        serial      NOT NULL,
    trigger_type int NOT NULL,
    trigger_code bigint NOT NULL,
    job_id bigint NOT NULL,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT t_ds_trigger_relation_unique UNIQUE (trigger_type,job_id,trigger_code)
);
d//
delimiter ;

ALTER TABLE t_ds_task_definition DROP COLUMN IF EXISTS is_cache;
ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';

ALTER TABLE t_ds_task_definition_log DROP COLUMN IF EXISTS is_cache;
ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';

ALTER TABLE t_ds_task_instance DROP COLUMN IF EXISTS is_cache;
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS cache_key varchar(200) DEFAULT NULL;
ALTER TABLE t_ds_task_instance DROP COLUMN IF EXISTS cacke_key;

CREATE INDEX IF NOT EXISTS idx_cache_key ON t_ds_task_instance USING Btree("cache_key");

-- add_t_ds_process_instance_add_project_code
delimiter ;
DROP FUNCTION IF EXISTS add_t_ds_process_instance_add_project_code();
delimiter d//
CREATE FUNCTION add_t_ds_process_instance_add_project_code() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='project_code')
      THEN
ALTER TABLE t_ds_process_instance ADD `project_code` bigint DEFAULT NULL COMMENT 'project code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='executor_name')
      THEN
ALTER TABLE t_ds_process_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='tenant_code')
      THEN
ALTER TABLE t_ds_process_instance ADD `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code';
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select add_t_ds_process_instance_add_project_code();
DROP FUNCTION add_t_ds_process_instance_add_project_code();

-- add_t_ds_process_instance_add_project_code
delimiter ;
DROP FUNCTION IF EXISTS add_t_ds_task_instance_add_project_code();
delimiter d//
CREATE FUNCTION add_t_ds_task_instance_add_project_code() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='process_instance_name')
      THEN
ALTER TABLE t_ds_task_instance ADD `process_instance_name` varchar(255) DEFAULT NULL COMMENT 'process instance name';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='project_code')
      THEN
ALTER TABLE t_ds_process_instance ADD `project_code` bigint DEFAULT NULL COMMENT 'project code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='executor_name')
      THEN
ALTER TABLE t_ds_task_instance ADD `executor_name` varchar(64) DEFAULT NULL COMMENT 'execute user name';
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select add_t_ds_task_instance_add_project_code();
DROP FUNCTION add_t_ds_task_instance_add_project_code();

ALTER TABLE `t_ds_alert`
    MODIFY `title` varchar(512) DEFAULT NULL ;