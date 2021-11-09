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

ALTER TABLE "t_ds_process_definition" DROP CONSTRAINT "t_ds_process_definition_pkey";
ALTER TABLE "t_ds_process_definition" ADD CONSTRAINT "t_ds_process_definition_pkey" PRIMARY KEY ("id","code");
ALTER TABLE "t_ds_process_definition" DROP CONSTRAINT "process_definition_unique";
DROP INDEX "process_definition_index";
ALTER TABLE "t_ds_process_definition" DROP "process_definition_json";
ALTER TABLE "t_ds_process_definition" DROP "connects";
ALTER TABLE "t_ds_process_definition" DROP "receivers";
ALTER TABLE "t_ds_process_definition" DROP "receivers_cc";
ALTER TABLE "t_ds_process_definition" DROP "modify_by";
ALTER TABLE "t_ds_process_definition" DROP "resource_ids";