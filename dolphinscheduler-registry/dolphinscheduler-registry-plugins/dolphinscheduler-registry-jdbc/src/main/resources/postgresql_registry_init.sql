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
    id               serial
        constraint t_ds_jdbc_registry_data_pk primary key,
    data_key         varchar                             not null,
    data_value       text                                not null,
    data_type        int4                                not null,
    last_term        bigint                              not null,
    last_update_time timestamp default current_timestamp not null,
    create_time      timestamp default current_timestamp not null
);

create unique index t_ds_jdbc_registry_data_key_uindex on t_ds_jdbc_registry_data (data_key);


DROP TABLE IF EXISTS t_ds_jdbc_registry_lock;
create table t_ds_jdbc_registry_lock
(
    id               serial
        constraint t_ds_jdbc_registry_lock_pk primary key,
    lock_key         varchar                             not null,
    lock_owner       varchar                             not null,
    last_term        bigint                              not null,
    last_update_time timestamp default current_timestamp not null,
    create_time      timestamp default current_timestamp not null
);
create unique index t_ds_jdbc_registry_lock_key_uindex on t_ds_jdbc_registry_lock (lock_key);
