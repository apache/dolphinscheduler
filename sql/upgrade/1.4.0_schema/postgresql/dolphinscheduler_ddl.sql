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

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS t_ds_plugin_define;
CREATE TABLE t_ds_plugin_define (
	id serial NOT NULL,
	plugin_name varchar(100) NOT NULL,
	plugin_type varchar(100) NOT NULL,
	plugin_params text NULL,
	create_time timestamp NULL,
	update_time timestamp NULL,
	CONSTRAINT t_ds_plugin_define_pk PRIMARY KEY (id),
	CONSTRAINT t_ds_plugin_define_un UNIQUE (plugin_name, plugin_type)
);

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert_plugin_instance;
CREATE TABLE t_ds_alert_plugin_instance (
                                            id                     serial NOT NULL,
                                            plugin_define_id       int4 NOT NULL,
                                            plugin_instance_params text NULL,
                                            create_time            timestamp NULL,
                                            update_time            timestamp NULL,
                                            instance_name          varchar(200) NULL,
                                            CONSTRAINT t_ds_alert_plugin_instance_pk PRIMARY KEY (id)
);

-- uc_dolphin_T_t_ds_process_definition_A_warning_group_id
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_process_definition_A_warning_group_id() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_process_definition'
                            AND COLUMN_NAME ='warning_group_id')
      THEN
         ALTER TABLE t_ds_process_definition ADD COLUMN `warning_group_id` int4 DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_process_definition_A_warning_group_id();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_definition_A_warning_group_id();

-- uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_process_definition_version'
                            AND COLUMN_NAME ='warning_group_id')
      THEN
         ALTER TABLE t_ds_process_definition_version ADD COLUMN `warning_group_id` int4 DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id();

-- uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_alertgroup'
                            AND COLUMN_NAME ='alert_instance_ids')
      THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `alert_instance_ids` varchar (255) DEFAULT NULL COMMENT 'alert instance ids' AFTER `id`;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids();

-- uc_dolphin_T_t_ds_alertgroup_A_create_user_id
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_alertgroup_A_create_user_id() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_alertgroup'
                            AND COLUMN_NAME ='create_user_id')
      THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `create_user_id` int4 DEFAULT NULL COMMENT 'create user id' AFTER `alert_instance_ids`;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_alertgroup_A_create_user_id();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_alertgroup_A_create_user_id();

-- ----------------------------
-- These columns will not be used in the new version,if you determine that the historical data is useless, you can delete it using the sql below
-- ----------------------------

-- ALTER TABLE t_ds_process_definition DROP COLUMN "receivers", DROP COLUMN "receivers_cc";

-- ALTER TABLE t_ds_process_definition_version DROP COLUMN "receivers", DROP COLUMN "receivers_cc";

-- ALTER TABLE  t_ds_alert DROP COLUMN "show_type",DROP COLUMN "alert_type",DROP COLUMN "receivers",DROP COLUMN "receivers_cc";

-- ALTER TABLE  t_ds_alertgroup DROP COLUMN "group_type";

-- DROP TABLE IF EXISTS t_ds_relation_user_alertgroup;
