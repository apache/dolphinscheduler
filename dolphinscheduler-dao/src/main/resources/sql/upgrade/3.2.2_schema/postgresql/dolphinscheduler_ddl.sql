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
DROP TABLE IF EXISTS t_ds_relation_project_worker_group;
CREATE TABLE t_ds_relation_project_worker_group (
    id int NOT NULL  ,
    project_code bigint DEFAULT NULL ,
    worker_group varchar(255) NOT NULL,
    create_time timestamp DEFAULT NULL,
    update_time timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT t_ds_relation_project_worker_group_un UNIQUE (project_code, worker_group)
);

DROP SEQUENCE IF EXISTS t_ds_relation_project_worker_group_sequence;
CREATE SEQUENCE  t_ds_relation_project_worker_group_sequence;
ALTER TABLE t_ds_relation_project_worker_group ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_project_worker_group_sequence');

ALTER TABLE t_ds_project_parameter ADD COLUMN IF NOT EXISTS operator int;