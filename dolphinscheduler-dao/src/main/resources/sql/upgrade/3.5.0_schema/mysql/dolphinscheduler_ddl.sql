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

-- Replace the single-column-prefix index with a composite index that also covers
-- task_execute_type, so the task instance list can filter and sort without a filesort.
ALTER TABLE `t_ds_task_instance`
    DROP INDEX `idx_project_submit_time`,
    ADD INDEX `idx_ti_project_type_submit_time` (`project_code`, `task_execute_type`, `submit_time` DESC),
    ADD INDEX `idx_ti_project_type_start_time` (`project_code`, `task_execute_type`, `start_time` DESC);

-- Replace the single-column-prefix index with composite indexes that also cover
-- is_sub_workflow (and workflow_definition_code), matching the workflow instance list query.
ALTER TABLE `t_ds_workflow_instance`
    DROP INDEX `idx_project_start_time`,
    ADD INDEX `idx_wi_project_sub_start_end` (`project_code`, `is_sub_workflow`, `start_time` DESC, `end_time` DESC),
    ADD INDEX `idx_wi_project_sub_def_start_end` (`project_code`, `is_sub_workflow`, `workflow_definition_code`, `start_time` DESC, `end_time` DESC);
