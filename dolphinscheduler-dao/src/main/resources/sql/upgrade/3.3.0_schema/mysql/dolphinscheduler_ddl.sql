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
-- Modify "t_ds_alertgroup" table
ALTER TABLE `t_ds_alertgroup` AUTO_INCREMENT 3;
-- Modify "t_ds_alert_plugin_instance" table
ALTER TABLE `t_ds_alert_plugin_instance`
    ADD COLUMN `instance_type` int NOT NULL DEFAULT 0, ADD COLUMN `warning_type` int NOT NULL DEFAULT 3;
-- Create "t_ds_listener_event" table
CREATE TABLE `t_ds_listener_event`
(
    `id`          int      NOT NULL AUTO_INCREMENT COMMENT "key",
    `content`     text NULL COMMENT "listener event json content",
    `sign`        char(64) NOT NULL DEFAULT "" COMMENT "sign=sha1(content)",
    `post_status` tinyint NOT NULL DEFAULT 0 COMMENT "0:wait running,1:success,2:failed,3:partial success",
    `event_type`  int NOT NULL COMMENT "listener event type",
    `log`         text NULL COMMENT "log",
    `create_time` datetime NULL COMMENT "create time",
    `update_time` datetime NULL COMMENT "update time",
    PRIMARY KEY (`id`),
    INDEX         `idx_sign` (`sign`),
    INDEX         `idx_status` (`post_status`)
) CHARSET utf8 COLLATE utf8_bin;

-- modify_data_t_ds_dq_rule_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_data_t_ds_dq_rule_input_entry;
DROP PROCEDURE if EXISTS modify_data_t_ds_dq_rule_input_entry;
delimiter d//
CREATE PROCEDURE modify_data_t_ds_dq_rule_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_dq_rule_input_entry'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='value')
   THEN
       ALTER TABLE `t_ds_dq_rule_input_entry`
       CHANGE COLUMN `value` `data` varchar(255) DEFAULT NULL;
   END IF;
END;
d//
delimiter ;
CALL modify_data_t_ds_dq_rule_input_entry;
DROP PROCEDURE modify_data_t_ds_dq_rule_input_entry;

-- modify_data_value_t_ds_dq_rule_input_entry behavior change
--DROP PROCEDURE if EXISTS modify_data_value_t_ds_dq_rule_input_entry;
DROP PROCEDURE if EXISTS modify_data_value_t_ds_dq_rule_input_entry;
delimiter d//
CREATE PROCEDURE modify_data_value_t_ds_dq_rule_input_entry()
BEGIN
   IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_dq_rule_input_entry'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='value_type')
   THEN
       ALTER TABLE `t_ds_dq_rule_input_entry`
       CHANGE COLUMN `value_type` `data_type` int(11) DEFAULT NULL;
   END IF;
END;
d//
delimiter ;
CALL modify_data_value_t_ds_dq_rule_input_entry;
DROP PROCEDURE modify_data_value_t_ds_dq_rule_input_entry;