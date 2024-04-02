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
DROP TABLE IF EXISTS `t_ds_relation_project_worker_group`;
CREATE TABLE `t_ds_relation_project_worker_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `project_code` bigint(20) NOT NULL COMMENT 'project code',
  `worker_group` varchar(255) DEFAULT NULL COMMENT 'worker group',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY unique_project_worker_group(project_code,worker_group)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE = utf8_bin;

ALTER TABLE t_ds_project_parameter ADD  `operator` int(11) DEFAULT NULL COMMENT 'operator user id';