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

-- Enforce idempotent task-result alerts at the database level.
-- Allows INSERT IGNORE (MySQL) / ON CONFLICT DO NOTHING (PostgreSQL) to atomically
-- prevent duplicates without check-then-insert race conditions.
-- Clean up any existing duplicate rows before adding the unique constraint.
DELETE FROM t_ds_alert a
USING t_ds_alert b
WHERE a.id < b.id
  AND a.sign = b.sign
  AND a.workflow_instance_id = b.workflow_instance_id
  AND a.alert_type = b.alert_type;
CREATE UNIQUE INDEX IF NOT EXISTS uk_alert_dedup ON t_ds_alert (sign, workflow_instance_id, alert_type);
