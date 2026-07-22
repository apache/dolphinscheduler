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
