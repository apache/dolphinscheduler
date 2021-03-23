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


-- uc_dolphin_T_t_ds_worker_group_A_ip_list
delimiter d//
CREATE OR REPLACE FUNCTION uc_dolphin_T_t_ds_worker_group_A_ip_list() RETURNS void AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_NAME='t_ds_worker_group'
        AND COLUMN_NAME ='ip_list')
    THEN
        ALTER TABLE t_ds_worker_group rename ip_list TO addr_list;
        ALTER TABLE t_ds_worker_group ALTER column addr_list type text;
    END IF;
END;
$$ LANGUAGE plpgsql;
d//

delimiter ;
SELECT uc_dolphin_T_t_ds_worker_group_A_ip_list();
DROP FUNCTION IF EXISTS uc_dolphin_T_t_ds_worker_group_A_ip_list();

-- Add foreign key constraints for t_ds_task_instance --
ALTER TABLE t_ds_task_instance ADD CONSTRAINT foreign_key_instance_id  FOREIGN KEY(process_instance_id) REFERENCES t_ds_process_instance(id) ON DELETE CASCADE;

