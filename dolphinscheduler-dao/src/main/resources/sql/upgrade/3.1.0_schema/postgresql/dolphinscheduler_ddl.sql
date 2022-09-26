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

-- t_ds_k8s_namespace
ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS online_job_num;
ALTER TABLE t_ds_k8s_namespace DROP COLUMN IF EXISTS k8s;
ALTER TABLE t_ds_k8s_namespace DROP CONSTRAINT IF EXISTS k8s_namespace_unique;
ALTER TABLE t_ds_k8s_namespace ADD COLUMN IF NOT EXISTS code bigint NOT NULL DEFAULT '0';
ALTER TABLE t_ds_k8s_namespace ADD COLUMN IF NOT EXISTS cluster_code bigint NOT NULL DEFAULT '0';
ALTER TABLE t_ds_k8s_namespace DROP CONSTRAINT IF EXISTS k8s_namespace_unique;
ALTER TABLE t_ds_k8s_namespace ADD CONSTRAINT k8s_namespace_unique UNIQUE (namespace, cluster_code);

-- t_ds_task_definition
ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT '-1';
ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT '-1';

-- t_ds_task_definition_log
ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT '-1';
ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT '-1';

-- t_ds_task_definition_log
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT '-1';
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT '-1';

-- t_ds_relation_process_instance
DROP INDEX IF EXISTS "idx_relation_process_instance_parent_process_task";
CREATE INDEX IF NOT EXISTS idx_relation_process_instance_parent_process_task ON t_ds_relation_process_instance USING Btree("parent_process_instance_id","parent_task_instance_id");
DROP INDEX IF EXISTS "idx_relation_process_instance_process_instance_id";
CREATE INDEX IF NOT EXISTS idx_relation_process_instance_process_instance_id ON t_ds_relation_process_instance USING Btree("process_instance_id");

-- t_ds_cluster
CREATE TABLE IF NOT EXISTS "t_ds_cluster" (
    id serial NOT NULL,
    code bigint NOT NULL,
    name varchar(100) DEFAULT NULL,
    config text DEFAULT NULL,
    description text,
    operator int DEFAULT NULL,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT cluster_name_unique UNIQUE (name),
    CONSTRAINT cluster_code_unique UNIQUE (code)
);

--- set process_instance_priority and task_priority default value as 2
alter table t_ds_process_instance alter column process_instance_priority set default 2;
alter table t_ds_schedules alter column process_instance_priority set default 2;
alter table t_ds_command alter column process_instance_priority set default 2;
alter table t_ds_error_command alter column process_instance_priority set default 2;
alter table t_ds_task_definition_log alter column task_priority set default 2;
alter table t_ds_task_definition alter column task_priority set default 2;

--- add column
ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS task_execute_type int DEFAULT '0';
ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS task_execute_type int DEFAULT '0';
ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS task_execute_type int DEFAULT '0';
ALTER TABLE t_ds_task_instance DROP CONSTRAINT IF EXISTS foreign_key_instance_id;
ALTER TABLE t_ds_project alter COLUMN description type varchar(255);
ALTER TABLE t_ds_task_group alter COLUMN description type varchar(255);
ALTER TABLE t_ds_worker_group ADD COLUMN IF NOT EXISTS other_params_json text DEFAULT NULL;
ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS state_history text DEFAULT NULL;
