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

ALTER TABLE `t_ds_task_instance` MODIFY COLUMN `task_params` longtext COMMENT 'job custom parameters' AFTER `app_link`;
ALTER TABLE `t_ds_process_task_relation` ADD KEY `idx_code` (`project_code`, `process_definition_code`) USING BTREE;
ALTER TABLE `t_ds_process_task_relation_log` ADD KEY `idx_process_code_version` (`process_definition_code`,`process_definition_version`) USING BTREE;

ALTER TABLE `t_ds_task_definition_log` ADD INDEX `idx_code_version` (`code`,`version`) USING BTREE;
alter table t_ds_task_definition_log add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
alter table t_ds_task_definition_log add `task_group_priority` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `task_group_id`;
alter table t_ds_task_definition add `task_group_id` int(11) DEFAULT NULL COMMENT 'task group id' AFTER `resource_ids`;
alter table t_ds_task_definition add `task_group_priority` int(11) DEFAULT '0' COMMENT 'task group id' AFTER `task_group_id`;