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

-- ac_dolphin_T_t_ds_resources_A_pid
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_pid() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='pid')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN pid int DEFAULT -1;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_pid();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_pid();

-- ac_dolphin_T_t_ds_resources_A_full_name
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_resources_A_full_name();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_full_name() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='full_name')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN full_name varchar(255) DEFAULT null;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_full_name();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_full_name();

-- ac_dolphin_T_t_ds_resources_A_is_directory
delimiter ;
DROP FUNCTION IF EXISTS ac_dolphin_T_t_ds_resources_A_is_directory();
delimiter d//
CREATE FUNCTION ac_dolphin_T_t_ds_resources_A_is_directory() RETURNS void AS $$
BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_CATALOG=current_database()
          AND TABLE_SCHEMA=current_schema()
          AND TABLE_NAME='t_ds_resources'
          AND COLUMN_NAME ='is_directory')
      THEN
         ALTER TABLE t_ds_resources ADD COLUMN is_directory boolean DEFAULT false;
       END IF;
END;
$$ LANGUAGE plpgsql;
d//
delimiter ;
select ac_dolphin_T_t_ds_resources_A_is_directory();
DROP FUNCTION ac_dolphin_T_t_ds_resources_A_is_directory();

