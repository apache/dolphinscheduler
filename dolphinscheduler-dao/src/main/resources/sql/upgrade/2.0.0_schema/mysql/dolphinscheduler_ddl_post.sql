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

alter table t_ds_process_definition drop primary key, ADD PRIMARY KEY (`id`,`code`);
ALTER TABLE t_ds_process_definition drop KEY `process_definition_unique`;
ALTER TABLE t_ds_process_definition drop KEY `process_definition_index`;
alter table t_ds_process_definition drop process_definition_json;
alter table t_ds_process_definition drop connects;
alter table t_ds_process_definition drop receivers;
alter table t_ds_process_definition drop receivers_cc;
alter table t_ds_process_definition drop modify_by;
alter table t_ds_process_definition drop resource_ids;