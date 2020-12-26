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

SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
DROP TABLE IF EXISTS `t_ds_plugin_define`;
CREATE TABLE `t_ds_plugin_define` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(100) NOT NULL COMMENT 'the name of plugin eg: email',
  `plugin_type` varchar(100) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
  `plugin_params` text COMMENT 'plugin params',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_ds_alert_plugin_instance`;
CREATE TABLE `t_ds_alert_plugin_instance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_define_id` int NOT NULL,
  `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `alert_group_id` int DEFAULT NULL,
  `instance_name` varchar(200) DEFAULT NULL COMMENT 'alert instance name',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE t_ds_process_definition ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;

ALTER TABLE t_ds_process_definition_version ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;



ALTER TABLE t_ds_process_definition DROP receivers, DROP receivers_cc;

ALTER TABLE t_ds_process_definition_version DROP receivers, DROP receivers_cc;

ALTER TABLE  t_ds_alert DROP show_type,DROP alert_type,DROP receivers,DROP receivers_cc;



