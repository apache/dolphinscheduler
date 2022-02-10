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

    --- rename columns
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_command RENAME COLUMN process_definition_id to process_definition_code';
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_error_command RENAME COLUMN process_definition_id to process_definition_code';
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_process_instance RENAME COLUMN process_definition_id to process_definition_code';
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_task_instance RENAME COLUMN process_definition_id to task_code';
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_schedules RENAME COLUMN process_definition_id to process_definition_code';
    EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_process_definition RENAME COLUMN project_id to project_code';

    --- alter column type
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_command ALTER COLUMN process_definition_code TYPE  bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_error_command ALTER COLUMN process_definition_code TYPE bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ALTER COLUMN process_definition_code TYPE bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ALTER COLUMN task_code TYPE bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_schedules ALTER COLUMN process_definition_code TYPE bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_definition ALTER COLUMN project_code TYPE bigint';

    --- add columns
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_user ADD COLUMN IF NOT EXISTS "state" int DEFAULT 1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_alertgroup ADD COLUMN IF NOT EXISTS "alert_instance_ids" varchar(255) DEFAULT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_alertgroup ADD COLUMN IF NOT EXISTS "create_user_id" int4 DEFAULT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_project ADD COLUMN IF NOT EXISTS "code" bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_command ADD COLUMN IF NOT EXISTS "environment_code" bigint DEFAULT -1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_command ADD COLUMN IF NOT EXISTS "dry_run" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_command ADD COLUMN IF NOT EXISTS "process_definition_version" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_command ADD COLUMN IF NOT EXISTS "process_instance_id" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_error_command ADD COLUMN IF NOT EXISTS "environment_code" bigint DEFAULT -1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_error_command ADD COLUMN IF NOT EXISTS "dry_run" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_error_command ADD COLUMN IF NOT EXISTS "process_definition_version" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_error_command ADD COLUMN IF NOT EXISTS "process_instance_id" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ADD COLUMN IF NOT EXISTS "process_definition_version" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ADD COLUMN IF NOT EXISTS "environment_code" bigint DEFAULT -1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ADD COLUMN IF NOT EXISTS "var_pool" text';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ADD COLUMN IF NOT EXISTS "dry_run" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance ADD COLUMN IF NOT EXISTS "next_process_instance_id" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "task_definition_version" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "task_params" text';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "environment_code" bigint DEFAULT -1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "environment_config" text';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "first_submit_time" timestamp DEFAULT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "delay_time" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "var_pool" text';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "dry_run" int DEFAULT 0';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance ADD COLUMN IF NOT EXISTS "task_group_id" int DEFAULT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_schedules ADD COLUMN IF NOT EXISTS "timezone_id" varchar(40) DEFAULT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_schedules ADD COLUMN IF NOT EXISTS "environment_code" int DEFAULT -1';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_definition ADD COLUMN IF NOT EXISTS "code" bigint';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_definition ADD COLUMN IF NOT EXISTS "warning_group_id" int';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_definition ADD COLUMN IF NOT EXISTS "execution_type" int DEFAULT 0';

    --update default value for not null
    EXECUTE 'UPDATE ' || quote_ident(v_schema) ||'.t_ds_process_definition SET code = id';
    EXECUTE 'UPDATE ' || quote_ident(v_schema) ||'.t_ds_project SET code = id';

    ---drop columns
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_tenant DROP COLUMN IF EXISTS "tenant_name"';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance DROP COLUMN IF EXISTS "process_instance_json"';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance DROP COLUMN IF EXISTS "locations"';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance DROP COLUMN IF EXISTS "connects"';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_process_instance DROP COLUMN IF EXISTS "dependence_schedule_times"';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'.t_ds_task_instance DROP COLUMN IF EXISTS "task_json"';

    -- add CONSTRAINT
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_alertgroup" ADD CONSTRAINT "t_ds_alertgroup_name_un" UNIQUE ("group_name")';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_datasource" ADD CONSTRAINT "t_ds_datasource_name_un" UNIQUE ("name","type")';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_command" ALTER COLUMN "process_definition_code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_process_instance" ALTER COLUMN "process_definition_code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_task_instance" ALTER COLUMN "task_code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_schedules" ALTER COLUMN "process_definition_code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_process_definition" ALTER COLUMN "code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_process_definition" ALTER COLUMN "project_code" SET NOT NULL';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_process_definition" ADD CONSTRAINT "process_unique" UNIQUE ("name","project_code")';
    EXECUTE 'ALTER TABLE ' || quote_ident(v_schema) ||'."t_ds_project" ALTER COLUMN "code" SET NOT NULL';

    --- drop index
    EXECUTE 'DROP INDEX IF EXISTS "process_instance_index"';
    EXECUTE 'DROP INDEX IF EXISTS "task_instance_index"';

    --- create index
    EXECUTE 'CREATE INDEX IF NOT EXISTS priority_id_index ON ' || quote_ident(v_schema) ||'.t_ds_command USING Btree("process_instance_priority","id")';
    EXECUTE 'CREATE INDEX IF NOT EXISTS process_instance_index ON ' || quote_ident(v_schema) ||'.t_ds_process_instance USING Btree("process_definition_code","id")';

    ---add comment
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_user.state is ''state 0:disable 1:enable''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_alertgroup.alert_instance_ids is ''alert instance ids''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_alertgroup.create_user_id is ''create user id''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_project.code is ''coding''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_command.process_definition_code is ''process definition code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_command.environment_code is ''environment code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_command.dry_run is ''dry run flag：0 normal, 1 dry run''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_command.process_definition_version is ''process definition version''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_command.process_instance_id is ''process instance id''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_error_command.process_definition_code is ''process definition code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_error_command.environment_code is ''environment code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_error_command.dry_run is ''dry run flag：0 normal, 1 dry run''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_error_command.process_definition_version is ''process definition version''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_error_command.process_instance_id is ''process instance id''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_instance.process_definition_code is ''process instance code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_instance.process_definition_version is ''process instance version''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_instance.environment_code is ''environment code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_instance.var_pool is ''var pool''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_instance.dry_run is ''dry run flag：0 normal, 1 dry run''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.task_code is ''task definition code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.task_definition_version is ''task definition version''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.task_params is ''task params''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.environment_code is ''environment code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.environment_config is ''this config contains many environment variables config''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.first_submit_time is ''task first submit time''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.delay_time is ''task delay execution time''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.var_pool is ''var pool''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_task_instance.dry_run is ''dry run flag：0 normal, 1 dry run''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_schedules.process_definition_code is ''process definition code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_schedules.timezone_id is ''timezone id''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_schedules.environment_code is ''environment code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_definition.code is ''encoding''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_definition.project_code is ''project code''';
    EXECUTE 'comment on column ' ||  quote_ident(v_schema) ||'.t_ds_process_definition.warning_group_id is ''alert group id''';

    --create table
    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_plugin_define" (
        id serial NOT NULL,
        plugin_name varchar(100) NOT NULL,
        plugin_type varchar(100) NOT NULL,
        plugin_params text NULL,
        create_time timestamp NULL,
        update_time timestamp NULL,
        CONSTRAINT t_ds_plugin_define_pk PRIMARY KEY (id),
        CONSTRAINT t_ds_plugin_define_un UNIQUE (plugin_name, plugin_type)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_alert_plugin_instance" (
        id serial NOT NULL,
        plugin_define_id int4 NOT NULL,
        plugin_instance_params text NULL,
        create_time timestamp NULL,
        update_time timestamp NULL,
        instance_name varchar(200) NULL,
        CONSTRAINT t_ds_alert_plugin_instance_pk PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_environment" (
        id serial NOT NULL,
        code bigint NOT NULL,
        name varchar(100) DEFAULT NULL,
        config text DEFAULT NULL,
        description text,
        operator int DEFAULT NULL,
        create_time timestamp DEFAULT NULL,
        update_time timestamp DEFAULT NULL,
        PRIMARY KEY (id),
        CONSTRAINT environment_name_unique UNIQUE (name),
        CONSTRAINT environment_code_unique UNIQUE (code)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_environment_worker_group_relation" (
        id serial NOT NULL,
        environment_code bigint NOT NULL,
        worker_group varchar(255) NOT NULL,
        operator int DEFAULT NULL,
        create_time timestamp DEFAULT NULL,
        update_time timestamp DEFAULT NULL,
        PRIMARY KEY (id) ,
        CONSTRAINT environment_worker_group_unique UNIQUE (environment_code,worker_group)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_process_definition_log" (
        id serial NOT NULL  ,
        code bigint NOT NULL,
        name varchar(255) DEFAULT NULL ,
        version int NOT NULL ,
        description text ,
        project_code bigint DEFAULT NULL ,
        release_state int DEFAULT NULL ,
        user_id int DEFAULT NULL ,
        global_params text ,
        locations text ,
        warning_group_id int DEFAULT NULL ,
        flag int DEFAULT NULL ,
        timeout int DEFAULT 0 ,
        tenant_id int DEFAULT -1 ,
        execution_type int DEFAULT 0,
        operator int DEFAULT NULL ,
        operate_time timestamp DEFAULT NULL ,
        create_time timestamp DEFAULT NULL ,
        update_time timestamp DEFAULT NULL ,
        PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_task_definition" (
        id serial NOT NULL  ,
        code bigint NOT NULL,
        name varchar(255) DEFAULT NULL ,
        version int NOT NULL ,
        description text ,
        project_code bigint DEFAULT NULL ,
        user_id int DEFAULT NULL ,
        task_type varchar(50) DEFAULT NULL ,
        task_params text ,
        flag int DEFAULT NULL ,
        task_priority int DEFAULT NULL ,
        worker_group varchar(255) DEFAULT NULL ,
        environment_code bigint DEFAULT -1,
        fail_retry_times int DEFAULT NULL ,
        fail_retry_interval int DEFAULT NULL ,
        timeout_flag int DEFAULT NULL ,
        timeout_notify_strategy int DEFAULT NULL ,
        timeout int DEFAULT 0 ,
        delay_time int DEFAULT 0 ,
        task_group_id int DEFAULT NULL,
        resource_ids text ,
        create_time timestamp DEFAULT NULL ,
        update_time timestamp DEFAULT NULL ,
        PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_task_definition_log" (
        id serial NOT NULL  ,
        code bigint NOT NULL,
        name varchar(255) DEFAULT NULL ,
        version int NOT NULL ,
        description text ,
        project_code bigint DEFAULT NULL ,
        user_id int DEFAULT NULL ,
        task_type varchar(50) DEFAULT NULL ,
        task_params text ,
        flag int DEFAULT NULL ,
        task_priority int DEFAULT NULL ,
        worker_group varchar(255) DEFAULT NULL ,
        environment_code bigint DEFAULT -1,
        fail_retry_times int DEFAULT NULL ,
        fail_retry_interval int DEFAULT NULL ,
        timeout_flag int DEFAULT NULL ,
        timeout_notify_strategy int DEFAULT NULL ,
        timeout int DEFAULT 0 ,
        delay_time int DEFAULT 0 ,
        task_group_id int DEFAULT NULL,
        resource_ids text ,
        operator int DEFAULT NULL ,
        operate_time timestamp DEFAULT NULL ,
        create_time timestamp DEFAULT NULL ,
        update_time timestamp DEFAULT NULL ,
        PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_process_task_relation" (
        id serial NOT NULL  ,
        name varchar(255) DEFAULT NULL ,
        project_code bigint DEFAULT NULL ,
        process_definition_code bigint DEFAULT NULL ,
        process_definition_version int DEFAULT NULL ,
        pre_task_code bigint DEFAULT NULL ,
        pre_task_version int DEFAULT 0 ,
        post_task_code bigint DEFAULT NULL ,
        post_task_version int DEFAULT 0 ,
        condition_type int DEFAULT NULL ,
        condition_params text ,
        create_time timestamp DEFAULT NULL ,
        update_time timestamp DEFAULT NULL ,
        PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_process_task_relation_log" (
        id serial NOT NULL  ,
        name varchar(255) DEFAULT NULL ,
        project_code bigint DEFAULT NULL ,
        process_definition_code bigint DEFAULT NULL ,
        process_definition_version int DEFAULT NULL ,
        pre_task_code bigint DEFAULT NULL ,
        pre_task_version int DEFAULT 0 ,
        post_task_code bigint DEFAULT NULL ,
        post_task_version int DEFAULT 0 ,
        condition_type int DEFAULT NULL ,
        condition_params text ,
        operator int DEFAULT NULL ,
        operate_time timestamp DEFAULT NULL ,
        create_time timestamp DEFAULT NULL ,
        update_time timestamp DEFAULT NULL ,
        PRIMARY KEY (id)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_worker_group" (
          id serial NOT NULL,
          name varchar(255) NOT NULL,
          addr_list text DEFAULT NULL,
          create_time timestamp DEFAULT NULL,
          update_time timestamp DEFAULT NULL,
          PRIMARY KEY (id),
          CONSTRAINT name_unique UNIQUE (name)
      )';

    EXECUTE 'CREATE TABLE IF NOT EXISTS '|| quote_ident(v_schema) ||'."t_ds_audit_log" (
          id serial NOT NULL,
          user_id int NOT NULL,
          resource_type int NOT NULL,
          operation int NOT NULL,
          time timestamp DEFAULT NULL ,
          resource_id int NOT NULL,
          PRIMARY KEY (id)
	)';

    return 'Success!';
    exception when others then
        ---Raise EXCEPTION '(%)',SQLERRM;
        return SQLERRM;
END;
$BODY$;

select dolphin_update_metadata();

d//
