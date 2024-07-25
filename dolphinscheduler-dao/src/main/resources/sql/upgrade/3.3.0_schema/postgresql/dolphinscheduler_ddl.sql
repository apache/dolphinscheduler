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

DROP TABLE IF EXISTS t_ds_process_task_lineage;
CREATE TABLE t_ds_process_task_lineage (
    id int NOT NULL,
    process_definition_code bigint NOT NULL DEFAULT 0,
    process_definition_version int NOT NULL DEFAULT 0,
    task_definition_code bigint NOT NULL DEFAULT 0,
    task_definition_version int NOT NULL DEFAULT 0,
    dept_project_code bigint NOT NULL DEFAULT 0,
    dept_process_definition_code bigint NOT NULL DEFAULT 0,
    dept_task_definition_code bigint NOT NULL DEFAULT 0,
    create_time timestamp NOT NULL DEFAULT current_timestamp,
    update_time timestamp NOT NULL DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

create index idx_process_code_version on t_ds_process_task_lineage (process_definition_code,process_definition_version);
create index idx_task_code_version on t_ds_process_task_lineage (task_definition_code,task_definition_version);
create index idx_dept_code on t_ds_process_task_lineage (dept_project_code,dept_process_definition_code,dept_task_definition_code);

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
