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

-- Normalize the default tenant created with queue_id = 0 by the PostgreSQL 3.2.0 upgrade.
UPDATE t_ds_tenant
SET queue_id = (SELECT id FROM t_ds_queue WHERE queue = 'default')
WHERE id = -1 AND tenant_code = 'default' AND queue_id = 0
  AND EXISTS (SELECT 1 FROM t_ds_queue WHERE queue = 'default');

-- Historical permissions are retained in the legacy tables without migration.
