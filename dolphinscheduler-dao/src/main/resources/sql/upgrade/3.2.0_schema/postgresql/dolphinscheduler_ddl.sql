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

ALTER TABLE t_ds_command ADD COLUMN IF NOT EXISTS test_flag int DEFAULT NULL;
COMMENT ON COLUMN t_ds_command.test_flag is 'test flag：0 normal, 1 test run';

ALTER TABLE t_ds_error_command ADD COLUMN IF NOT EXISTS test_flag int DEFAULT NULL;
COMMENT ON COLUMN t_ds_error_command.test_flag is 'test flag：0 normal, 1 test run';

ALTER TABLE t_ds_datasource ADD COLUMN IF NOT EXISTS test_flag int DEFAULT NULL;
COMMENT ON COLUMN t_ds_datasource.test_flag is 'test flag：0 normal, 1 test run';

ALTER TABLE t_ds_datasource ADD COLUMN IF NOT EXISTS bind_test_id int DEFAULT NULL;
COMMENT ON COLUMN t_ds_datasource.bind_test_id is 'bind testDataSource id';

ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS test_flag int DEFAULT NULL;
COMMENT ON COLUMN t_ds_process_instance.test_flag is 'test flag：0 normal, 1 test run';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS test_flag int DEFAULT NULL;
COMMENT ON COLUMN t_ds_task_instance.test_flag is 'test flag：0 normal, 1 test run';

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

ALTER TABLE t_ds_task_definition ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
COMMENT ON COLUMN t_ds_task_definition.is_cache is '0 not available, 1 available';

ALTER TABLE t_ds_task_definition_log ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
COMMENT ON COLUMN t_ds_task_definition_log.is_cache is '0 not available, 1 available';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS is_cache int DEFAULT '0';
COMMENT ON COLUMN t_ds_task_instance.is_cache is '0 not available, 1 available';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS cache_key varchar(200) DEFAULT NULL;
COMMENT ON COLUMN t_ds_task_instance.cache_key is 'cache key';

CREATE INDEX IF NOT EXISTS idx_cache_key ON t_ds_task_instance USING Btree("cache_key");

ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS project_code bigint DEFAULT NULL;
COMMENT ON COLUMN t_ds_process_instance.project_code is 'project code';

ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS executor_name varchar(64) DEFAULT NULL;
COMMENT ON COLUMN t_ds_process_instance.executor_name is 'execute user name';

ALTER TABLE t_ds_process_instance ADD COLUMN IF NOT EXISTS tenant_code varchar(64) DEFAULT NULL;
COMMENT ON COLUMN t_ds_process_instance.tenant_code is 'tenant code';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS process_instance_name varchar(255) DEFAULT NULL;
COMMENT ON COLUMN t_ds_task_instance.process_instance_name is 'process instance name';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS project_code bigint DEFAULT NULL;
COMMENT ON COLUMN t_ds_task_instance.project_code is 'project code';

ALTER TABLE t_ds_task_instance ADD COLUMN IF NOT EXISTS executor_name varchar(64) DEFAULT NULL;
COMMENT ON COLUMN t_ds_task_instance.executor_name is 'execute user name';

ALTER TABLE t_ds_fav_task RENAME COLUMN task_name to task_type;