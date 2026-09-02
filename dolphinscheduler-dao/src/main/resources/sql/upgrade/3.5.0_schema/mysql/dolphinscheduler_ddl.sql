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

ALTER TABLE `t_ds_task_instance` ADD INDEX idx_project_submit_time (project_code ASC, submit_time DESC);
ALTER TABLE `t_ds_workflow_instance` ADD INDEX idx_project_start_time (project_code ASC, start_time DESC);
ALTER TABLE `t_ds_schedules`
    ADD COLUMN `missed_fire_policy` tinyint NOT NULL DEFAULT '2' COMMENT 'missed fire policy: 0 skip missed, 1 fire once now, 2 fire all missed' AFTER `crontab`;

-- Enforce idempotent task-result alerts at the database level.
-- Allows INSERT IGNORE (MySQL) / ON CONFLICT DO NOTHING (PostgreSQL) to atomically
-- prevent duplicates without check-then-insert race conditions.
-- Clean up any existing duplicate rows before adding the unique constraint.
DELETE t1 FROM t_ds_alert t1
INNER JOIN t_ds_alert t2
WHERE t1.id < t2.id
  AND t1.sign = t2.sign
  AND t1.workflow_instance_id = t2.workflow_instance_id
  AND t1.alert_type = t2.alert_type;
ALTER TABLE `t_ds_alert` ADD UNIQUE INDEX `uk_alert_dedup` (`sign`, `workflow_instance_id`, `alert_type`);

