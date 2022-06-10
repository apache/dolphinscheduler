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

delimiter d//
CREATE OR REPLACE FUNCTION public.dolphin_update_metadata(
    )
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
v_schema varchar;
BEGIN
    ---get schema name
    v_schema =current_schema();

    --- alter column
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_resources ALTER COLUMN full_name Type varchar(128)';

--- add column
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_alert ADD COLUMN IF NOT EXISTS sign varchar(40) NOT NULL DEFAULT ''''  ';
EXECUTE 'comment on column ' || quote_ident(v_schema) ||'.t_ds_alert.sign is ''sign=sha1(content)''';

-- Add resource limit column
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_definition ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT ''-1''  ';
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_definition ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT ''-1''  ';
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_definition_log ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT ''-1''  ';
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_definition_log ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT ''-1''  ';
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS cpu_quota int NOT NULL DEFAULT ''-1''  ';
EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS memory_max int NOT NULL DEFAULT ''-1''  ';


return 'Success!';
exception when others then
        ---Raise EXCEPTION '(%)',SQLERRM;
        return SQLERRM;
END;
$BODY$;

select dolphin_update_metadata();

d//

-- add unique key to t_ds_relation_project_user
CREATE UNIQUE INDEX t_ds_relation_project_user_un
    on t_ds_relation_project_user (user_id, project_id);

delimiter d//
CREATE OR REPLACE FUNCTION public.dolphin_update_metadata(
	)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
v_schema varchar;
BEGIN
    ---get schema name
    v_schema =current_schema();

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_idx_project_code_process_definition_code"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_idx_project_code_process_definition_code ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation USING Btree("project_code","process_definition_code")';

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_idx_pre_task_code_version"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_idx_pre_task_code_version ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation USING Btree("pre_task_code","pre_task_version")';

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_idx_post_task_code_version"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_idx_post_task_code_version ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation USING Btree("post_task_code","post_task_version")';

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_log_idx_project_code_process_definition_code"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_log_idx_project_code_process_definition_code ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation_log USING Btree("project_code","process_definition_code")';

EXECUTE 'DROP INDEX IF EXISTS "idx_task_definition_log_code_version"';
EXECUTE 'CREATE INDEX IF NOT EXISTS idx_task_definition_log_code_version ON ' || quote_ident(v_schema) ||'.t_ds_task_definition_log USING Btree("code","version")';

EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_user ADD COLUMN IF NOT EXISTS "time_zone" varchar(32) DEFAULT NULL';

EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_alert ADD COLUMN IF NOT EXISTS "warning_type" int DEFAULT 2';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_comparison_type" (
        id serial NOT NULL,
        "type" varchar NOT NULL,
        execute_sql varchar NULL,
        output_table varchar NULL,
        "name" varchar NULL,
        create_time timestamp NULL,
        update_time timestamp NULL,
        is_inner_source bool NULL,
        CONSTRAINT t_ds_dq_comparison_type_pk PRIMARY KEY (id)
    )';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_execute_result" (
    id serial NOT NULL,
    process_definition_id int4 NULL,
    process_instance_id int4 NULL,
    task_instance_id int4 NULL,
    rule_type int4 NULL,
    rule_name varchar(255) DEFAULT NULL,
    statistics_value float8 NULL,
    comparison_value float8 NULL,
    check_type int4 NULL,
    threshold float8 NULL,
    "operator" int4 NULL,
    failure_strategy int4 NULL,
    state int4 NULL,
    user_id int4 NULL,
    create_time timestamp NULL,
    update_time timestamp NULL,
    comparison_type int4 NULL,
    error_output_path text NULL,
    CONSTRAINT t_ds_dq_execute_result_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_rule" (
    id serial NOT NULL,
    "name" varchar(100) DEFAULT NULL,
    "type" int4 NULL,
    user_id int4 NULL,
    create_time timestamp NULL,
    update_time timestamp NULL,
    CONSTRAINT t_ds_dq_rule_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_rule_execute_sql" (
    id serial NOT NULL,
    "index" int4 NULL,
    "sql" text NULL,
    table_alias varchar(255) DEFAULT NULL,
    "type" int4 NULL,
    create_time timestamp NULL,
    update_time timestamp NULL,
    is_error_output_sql bool NULL,
    CONSTRAINT t_ds_dq_rule_execute_sql_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_rule_input_entry" (
    id serial NOT NULL,
    field varchar(255) DEFAULT NULL,
    "type" varchar(255) DEFAULT NULL,
    title varchar(255) DEFAULT NULL,
    value varchar(255)  DEFAULT NULL,
    "options" text DEFAULT NULL,
    placeholder varchar(255) DEFAULT NULL,
    option_source_type int4 NULL,
    value_type int4 NULL,
    input_type int4 NULL,
    is_show int2 NULL DEFAULT "1"::smallint,
    can_edit int2 NULL DEFAULT "1"::smallint,
    is_emit int2 NULL DEFAULT "0"::smallint,
    is_validate int2 NULL DEFAULT "0"::smallint,
    create_time timestamp NULL,
    update_time timestamp NULL,
    CONSTRAINT t_ds_dq_rule_input_entry_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_dq_task_statistics_value" (
    id serial NOT NULL,
    process_definition_id int4 NOT NULL,
    task_instance_id int4 NULL,
    rule_id int4 NOT NULL,
    unique_code varchar NOT NULL,
    statistics_name varchar NULL,
    statistics_value float8 NULL,
    data_time timestamp(0) NULL,
    create_time timestamp(0) NULL,
    update_time timestamp(0) NULL,
    CONSTRAINT t_ds_dq_task_statistics_value_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_relation_rule_execute_sql" (
    id serial NOT NULL,
    rule_id int4 NULL,
    execute_sql_id int4 NULL,
    create_time timestamp NULL,
    update_time timestamp NULL,
    CONSTRAINT t_ds_relation_rule_execute_sql_pk PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS' || quote_ident(v_schema) ||'."t_ds_relation_rule_input_entry" (
    id serial NOT NULL,
    rule_id int4 NULL,
    rule_input_entry_id int4 NULL,
    values_map text NULL,
    "index" int4 NULL,
    create_time timestamp NULL,
    update_time timestamp NULL,
    CONSTRAINT t_ds_relation_rule_input_entry_pk PRIMARY KEY (id)
)';

EXECUTE 'DROP INDEX IF EXISTS "idx_alert_status"';
EXECUTE 'CREATE INDEX IF NOT EXISTS idx_alert_status ON ' || quote_ident(v_schema) ||'.t_ds_alert USING Btree("alert_status")';

EXECUTE 'DROP INDEX IF EXISTS "idx_task_definition_log_project_code"';
EXECUTE 'CREATE INDEX IF NOT EXISTS idx_task_definition_log_project_code ON ' || quote_ident(v_schema) ||'.t_ds_task_definition_log USING Btree("project_code")';

EXECUTE 'DROP INDEX IF EXISTS "idx_task_instance_code_version"';
EXECUTE 'CREATE INDEX IF NOT EXISTS idx_task_instance_code_version ON' || quote_ident(v_schema) ||'.t_ds_task_instance USING Btree("task_code","task_definition_version")';

EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_k8s" (
   id serial NOT NULL,
   k8s_name    VARCHAR(100) DEFAULT NULL ,
   k8s_config  text ,
   create_time timestamp DEFAULT NULL ,
   update_time timestamp DEFAULT NULL ,
   PRIMARY KEY (id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_k8s_namespace" (
   id serial NOT NULL,
   limits_memory      int DEFAULT NULL ,
   namespace          varchar(100) DEFAULT NULL ,
   online_job_num     int DEFAULT NULL,
   user_id            int DEFAULT NULL,
   pod_replicas       int DEFAULT NULL,
   pod_request_cpu    NUMERIC(13,4) NULL,
   pod_request_memory int DEFAULT NULL,
   limits_cpu         NUMERIC(13,4) NULL,
   k8s                varchar(100) DEFAULT NULL,
   create_time        timestamp DEFAULT NULL ,
   update_time        timestamp DEFAULT NULL ,
   PRIMARY KEY (id) ,
   CONSTRAINT k8s_namespace_unique UNIQUE (namespace,k8s)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_relation_namespace_user" (
    id serial NOT NULL,
    user_id           int DEFAULT NULL ,
    namespace_id      int DEFAULT NULL ,
    perm              int DEFAULT NULL ,
    create_time       timestamp DEFAULT NULL ,
    update_time       timestamp DEFAULT NULL ,
    PRIMARY KEY (id) ,
    CONSTRAINT namespace_user_unique UNIQUE (user_id,namespace_id)
)';

EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_cluster" (
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
)';

return 'Success!';
exception when others then
		---Raise EXCEPTION '(%)',SQLERRM;
        return SQLERRM;
END;
$BODY$;

select dolphin_update_metadata();

d//
