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

-- create table
CREATE TABLE IF NOT EXISTS t_ds_trigger_relation (
    id        serial      NOT NULL,
    trigger_type int NOT NULL,
    trigger_code bigint NOT NULL,
    job_id bigint NOT NULL,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT t_ds_trigger_relation_unique UNIQUE (trigger_type,job_id,trigger_code)
);
CREATE TABLE IF NOT EXISTS t_ds_relation_sub_workflow (
    id        serial      NOT NULL,
    parent_workflow_instance_id BIGINT NOT NULL,
    parent_task_code BIGINT NOT NULL,
    sub_workflow_instance_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE if not exists "t_ds_fav_task" (
    "id" serial NOT NULL,
    "task_type" VARCHAR(64) NOT NULL,
    "user_id" integer NOT NULL,
    PRIMARY KEY ("id")
);
CREATE TABLE if not exists "t_ds_project_preference" (
    "id" int NOT NULL,
    "code" bigint NOT NULL,
    "project_code" bigint NOT NULL,
    "preferences" VARCHAR(512) NOT NULL,
    "user_id" integer NULL,
    "state" integer NULL DEFAULT 1,
    "create_time" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);
CREATE TABLE if not exists "t_ds_project_parameter" (
    "id" int NOT NULL,
    "param_name" VARCHAR(255) NOT NULL,
    "param_value" VARCHAR(255) NOT NULL,
    "code" bigint NOT NULL,
    "project_code" bigint NOT NULL,
    "user_id" integer NULL,
    "create_time" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

-- add column, if you want to add constraint to the new column you should add them first
ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS cache_key varchar(200) DEFAULT NULL;
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS process_instance_name varchar(255);
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS executor_name varchar(64);
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS test_flag int;
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS project_code int;
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS project_code bigint;
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS executor_name varchar(64);
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS tenant_code varchar(64);
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS project_code bigint;
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS test_flag int;
ALTER TABLE t_ds_command ADD COLUMN IF NOT EXISTS tenant_code varchar(64);
ALTER TABLE t_ds_command ADD COLUMN IF NOT EXISTS test_flag int;
ALTER TABLE t_ds_error_command ADD COLUMN IF NOT EXISTS tenant_code varchar(64);
ALTER TABLE t_ds_error_command ADD COLUMN IF NOT EXISTS test_flag int;
ALTER TABLE t_ds_schedules ADD COLUMN IF NOT EXISTS tenant_code varchar(64);
ALTER TABLE t_ds_alert ADD COLUMN IF NOT EXISTS title varchar(512);
ALTER TABLE t_ds_command ADD COLUMN IF NOT EXISTS worker_group varchar(255);
ALTER TABLE t_ds_project ADD COLUMN IF NOT EXISTS name varchar(255);
ALTER TABLE t_ds_schedules ADD COLUMN IF NOT EXISTS worker_group varchar(255);
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS worker_group varchar(255);
ALTER TABLE t_ds_udfs ADD COLUMN IF NOT EXISTS func_name varchar(255);
ALTER TABLE t_ds_version ADD COLUMN IF NOT EXISTS version varchar(63);
ALTER TABLE t_ds_plugin_define ADD COLUMN IF NOT EXISTS plugin_name varchar(255);
ALTER TABLE t_ds_plugin_define ADD COLUMN IF NOT EXISTS plugin_type varchar(63);
ALTER TABLE t_ds_alert_plugin_instance ADD COLUMN IF NOT EXISTS instance_name varchar(255);
ALTER TABLE t_ds_dq_rule ADD COLUMN IF NOT EXISTS name varchar(255);
ALTER TABLE t_ds_environment ADD COLUMN IF NOT EXISTS name varchar(255);
ALTER TABLE t_ds_task_group_queue ADD COLUMN IF NOT EXISTS task_name VARCHAR(255);
ALTER TABLE t_ds_task_group ADD COLUMN IF NOT EXISTS name varchar(255);
ALTER TABLE t_ds_k8s ADD COLUMN IF NOT EXISTS k8s_name VARCHAR(255);
ALTER TABLE t_ds_k8s_namespace ADD COLUMN IF NOT EXISTS namespace varchar(255);
ALTER TABLE t_ds_cluster ADD COLUMN IF NOT EXISTS name varchar(255);
ALTER TABLE "t_ds_fav_task" ADD COLUMN IF NOT EXISTS "task_type" varchar(64) NOT NULL;

-- alter column
ALTER TABLE "t_ds_alert" ALTER COLUMN "title" TYPE VARCHAR(512);
ALTER TABLE "t_ds_alert_plugin_instance" ALTER COLUMN "instance_name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_cluster" ALTER COLUMN "name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_command" ALTER COLUMN "worker_group" TYPE VARCHAR(255), ALTER COLUMN "tenant_code" SET DEFAULT 'default';
ALTER TABLE "t_ds_dq_rule" ALTER COLUMN "name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_environment" ALTER COLUMN "name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_error_command" ALTER COLUMN "worker_group" TYPE VARCHAR(255), ALTER COLUMN "tenant_code" SET DEFAULT 'default';
ALTER TABLE "t_ds_k8s" ALTER COLUMN "k8s_name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_k8s_namespace" ALTER COLUMN "namespace" TYPE VARCHAR(255), ALTER COLUMN "code" DROP DEFAULT, ALTER COLUMN "cluster_code" DROP DEFAULT;
ALTER TABLE "t_ds_plugin_define" ALTER COLUMN "plugin_name" TYPE VARCHAR(255), ALTER COLUMN "plugin_type" TYPE VARCHAR(63);
ALTER TABLE "t_ds_process_instance" ALTER COLUMN "worker_group" TYPE VARCHAR(255), ALTER COLUMN "executor_name" SET DEFAULT NULL::VARCHAR, ALTER COLUMN "tenant_code" SET DEFAULT 'default';
ALTER TABLE "t_ds_project" ALTER COLUMN "name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_schedules" ALTER COLUMN "worker_group" TYPE VARCHAR(255), ALTER COLUMN "tenant_code" SET DEFAULT 'default';
ALTER TABLE "t_ds_task_group" ALTER COLUMN "name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_task_group_queue" ALTER COLUMN "task_name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_task_instance" ALTER COLUMN "worker_group" TYPE VARCHAR(255), ALTER COLUMN "process_instance_name" SET DEFAULT NULL::VARCHAR, ALTER COLUMN "executor_name" SET DEFAULT NULL::VARCHAR, ALTER COLUMN "project_code" TYPE bigint;
ALTER TABLE "t_ds_udfs" ALTER COLUMN "func_name" TYPE VARCHAR(255);
ALTER TABLE "t_ds_user" ALTER COLUMN "tenant_id" SET DEFAULT -1;
ALTER TABLE "t_ds_version" ALTER COLUMN "version" TYPE VARCHAR(63);
ALTER TABLE "t_ds_worker_group" ALTER COLUMN "description" TYPE text, ALTER COLUMN "description" DROP DEFAULT;
ALTER TABLE "t_ds_error_command" ALTER COLUMN "id" DROP DEFAULT;
ALTER TABLE "t_ds_task_instance" ALTER COLUMN "log_path" DROP DEFAULT;

-- create index
DROP INDEX IF EXISTS idx_code;
DROP INDEX IF EXISTS idx_process_code_version;
DROP INDEX IF EXISTS idx_code_version;
-- re index table t_ds_task_definition with index name task_definition_index
DROP INDEX IF EXISTS task_definition_index;
CREATE INDEX IF NOT EXISTS idx_cache_key ON t_ds_task_instance USING Btree("cache_key");
CREATE INDEX IF NOT EXISTS idx_parent_workflow_instance_id ON t_ds_relation_sub_workflow (parent_workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_parent_task_code ON t_ds_relation_sub_workflow (parent_task_code);
CREATE INDEX IF NOT EXISTS idx_sub_workflow_instance_id ON t_ds_relation_sub_workflow (sub_workflow_instance_id);
CREATE INDEX IF NOT EXISTS task_definition_index ON t_ds_task_definition (project_code, id);
CREATE UNIQUE INDEX IF NOT EXISTS "unique_project_parameter_code" ON "t_ds_project_parameter" ("code");
CREATE UNIQUE INDEX IF NOT EXISTS "unique_project_parameter_name" ON "t_ds_project_parameter" ("project_code", "param_name");
CREATE UNIQUE INDEX IF NOT EXISTS "unique_project_preference_code" ON "t_ds_project_preference" ("code");
CREATE UNIQUE INDEX IF NOT EXISTS "unique_project_preference_project_code" ON "t_ds_project_preference" ("project_code");

-- SEQUENCE
DROP SEQUENCE IF EXISTS t_ds_project_preference_id_sequence;
CREATE SEQUENCE t_ds_project_preference_id_sequence;
ALTER TABLE t_ds_project_preference ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_project_preference_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_project_parameter_id_sequence;
CREATE SEQUENCE  t_ds_project_parameter_id_sequence;
ALTER TABLE t_ds_project_parameter ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_project_parameter_id_sequence');
DROP SEQUENCE IF EXISTS "t_ds_error_command_id_seq";

-- add comment
comment on column t_ds_process_instance.project_code is 'project code';
comment on column t_ds_process_instance.executor_name is 'execute user name';
comment on column t_ds_process_instance.tenant_code is 'tenant code';
comment on column t_ds_task_instance.process_instance_name is 'process instance name';
comment on column t_ds_process_instance.project_code is 'project code';
comment on column t_ds_task_instance.executor_name is 'execute user name';
comment on column t_ds_command.tenant_code is 'tenant code';
comment on column t_ds_error_command.tenant_code is 'tenant code';
comment on column t_ds_schedules.tenant_code is 'tenant code';
COMMENT ON COLUMN "t_ds_command" ."tenant_code" IS '';
COMMENT ON COLUMN "t_ds_error_command" ."tenant_code" IS '';
COMMENT ON COLUMN "t_ds_process_instance" ."project_code" IS '';
COMMENT ON COLUMN "t_ds_process_instance" ."executor_name" IS '';
COMMENT ON COLUMN "t_ds_process_instance" ."tenant_code" IS '';
COMMENT ON COLUMN "t_ds_schedules" ."tenant_code" IS '';
COMMENT ON COLUMN "t_ds_task_instance" ."process_instance_name" IS '';
COMMENT ON COLUMN "t_ds_task_instance" ."executor_name" IS '';
COMMENT ON COLUMN "t_ds_alert" ."sign" IS 'sign=sha1(content)';
