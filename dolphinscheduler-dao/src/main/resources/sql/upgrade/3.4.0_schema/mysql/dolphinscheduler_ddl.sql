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

ALTER TABLE `t_ds_task_group_queue` ADD KEY `idx_task_id` (`task_id`);
ALTER TABLE `t_ds_task_group_queue` ADD KEY `idx_group_id` (`group_id`);
ALTER TABLE `t_ds_task_group_queue` ADD KEY `idx_status` (`status`);
ALTER TABLE `t_ds_task_group_queue` ADD KEY `idx_workflow_instance_id` (`workflow_instance_id`);

-- ----------------------------
-- Table structure for t_ds_serial_command
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_ds_serial_command` (
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
   `workflow_definition_code` int(11) NOT NULL COMMENT 'workflow definition code',
   `workflow_definition_version` int(11) NOT NULL COMMENT 'workflow definition version',
   `workflow_instance_id` bigint(20) NOT NULL COMMENT 'workflow instance id',
   `state` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'state of the serial queue: 0 waiting, 1 fired',
   `command` text COMMENT 'command json',
   `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
   `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
   PRIMARY KEY (`id`),
   KEY `idx_workflow_instance_id` (`workflow_instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

