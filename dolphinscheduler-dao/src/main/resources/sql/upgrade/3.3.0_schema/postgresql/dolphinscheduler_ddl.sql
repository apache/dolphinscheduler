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
