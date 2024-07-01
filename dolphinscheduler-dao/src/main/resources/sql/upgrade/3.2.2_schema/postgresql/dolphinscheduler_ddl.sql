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

-- modify_data_t_ds_audit_log_input_entry
delimiter d//
CREATE OR REPLACE FUNCTION modify_data_t_ds_audit_log_input_entry() RETURNS void AS $$
BEGIN
      IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_audit_log'
                  AND column_name = 'resource_type')
      THEN
            ALTER TABLE t_ds_audit_log
            drop resource_type, drop operation, drop resource_id,
                add model_id        bigint NOT NULL,
                add model_name      VARCHAR(255) NOT NULL,
                add model_type      VARCHAR(255) NOT NULL,
                add operation_type  VARCHAR(255) NOT NULL,
                add description     VARCHAR(255) NOT NULL,
                add latency         int NOT NULL,
                add detail          VARCHAR(255) DEFAULT NULL;
            ALTER TABLE t_ds_audit_log RENAME COLUMN "time" TO "create_time";
END IF;
END;
$$ LANGUAGE plpgsql;
d//

select modify_data_t_ds_audit_log_input_entry();
DROP FUNCTION IF EXISTS modify_data_t_ds_audit_log_input_entry();

ALTER TABLE t_ds_project_parameter ADD COLUMN IF NOT EXISTS param_data_type varchar(50) DEFAULT 'VARCHAR';