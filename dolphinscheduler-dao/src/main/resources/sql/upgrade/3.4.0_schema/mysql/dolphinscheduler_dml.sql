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

UPDATE t_ds_datasource SET connection_params = REPLACE(connection_params, '"publicKey"', '"privateKey"') WHERE type = 17 AND connection_params LIKE '%"publicKey"%';
INSERT INTO t_ds_relation_project_user (user_id, project_id, perm, create_time, update_time) SELECT user_id, id, 99, now(), now() FROM t_ds_project;
INSERT INTO t_ds_relation_datasource_user (user_id, datasource_id, perm, create_time, update_time) SELECT user_id, id, 7, now(), now() FROM t_ds_datasource;