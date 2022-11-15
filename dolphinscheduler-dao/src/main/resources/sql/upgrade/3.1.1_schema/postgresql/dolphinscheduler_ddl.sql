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

--- rename t_ds_fav_task task_name to task_type
DO $$
DECLARE
v_schema varchar;
BEGIN
    v_schema =current_schema();
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='t_ds_fav_task' and column_name='task_name')
  then
   EXECUTE 'ALTER TABLE IF EXISTS ' || quote_ident(v_schema) ||'.t_ds_fav_task RENAME COLUMN task_name TO task_type';
END IF;
END $$;

--- add column
ALTER TABLE t_ds_worker_group ADD COLUMN IF NOT EXISTS description varchar(255) DEFAULT NULL;
