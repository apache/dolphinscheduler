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

-- uc_dolphin_T_t_ds_user_A_state
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_user_A_state();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_user_A_state() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_user'
          AND COLUMN_NAME ='state')
      THEN
         ALTER TABLE t_ds_user ADD COLUMN state int DEFAULT 1;
         comment on column t_ds_user.state is 'state 0:disable 1:enable';
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_user_A_state();
DROP FUNCTION uc_dolphin_T_t_ds_user_A_state();

-- uc_dolphin_T_t_ds_tenant_A_tenant_name
delimiter ;
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_tenant_A_tenant_name();
delimiter d//
CREATE FUNCTION uc_dolphin_T_t_ds_tenant_A_tenant_name() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_tenant'
          AND COLUMN_NAME ='tenant_name')
      THEN
         ALTER TABLE t_ds_tenant DROP COLUMN "tenant_name";
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select uc_dolphin_T_t_ds_tenant_A_tenant_name();
DROP FUNCTION uc_dolphin_T_t_ds_tenant_A_tenant_name();

-- uc_dolphin_T_t_ds_task_instance_A_first_submit_time
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_first_submit_time() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='first_submit_time')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN first_submit_time timestamp DEFAULT NULL;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_first_submit_time();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_first_submit_time();

-- uc_dolphin_T_t_ds_task_instance_A_delay_time
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_delay_time() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='delay_time')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN delay_time int DEFAULT '0';
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_delay_time();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_delay_time();

-- uc_dolphin_T_t_ds_task_instance_A_var_pool
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_task_instance_A_var_pool() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_task_instance'
                            AND COLUMN_NAME ='var_pool')
      THEN
         ALTER TABLE t_ds_task_instance ADD COLUMN var_pool text;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_task_instance_A_var_pool();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_task_instance_A_var_pool();

-- uc_dolphin_T_t_ds_process_instance_A_var_pool
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_process_instance_A_var_pool() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_process_instance'
                            AND COLUMN_NAME ='var_pool')
      THEN
         ALTER TABLE t_ds_process_instance ADD COLUMN var_pool text;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_process_instance_A_var_pool();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_process_instance_A_var_pool();

-- uc_dolphin_T_t_ds_process_definition_A_modify_by
delimiter d//
CREATE OR REPLACE FUNCTION ct_dolphin_T_t_ds_process_definition_version() RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS t_ds_process_definition_version (
        id int NOT NULL  ,
        process_definition_id int NOT NULL  ,
        version int DEFAULT NULL ,
        process_definition_json text ,
        description text ,
        global_params text ,
        locations text ,
        connects text ,
        receivers text ,
        receivers_cc text ,
        create_time timestamp DEFAULT NULL ,
        timeout int DEFAULT '0' ,
        resource_ids varchar(64),
        PRIMARY KEY (id)
    ) ;
    create index process_definition_id_and_version on t_ds_process_definition_version (process_definition_id,version);

    DROP SEQUENCE IF EXISTS t_ds_process_definition_version_id_sequence;
    CREATE SEQUENCE  t_ds_process_definition_version_id_sequence;
    ALTER TABLE t_ds_process_definition_version ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_version_id_sequence');
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT ct_dolphin_T_t_ds_process_definition_version();
DROP FUNCTION IF EXISTS ct_dolphin_T_t_ds_process_definition_version();

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
         ALTER TABLE t_ds_process_definition ADD COLUMN warning_group_id int4 DEFAULT NULL;
         COMMENT ON COLUMN t_ds_process_definition.warning_group_id IS 'alert group id';
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
         ALTER TABLE t_ds_process_definition_version ADD COLUMN warning_group_id int4 DEFAULT NULL;
         COMMENT ON COLUMN t_ds_process_definition_version.warning_group_id IS 'alert group id';
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
         ALTER TABLE t_ds_alertgroup ADD COLUMN alert_instance_ids varchar (255) DEFAULT NULL;
         COMMENT ON COLUMN t_ds_alertgroup.alert_instance_ids IS 'alert instance ids';
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
         ALTER TABLE t_ds_alertgroup ADD COLUMN create_user_id int4 DEFAULT NULL;
         COMMENT ON COLUMN t_ds_alertgroup.create_user_id IS 'create user id';
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_alertgroup_A_create_user_id();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_alertgroup_A_create_user_id();

-- uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_stat_all_indexes
          WHERE relname='t_ds_alertgroup'
                            AND indexrelname ='t_ds_alertgroup_name_UN')
      THEN
         ALTER TABLE t_ds_process_definition ADD CONSTRAINT t_ds_alertgroup_name_UN UNIQUE (group_name);
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_alertgroup_A_add_UN_groupName();

-- uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_stat_all_indexes
          WHERE relname='t_ds_datasource'
                            AND indexrelname ='t_ds_datasource_name_UN')
      THEN
         ALTER TABLE t_ds_process_definition ADD CONSTRAINT t_ds_datasource_name_UN UNIQUE (name, type);
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_datasource_A_add_UN_datasourceName();

-- uc_dolphin_T_t_ds_schedules_A_add_timezone
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_schedules_A_add_timezone() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_NAME='t_ds_schedules'
                            AND COLUMN_NAME ='timezone_id')
      THEN
ALTER TABLE t_ds_schedules ADD COLUMN timezone_id varchar(40) DEFAULT NULL;
END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_schedules_A_add_timezone();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_schedules_A_add_timezone();
-- ----------------------------
-- These columns will not be used in the new version,if you determine that the historical data is useless, you can delete it using the sql below
-- ----------------------------

-- ALTER TABLE t_ds_alert DROP COLUMN "show_type", DROP COLUMN "alert_type", DROP COLUMN "receivers", DROP COLUMN "receivers_cc";

-- ALTER TABLE t_ds_alertgroup DROP COLUMN "group_type";

-- ALTER TABLE t_ds_process_definition DROP COLUMN "receivers", DROP COLUMN "receivers_cc";

-- ALTER TABLE t_ds_process_definition_version DROP COLUMN "receivers", DROP COLUMN "receivers_cc";

-- DROP TABLE IF EXISTS t_ds_relation_user_alertgroup;

-- ALTER TABLE t_ds_command DROP COLUMN "dependence";

-- ALTER TABLE t_ds_error_command DROP COLUMN "dependence";