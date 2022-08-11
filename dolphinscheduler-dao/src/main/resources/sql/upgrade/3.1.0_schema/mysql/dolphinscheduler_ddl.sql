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


ALTER TABLE `t_ds_task_definition` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
ALTER TABLE `t_ds_task_definition_log` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
ALTER TABLE `t_ds_task_instance` ADD COLUMN `task_execute_type` int(11) DEFAULT '0' COMMENT 'task execute type: 0-batch, 1-stream' AFTER `task_type`;
ALTER TABLE `t_ds_task_instance` DROP FOREIGN KEY foreign_key_instance_id;

SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
alter table `t_ds_project` modify `description` varchar(255);
alter table `t_ds_task_group` modify `description` varchar(255);

