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
ALTER TABLE t_ds_command alter column test_flag type int;
ALTER TABLE t_ds_command alter column test_flag set DEFAULT NULL;
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
ALTER TABLE t_ds_error_command alter column test_flag type int;
ALTER TABLE t_ds_error_command alter column test_flag set DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_error_command_R_test_flag();
DROP FUNCTION uc_dolphin_T_t_ds_error_command_R_test_flag();

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
ALTER TABLE t_ds_process_instance alter column test_flag type int;
ALTER TABLE t_ds_process_instance alter column test_flag set DEFAULT NULL;
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
ALTER TABLE t_ds_task_instance alter column test_flag type int;
ALTER TABLE t_ds_task_instance alter column test_flag set DEFAULT NULL;
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
ALTER TABLE t_ds_process_instance ADD project_code bigint;
ALTER TABLE t_ds_process_instance alter column project_code set DEFAULT NULL;
comment on column t_ds_process_instance.project_code is 'project code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='executor_name')
      THEN
ALTER TABLE t_ds_process_instance ADD executor_name varchar(64);
ALTER TABLE t_ds_process_instance alter column executor_name set DEFAULT NULL;
comment on column t_ds_process_instance.executor_name is 'execute user name';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_process_instance'
          AND COLUMN_NAME ='tenant_code')
      THEN
ALTER TABLE t_ds_process_instance ADD tenant_code varchar(64);
ALTER TABLE t_ds_process_instance alter column tenant_code set DEFAULT NULL;
comment on column t_ds_process_instance.tenant_code is 'tenant code';
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
ALTER TABLE t_ds_task_instance ADD process_instance_name varchar(255);
ALTER TABLE t_ds_task_instance alter column process_instance_name set DEFAULT NULL;
comment on column t_ds_task_instance.process_instance_name is 'process instance name';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='project_code')
      THEN
ALTER TABLE t_ds_process_instance ADD project_code bigint;
ALTER TABLE t_ds_process_instance alter column project_code set DEFAULT NULL;
comment on column t_ds_process_instance.project_code is 'project code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_task_instance'
          AND COLUMN_NAME ='executor_name')
      THEN
ALTER TABLE t_ds_task_instance ADD executor_name varchar(64);
ALTER TABLE t_ds_task_instance alter column executor_name set DEFAULT NULL;
comment on column t_ds_task_instance.executor_name is 'execute user name';
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select add_t_ds_task_instance_add_project_code();
DROP FUNCTION add_t_ds_task_instance_add_project_code();

ALTER TABLE t_ds_alert alter column title type varchar(512);
ALTER TABLE t_ds_alert alter column title set DEFAULT NULL;
ALTER TABLE t_ds_command alter column worker_group type varchar(255);
ALTER TABLE t_ds_project alter column name type varchar(255);
ALTER TABLE t_ds_project alter column name set DEFAULT NULL;
ALTER TABLE t_ds_schedules alter column worker_group type varchar(255);
ALTER TABLE t_ds_task_instance alter column worker_group type varchar(255);
ALTER TABLE t_ds_udfs alter column func_name type varchar(255);
ALTER TABLE t_ds_udfs alter column func_name set NOT NULL ;
ALTER TABLE t_ds_version alter column version type varchar(63);
ALTER TABLE t_ds_version alter column version set NOT NULL;
ALTER TABLE t_ds_plugin_define alter column plugin_name type varchar(255);
ALTER TABLE t_ds_plugin_define alter column plugin_name set NOT NULL;
ALTER TABLE t_ds_plugin_define alter column plugin_type type varchar(63);
ALTER TABLE t_ds_plugin_define alter column plugin_type set NOT NULL;
ALTER TABLE t_ds_alert_plugin_instance alter column instance_name type varchar(255);
ALTER TABLE t_ds_alert_plugin_instance alter column instance_name set DEFAULT NULL;
ALTER TABLE t_ds_dq_rule alter column name type varchar(255);
ALTER TABLE t_ds_dq_rule alter column name set DEFAULT NULL;
ALTER TABLE t_ds_environment alter column name type varchar(255);
ALTER TABLE t_ds_environment alter column name set DEFAULT NULL;
ALTER TABLE t_ds_task_group_queue alter column task_name type VARCHAR(255);
ALTER TABLE t_ds_task_group_queue alter column task_name set DEFAULT NULL ;
ALTER TABLE t_ds_task_group alter column name type varchar(255);
ALTER TABLE t_ds_task_group alter column name set DEFAULT NULL ;
ALTER TABLE t_ds_k8s alter column k8s_name type VARCHAR(255);
ALTER TABLE t_ds_k8s alter column k8s_name set DEFAULT NULL ;
ALTER TABLE t_ds_k8s_namespace alter column namespace type varchar(255);
ALTER TABLE t_ds_k8s_namespace alter column namespace set DEFAULT NULL;
ALTER TABLE t_ds_cluster alter column name type varchar(255);
ALTER TABLE t_ds_cluster alter column name set DEFAULT NULL;

-- tenant improvement
delimiter ;
DROP FUNCTION IF EXISTS add_improvement_workflow_run_tenant();
delimiter d//
CREATE FUNCTION add_improvement_workflow_run_tenant() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_command'
          AND COLUMN_NAME ='tenant_code')
      THEN
ALTER TABLE t_ds_command ADD tenant_code varchar(64);
ALTER TABLE t_ds_command alter column tenant_code set DEFAULT 'default';
comment on column t_ds_command.tenant_code is 'tenant code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_error_command'
          AND COLUMN_NAME ='tenant_code')
      THEN
ALTER TABLE t_ds_error_command ADD tenant_code varchar(64);
ALTER TABLE t_ds_error_command alter column tenant_code set DEFAULT 'default';
comment on column t_ds_error_command.tenant_code is 'tenant code';
END IF;
IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_schedules'
          AND COLUMN_NAME ='tenant_code')
      THEN
ALTER TABLE t_ds_schedules ADD tenant_code varchar(64);
ALTER TABLE t_ds_schedules alter column tenant_code  set DEFAULT NULL;
comment on column t_ds_schedules.tenant_code is 'tenant code';
END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select add_improvement_workflow_run_tenant();
DROP FUNCTION add_improvement_workflow_run_tenant();

-- uc_dolphin_T_t_ds_relation_sub_workflow
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_relation_sub_workflow()
RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='t_ds_relation_sub_workflow'
        AND table_schema=current_schema()
    ) THEN
CREATE TABLE t_ds_relation_sub_workflow (
                                            id        serial      NOT NULL,
                                            parent_workflow_instance_id BIGINT NOT NULL,
                                            parent_task_code BIGINT NOT NULL,
                                            sub_workflow_instance_id BIGINT NOT NULL,
                                            PRIMARY KEY (id)
);
CREATE INDEX idx_parent_workflow_instance_id ON t_ds_relation_sub_workflow (parent_workflow_instance_id);
CREATE INDEX idx_parent_task_code ON t_ds_relation_sub_workflow (parent_task_code);
CREATE INDEX idx_sub_workflow_instance_id ON t_ds_relation_sub_workflow (sub_workflow_instance_id);
END IF;
END;
$$ LANGUAGE plpgsql;
