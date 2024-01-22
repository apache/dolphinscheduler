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
alter table t_ds_process_definition alter column `version` int(11) NOT NULL default 1;
alter table t_ds_process_definition_log alter column `version` int(11) NOT NULL default 1;
alter table t_ds_task_definition alter column `version` int(11) NOT NULL default 1;
alter table t_ds_task_definition_log alter column `version` int(11) NOT NULL default 1;
alter table t_ds_process_instance alter column `process_definition_version` int(11) NOT NULL default 1;
alter table t_ds_task_instance alter column `task_definition_version` int(11) NOT NULL default 1;