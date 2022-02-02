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

-- uc_dolphin_T_t_ds_datasource_A_note
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_datasource_A_note() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_datasource'
        AND COLUMN_NAME ='note')
    THEN
        ALTER TABLE t_ds_datasource ALTER COLUMN note type varchar(255);
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_datasource_A_note();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_datasource_A_note();

-- uc_dolphin_T_t_ds_resources_A_description
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_resources_A_description() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_resources'
        AND COLUMN_NAME ='description')
    THEN
        ALTER TABLE t_ds_resources ALTER COLUMN description type varchar(255);
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_resources_A_description();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_resources_A_description();

-- uc_dolphin_T_t_ds_schedules_A_crontab
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_schedules_A_crontab() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_schedules'
        AND COLUMN_NAME ='crontab')
    THEN
        ALTER TABLE t_ds_schedules ALTER COLUMN crontab type varchar(255);
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_schedules_A_crontab();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_schedules_A_crontab();

-- uc_dolphin_T_t_ds_tenant_A_description
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_tenant_A_description() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_tenant'
        AND COLUMN_NAME ='description')
    THEN
        ALTER TABLE t_ds_tenant ALTER COLUMN description type varchar(255);
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_tenant_A_description();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_tenant_A_description();

-- uc_dolphin_T_t_ds_worker_group_A_name
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_worker_group_A_name() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_worker_group'
        AND COLUMN_NAME ='name')
    THEN
        ALTER TABLE t_ds_worker_group ALTER COLUMN name type varchar(255);
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_worker_group_A_name();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_worker_group_A_name();
