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

package org.apache.dolphinscheduler.api.it.core.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface DatabaseCleaner {

    /**
     * All t_ds_* tables defined in dolphinscheduler-dao/src/main/resources/sql/dolphinscheduler_h2.sql.
     * Shared between H2 and MySQL cleaners so new dialects only need to implement the TRUNCATE command.
     * Order does not matter because referential integrity is disabled during clean.
     * Append new tables here when the schema grows.
     *
     * <p>Excluded on purpose:
     * <ul>
     *   <li>{@code t_ds_version} — schema metadata</li>
     *   <li>{@code t_ds_jdbc_registry_*} — owned by the registry module</li>
     *   <li>{@code QRTZ_*} — owned by Quartz</li>
     * </ul>
     */
    List<String> TABLES = Collections.unmodifiableList(Arrays.asList(
            "t_ds_access_token",
            "t_ds_alert",
            "t_ds_alert_plugin_instance",
            "t_ds_alert_send_status",
            "t_ds_alertgroup",
            "t_ds_audit_log",
            "t_ds_cluster",
            "t_ds_command",
            "t_ds_datasource",
            "t_ds_environment",
            "t_ds_environment_worker_group_relation",
            "t_ds_error_command",
            "t_ds_fav_task",
            "t_ds_k8s",
            "t_ds_k8s_namespace",
            "t_ds_plugin_define",
            "t_ds_project",
            "t_ds_project_parameter",
            "t_ds_project_preference",
            "t_ds_queue",
            "t_ds_relation_datasource_user",
            "t_ds_relation_namespace_user",
            "t_ds_relation_project_user",
            "t_ds_relation_project_worker_group",
            "t_ds_relation_resources_user",
            "t_ds_relation_sub_workflow",
            "t_ds_relation_udfs_user",
            "t_ds_relation_workflow_instance",
            "t_ds_resources",
            "t_ds_schedules",
            "t_ds_serial_command",
            "t_ds_session",
            "t_ds_task_definition",
            "t_ds_task_definition_log",
            "t_ds_task_group",
            "t_ds_task_group_queue",
            "t_ds_task_instance",
            "t_ds_task_instance_context",
            "t_ds_tenant",
            "t_ds_udfs",
            "t_ds_user",
            "t_ds_worker_group",
            "t_ds_workflow_definition",
            "t_ds_workflow_definition_log",
            "t_ds_workflow_instance",
            "t_ds_workflow_task_lineage",
            "t_ds_workflow_task_relation",
            "t_ds_workflow_task_relation_log"));

    /** Truncate all IT-managed tables (without dropping or recreating schema). */
    void cleanAll();
}
