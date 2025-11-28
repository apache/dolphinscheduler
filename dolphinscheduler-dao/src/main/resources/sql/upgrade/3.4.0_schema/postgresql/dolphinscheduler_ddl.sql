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

create index idx_t_ds_task_group_queue_task_id on t_ds_task_group_queue(task_id);
create index idx_t_ds_task_group_queue_group_id on t_ds_task_group_queue(group_id);
create index idx_t_ds_task_group_queue_status on t_ds_task_group_queue(status);
create index idx_t_ds_task_group_queue_workflow_instance_id on t_ds_task_group_queue(workflow_instance_id);    
    
-- ----------------------------
-- Table structure for t_ds_serial_command
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_ds_serial_command (
     id SERIAL PRIMARY KEY,
     workflow_definition_code BIGINT NOT NULL,
     workflow_definition_version INT NOT NULL,
     workflow_instance_id BIGINT NOT NULL,
     state SMALLINT NOT NULL DEFAULT 0,
     command TEXT,
     create_time TIMESTAMP NOT NULL DEFAULT now(),
     update_time TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX "idx_workflow_instance_id" ON "t_ds_serial_command" ("workflow_instance_id");
COMMENT ON TABLE "t_ds_serial_command" IS 'serial command queue table';
COMMENT ON COLUMN "t_ds_serial_command"."id" IS 'primary key';
COMMENT ON COLUMN "t_ds_serial_command"."workflow_definition_code" IS 'workflow definition code';
COMMENT ON COLUMN "t_ds_serial_command"."workflow_definition_version" IS 'workflow definition version';
COMMENT ON COLUMN "t_ds_serial_command"."workflow_instance_id" IS 'workflow instance id';
COMMENT ON COLUMN "t_ds_serial_command"."state" IS 'state of the serial queue: 0 waiting, 1 fired';
COMMENT ON COLUMN "t_ds_serial_command"."command" IS 'command json';
COMMENT ON COLUMN "t_ds_serial_command"."create_time" IS 'create time';
COMMENT ON COLUMN "t_ds_serial_command"."update_time" IS 'update time';
