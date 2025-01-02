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

DROP TABLE IF EXISTS t_ds_workflow_task_lineage;
CREATE TABLE t_ds_workflow_task_lineage (
    id int NOT NULL,
    workflow_definition_code bigint NOT NULL DEFAULT 0,
    workflow_definition_version int NOT NULL DEFAULT 0,
    task_definition_code bigint NOT NULL DEFAULT 0,
    task_definition_version int NOT NULL DEFAULT 0,
    dept_project_code bigint NOT NULL DEFAULT 0,
    dept_workflow_definition_code bigint NOT NULL DEFAULT 0,
    dept_task_definition_code bigint NOT NULL DEFAULT 0,
    create_time timestamp NOT NULL DEFAULT current_timestamp,
    update_time timestamp NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

create index idx_workflow_code_version on t_ds_workflow_task_lineage (workflow_definition_code,workflow_definition_version);
create index idx_task_code_version on t_ds_workflow_task_lineage (task_definition_code,task_definition_version);
create index idx_dept_code on t_ds_workflow_task_lineage (dept_project_code,dept_workflow_definition_code,dept_task_definition_code);

DROP TABLE IF EXISTS t_ds_jdbc_registry_data;
create table t_ds_jdbc_registry_data
(
    id               bigserial not null,
    data_key         varchar   not null,
    data_value       text      not null,
    data_type        varchar   not null,
    client_id        bigint    not null,
    create_time      timestamp not null default current_timestamp,
    last_update_time timestamp not null default current_timestamp,
    primary key (id)
);
create unique index uk_t_ds_jdbc_registry_dataKey on t_ds_jdbc_registry_data (data_key);


DROP TABLE IF EXISTS t_ds_jdbc_registry_lock;
create table t_ds_jdbc_registry_lock
(
    id          bigserial not null,
    lock_key    varchar   not null,
    lock_owner  varchar   not null,
    client_id   bigint    not null,
    create_time timestamp not null default current_timestamp,
    primary key (id)
);
create unique index uk_t_ds_jdbc_registry_lockKey on t_ds_jdbc_registry_lock (lock_key);


DROP TABLE IF EXISTS t_ds_jdbc_registry_client_heartbeat;
create table t_ds_jdbc_registry_client_heartbeat
(
    id                  bigint    not null,
    client_name         varchar   not null,
    last_heartbeat_time bigint    not null,
    connection_config   text      not null,
    create_time         timestamp not null default current_timestamp,
    primary key (id)
);

DROP TABLE IF EXISTS t_ds_jdbc_registry_data_change_event;
create table t_ds_jdbc_registry_data_change_event
(
    id                 bigserial not null,
    event_type         varchar   not null,
    jdbc_registry_data text      not null,
    create_time        timestamp not null default current_timestamp,
    primary key (id)
);

DROP TABLE IF EXISTS t_ds_listener_event;

-- drop_column_t_ds_alert_plugin_instance
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_alert_plugin_instance() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_alert_plugin_instance'
                  AND column_name = 'instance_type')
      THEN
ALTER TABLE t_ds_alert_plugin_instance
    DROP COLUMN "instance_type";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_alert_plugin_instance();
DROP FUNCTION IF EXISTS drop_column_t_ds_alert_plugin_instance();


-- drop_column_t_ds_alert_plugin_instance
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_alert_plugin_instance() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_alert_plugin_instance'
                  AND column_name = 'warning_type')
      THEN
ALTER TABLE t_ds_alert_plugin_instance
DROP COLUMN "warning_type";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_alert_plugin_instance();
DROP FUNCTION IF EXISTS drop_column_t_ds_alert_plugin_instance();

DROP TABLE IF EXISTS t_ds_trigger_relation;

-- Rename tables and fields from process to workflow
delimiter d//
CREATE OR REPLACE FUNCTION rename_tables_and_fields_from_process_to_workflow() RETURNS void AS $$
BEGIN

ALTER TABLE t_ds_alert RENAME COLUMN process_definition_code TO workflow_definition_code;
ALTER TABLE t_ds_alert RENAME COLUMN process_instance_id TO workflow_instance_id;

ALTER TABLE t_ds_command RENAME COLUMN process_definition_code TO workflow_definition_code;
ALTER TABLE t_ds_command RENAME COLUMN process_instance_priority TO workflow_instance_priority;
ALTER TABLE t_ds_command RENAME COLUMN process_instance_id TO workflow_instance_id;
ALTER TABLE t_ds_command RENAME COLUMN process_definition_version TO workflow_definition_version;

ALTER TABLE t_ds_error_command RENAME COLUMN process_definition_code TO workflow_definition_code;
ALTER TABLE t_ds_error_command RENAME COLUMN process_instance_priority TO workflow_instance_priority;
ALTER TABLE t_ds_error_command RENAME COLUMN process_instance_id TO workflow_instance_id;
ALTER TABLE t_ds_error_command RENAME COLUMN process_definition_version TO workflow_definition_version;

ALTER TABLE t_ds_process_task_relation RENAME COLUMN process_definition_version TO workflow_definition_version;
ALTER TABLE t_ds_process_task_relation RENAME COLUMN process_definition_code TO workflow_definition_code;

ALTER TABLE t_ds_process_task_relation_log RENAME COLUMN process_definition_version TO workflow_definition_version;
ALTER TABLE t_ds_process_task_relation_log RENAME COLUMN process_definition_code TO workflow_definition_code;

ALTER TABLE t_ds_process_instance RENAME COLUMN process_definition_code TO workflow_definition_code;
ALTER TABLE t_ds_process_instance RENAME COLUMN process_definition_version TO workflow_definition_version;
ALTER TABLE t_ds_process_instance RENAME COLUMN is_sub_process TO is_sub_workflow;
ALTER TABLE t_ds_process_instance RENAME COLUMN process_instance_priority TO workflow_instance_priority;
ALTER TABLE t_ds_process_instance RENAME COLUMN next_process_instance_id TO next_workflow_instance_id;

ALTER TABLE t_ds_schedules RENAME COLUMN process_definition_code TO workflow_definition_code;
ALTER TABLE t_ds_schedules RENAME COLUMN process_instance_priority TO workflow_instance_priority;

ALTER TABLE t_ds_task_instance RENAME COLUMN process_instance_id TO workflow_instance_id;
ALTER TABLE t_ds_task_instance RENAME COLUMN process_instance_name TO workflow_instance_name;

ALTER TABLE t_ds_dq_execute_result RENAME COLUMN process_definition_id TO workflow_definition_id;
ALTER TABLE t_ds_dq_execute_result RENAME COLUMN process_instance_id TO workflow_instance_id;

ALTER TABLE t_ds_dq_task_statistics_value RENAME COLUMN process_definition_id TO workflow_definition_id;

ALTER TABLE t_ds_task_group_queue RENAME COLUMN process_id TO workflow_instance_id;

ALTER TABLE t_ds_relation_process_instance RENAME COLUMN parent_process_instance_id TO parent_workflow_instance_id;
ALTER TABLE t_ds_relation_process_instance RENAME COLUMN process_instance_id TO workflow_instance_id;

ALTER TABLE t_ds_process_definition RENAME TO t_ds_workflow_definition;
ALTER TABLE t_ds_process_definition_log RENAME TO t_ds_workflow_definition_log;
ALTER TABLE t_ds_process_task_relation RENAME TO t_ds_workflow_task_relation;
ALTER TABLE t_ds_process_task_relation_log RENAME TO t_ds_workflow_task_relation_log;
ALTER TABLE t_ds_process_instance RENAME TO t_ds_workflow_instance;
ALTER TABLE t_ds_relation_process_instance RENAME TO t_ds_relation_workflow_instance;

ALTER SEQUENCE t_ds_relation_process_instance_id_sequence RENAME TO t_ds_relation_workflow_instance_id_sequence;
ALTER SEQUENCE t_ds_process_definition_id_sequence RENAME TO t_ds_workflow_definition_id_sequence;
ALTER SEQUENCE t_ds_process_definition_log_id_sequence RENAME TO t_ds_workflow_definition_log_id_sequence;
ALTER SEQUENCE t_ds_process_instance_id_sequence RENAME TO t_ds_workflow_instance_id_sequence;
ALTER SEQUENCE t_ds_process_task_relation_id_sequence RENAME TO t_ds_workflow_task_relation_id_sequence;
ALTER SEQUENCE t_ds_process_task_relation_log_id_sequence RENAME TO t_ds_workflow_task_relation_log_id_sequence;

ALTER INDEX "idx_relation_process_instance_parent_process_task" RENAME TO "idx_relation_workflow_instance_parent_workflow_task";
ALTER INDEX "idx_relation_process_instance_process_instance_id" RENAME TO "idx_relation_workflow_instance_workflow_instance_id";
ALTER INDEX "process_definition_index" RENAME TO "workflow_definition_index";
ALTER INDEX "process_definition_unique" RENAME TO "workflow_definition_unique";
ALTER TABLE "t_ds_workflow_instance" RENAME COLUMN "process_instance_json" TO "workflow_instance_json";
ALTER INDEX "process_instance_index" RENAME TO "workflow_instance_index";
ALTER INDEX "process_task_relation_idx_post_task_code_version" RENAME TO "workflow_task_relation_idx_post_task_code_version";
ALTER INDEX "process_task_relation_idx_pre_task_code_version" RENAME TO "workflow_task_relation_idx_pre_task_code_version";
ALTER INDEX "process_task_relation_idx_project_code_process_definition_code" RENAME TO "workflow_task_relation_idx_project_code_workflow_definition_cod";
ALTER INDEX "process_task_relation_log_idx_project_code_process_definition_c" RENAME TO "workflow_task_relation_log_idx_project_code_workflow_definition";

END;
$$ LANGUAGE plpgsql;
d//

select rename_tables_and_fields_from_process_to_workflow();
DROP FUNCTION IF EXISTS rename_tables_and_fields_from_process_to_workflow();


-- Drop data quality tables
delimiter d//
CREATE OR REPLACE FUNCTION drop_data_quality_tables() RETURNS void AS $$
BEGIN

DROP TABLE IF EXISTS t_ds_dq_comparison_type;
DROP TABLE IF EXISTS t_ds_dq_rule_execute_sql;
DROP TABLE IF EXISTS t_ds_dq_rule_input_entry;
DROP TABLE IF EXISTS t_ds_dq_task_statistics_value;
DROP TABLE IF EXISTS t_ds_dq_execute_result;
DROP TABLE IF EXISTS t_ds_dq_rule;
DROP TABLE IF EXISTS t_ds_relation_rule_input_entry;
DROP TABLE IF EXISTS t_ds_relation_rule_execute_sql;

END;
$$ LANGUAGE plpgsql;
d//

select drop_data_quality_tables();
DROP FUNCTION IF EXISTS drop_data_quality_tables();

create index workflow_definition_index_project_code on t_ds_workflow_definition (project_code);
create index workflow_definition_log_index_project_code on t_ds_workflow_definition_log (project_code);

-- drop_column_t_ds_worker_group other_params_json
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_worker_group_other_params_json() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_worker_group'
                  AND column_name = 'other_params_json')
      THEN
ALTER TABLE t_ds_worker_group
DROP COLUMN "other_params_json";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_worker_group_other_params_json();
DROP FUNCTION IF EXISTS drop_column_t_ds_worker_group_other_params_json();

-- drop_column_t_ds_task_definition is_cache
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_definition_is_cache() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_definition'
                  AND column_name = 'is_cache')
      THEN
ALTER TABLE t_ds_task_definition
DROP COLUMN "is_cache";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_definition_is_cache();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_definition_is_cache();

-- drop_column_t_ds_task_definition cache_key
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_definition_cache_key() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_definition'
                  AND column_name = 'cache_key')
      THEN
ALTER TABLE t_ds_task_definition
DROP COLUMN "cache_key";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_definition_cache_key();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_definition_cache_key();

-- drop_column_t_ds_task_definition_log is_cache
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_definition_log_is_cache() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_definition_log'
                  AND column_name = 'is_cache')
      THEN
ALTER TABLE t_ds_task_definition_log
DROP COLUMN "is_cache";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_definition_log_is_cache();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_definition_log_is_cache();

-- drop_column_t_ds_task_definition_log cache_key
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_definition_log_cache_key() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_definition_log'
                  AND column_name = 'cache_key')
      THEN
ALTER TABLE t_ds_task_definition_log
DROP COLUMN "cache_key";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_definition_log_cache_key();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_definition_log_cache_key();

-- drop_column_t_ds_task_instance is_cache
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_instance_is_cache() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_instance'
                  AND column_name = 'is_cache')
      THEN
ALTER TABLE t_ds_task_instance
DROP COLUMN "is_cache";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_instance_is_cache();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_instance_is_cache();

-- drop_column_t_ds_task_instance cache_key
delimiter d//
CREATE OR REPLACE FUNCTION drop_column_t_ds_task_instance_cache_key() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_task_instance'
                  AND column_name = 'cache_key')
      THEN
ALTER TABLE t_ds_task_instance
DROP COLUMN "cache_key";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select drop_column_t_ds_task_instance_cache_key();
DROP FUNCTION IF EXISTS drop_column_t_ds_task_instance_cache_key();

DROP TABLE IF EXISTS t_ds_task_instance_context;
CREATE TABLE t_ds_task_instance_context (
    id int NOT NULL,
    task_instance_id int NOT NULL,
    context text NOT NULL,
    context_type varchar(200) NOT NULL,
    create_time timestamp NOT NULL,
    update_time timestamp NOT NULL,
    PRIMARY KEY (id)
);

create unique index idx_task_instance_id on t_ds_task_instance_context (task_instance_id, context_type);
