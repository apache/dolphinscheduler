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

ALTER TABLE t_ds_schedule ALTER COLUMN warning_type TYPE VARCHAR(64);
ALTER TABLE t_ds_schedule ALTER COLUMN warning_type DROP NOT NULL;
ALTER TABLE t_ds_schedule ALTER COLUMN warning_type SET DEFAULT NULL;

COMMENT ON COLUMN t_ds_schedule.warning_type IS 'Warning type: comma-separated codes like 1,2,3 (1=SUCCESS,2=FAILURE,3=TIMEOUT)';

ALTER TABLE t_ds_process_instance ALTER COLUMN warning_type TYPE VARCHAR(64);
ALTER TABLE t_ds_process_instance ALTER COLUMN warning_type SET DEFAULT NULL;

ALTER TABLE t_ds_command ALTER COLUMN warning_type TYPE VARCHAR(64);
ALTER TABLE t_ds_command ALTER COLUMN warning_type SET DEFAULT NULL;

ALTER TABLE t_ds_error_command ALTER COLUMN warning_type TYPE VARCHAR(64);
ALTER TABLE t_ds_error_command ALTER COLUMN warning_type SET DEFAULT NULL;

