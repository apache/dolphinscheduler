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

-- t_ds_k8s_namespace
ALTER TABLE "t_ds_k8s_namespace" DROP COLUMN IF EXISTS "limits_cpu";
ALTER TABLE "t_ds_k8s_namespace" DROP COLUMN IF EXISTS "limits_memory";
ALTER TABLE "t_ds_k8s_namespace" DROP COLUMN IF EXISTS "pod_replicas";
ALTER TABLE "t_ds_k8s_namespace" DROP COLUMN IF EXISTS "pod_request_cpu";
ALTER TABLE "t_ds_k8s_namespace" DROP COLUMN IF EXISTS "pod_request_memory";

ALTER TABLE t_ds_project_parameter ALTER COLUMN param_value TYPE text;

ALTER TABLE "t_ds_process_definition" ALTER COLUMN "version" SET DEFAULT 1;
ALTER TABLE "t_ds_process_definition_log" ALTER COLUMN "version" SET DEFAULT 1;
ALTER TABLE "t_ds_task_definition" ALTER COLUMN "version" SET DEFAULT 1;
ALTER TABLE "t_ds_task_definition_log" ALTER COLUMN "version" SET DEFAULT 1;
ALTER TABLE "t_ds_process_instance" ALTER COLUMN "process_definition_version" SET NOT NULL, ALTER COLUMN "process_definition_version" SET DEFAULT 1;
ALTER TABLE "t_ds_task_instance" ALTER COLUMN "task_definition_version" SET NOT NULL, ALTER COLUMN "task_definition_version" SET DEFAULT 1;