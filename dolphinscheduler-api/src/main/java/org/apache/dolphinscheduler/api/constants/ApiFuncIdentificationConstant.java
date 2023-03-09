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

package org.apache.dolphinscheduler.api.constants;

import org.apache.dolphinscheduler.api.enums.ExecuteType;

import java.util.HashMap;
import java.util.Map;

public class ApiFuncIdentificationConstant {

    public static final String ACCESS_TOKEN_MANAGE = "security:token:view";
    public static final String ACCESS_TOKEN_VIEW = "security:token:view";
    public static final String ACCESS_TOKEN_CREATE = "security:token:create";
    public static final String ACCESS_TOKEN_UPDATE = "security:token:update";
    public static final String ACCESS_TOKEN_DELETE = "security:token:delete";
    public static final String ALERT_GROUP_VIEW = "security:alert-group:view";
    public static final String ALERT_GROUP_CREATE = "security:alert-group:create";
    public static final String ALERT_GROUP_UPDATE = "security:alert-group:update";
    public static final String ALERT_GROUP_DELETE = "security:alert-group:delete";
    public static final String TENANT_MANAGER = "security:tenant:view";
    public static final String TENANT_CREATE = "security:tenant:create";
    public static final String TENANT_UPDATE = "security:tenant:update";
    public static final String TENANT_DELETE = "security:tenant:delete";
    public static final String ALART_LIST = "monitor:alert:view";
    public static final String ALART_INSTANCE_CREATE = "security:alert-plugin:create";
    public static final String ALERT_PLUGIN_UPDATE = "security:alert-plugin:update";
    public static final String ALERT_PLUGIN_DELETE = "security:alert-plugin:delete";
    public static final String WORKER_GROUP_CREATE = "security:worker-group:create";
    public static final String WORKER_GROUP_EDIT = "security:worker-group:update";
    public static final String WORKER_GROUP_DELETE = "security:worker-group:delete";
    public static final String YARN_QUEUE_MANAGE = "security:queue:view";
    public static final String YARN_QUEUE_CREATE = "security:queue:create";
    public static final String YARN_QUEUE_UPDATE = "security:queue:update";
    public static final String ENVIRONMENT_CREATE = "security:environment:create";
    public static final String ENVIRONMENT_UPDATE = "security:environment:update";
    public static final String ENVIRONMENT_DELETE = "security:environment:delete";
    public static final String ENVIRONMENT_MANAGER = "security:environment:view";
    public static final String CALENDAR_CREATE = "security:calendar:create";
    public static final String CALENDAR_EDIT = "security:calendar:update";
    public static final String CALENDAR_DELETE = "security:calendar:delete";
    public static final String CARDS_CREATE = "security:cards:create";
    public static final String CARDS_EDIT = "security:cards:update";
    public static final String CARDS_EDIT_VALUE = "security:cards:value";
    public static final String CARDS_CARDS_DELETE = "security:cards:delete";
    public static final String ALARM_GROUP_MANAGE = "security:alert-group:view";
    public static final String ALARM_INSTANCE_MANAGE = "security:alert-plugin:view";
    public static final String WORKER_GROUP_MANAGE = "security:worker-group:view";
    public static final String CALENDAR_MANAGE = "security:calendar:view";
    public static final String CARDS_MANAGER = "security:cards:view";
    public static final String USER_MANAGER = "security:user:view";

    public static final String PROJECT = "project:view";
    public static final String PROJECT_CREATE = "project:create";
    public static final String PROJECT_UPDATE = "project:edit";
    public static final String PROJECT_DELETE = "project:delete";
    public static final String WORKFLOW_DEFINITION = "project:definition:list";
    public static final String WORKFLOW_CREATE = "project:definition:create";
    public static final String WORKFLOW_IMPORT = "project:definition:import";
    public static final String WORKFLOW_UPDATE = "project:definition:update";
    public static final String WORKFLOW_START = "project:executors:start";
    public static final String TIMING = "project:schedules:timing";
    public static final String WORKFLOW_ONLINE_OFFLINE = "project:definition:release";
    public static final String WORKFLOW_COPY = "project:definition:copy";
    public static final String CRON_MANAGE = "project:schedules:corn";
    public static final String WORKFLOW_DELETE = "project:definition:delete";
    public static final String WORKFLOW_TREE_VIEW = "project:definition:view-tree";
    public static final String WORKFLOW_EXPORT = "project:definition:export";
    public static final String WORKFLOW_BATCH_COPY = "project:definition:batch-copy";
    public static final String WORKFLOW_DEFINITION_EXPORT = "project:definition:batch-export";
    public static final String WORKFLOW_DEFINITION_BATCH_DELETE = "project:definition:batch-delete";
    public static final String WORKFLOW_SWITCH_TO_THIS_VERSION = "project:definition:version:switch";
    public static final String WORKFLOW_DEFINITION_DELETE = "project:definition:version:delete";
    public static final String WORKFLOW_SAVE = "project:definition:verify-name";
    public static final String WORKFLOW_INSTANCE = "project:process-instance:list";
    public static final String RERUN = "project:executors:execute";
    public static final String FAILED_TO_RETRY = "project:executors:retry";
    public static final String STOP = "project:executors:stop";
    public static final String RECOVERY_SUSPEND = "project:executors:recover";
    public static final String PAUSE = "project:executors:pause";
    public static final String INSTANCE_DELETE = "project:process-instance:delete";
    public static final String INSTANCE_BATCH_DELETE = "project:process-instance:batch-delete";
    public static final String FORCED_SUCCESS = "project:task-instance:force-success";
    public static final String VIEW_LOG = "project:log:detail";
    public static final String DOWNLOAD_LOG = "project:log:download-log";
    public static final String PROJECT_OVERVIEW = "project:overview:view";
    public static final String WORKFLOW_RELATION = "project:lineages:view";
    public static final String WORKFLOW_DEFINITION_VIEW = "project:definition:view";
    public static final String WORKFLOW_INSTANCE_VIEW = "project:process-instance:view";
    public static final String TASK_INSTANCE = "project:task-instance:view";
    public static final String INSTANCE_UPDATE = "project:process-instance:update";
    public static final String VERSION_LIST = "project:version:list";
    public static final String TASK_DEFINITION = "project:task-definition:view";
    public static final String TASK_DEFINITION_CREATE = "project:task-definition:create";
    public static final String TASK_DEFINITION_UPDATE = "project:task-definition:edit";
    public static final String TASK_DEFINITION_MOVE = "project:task-definition:move";
    public static final String TASK_VERSION_VIEW = "project:task-definition:version";
    public static final String TASK_DEFINITION_DELETE = "project:task-definition:delete";
    public static final String VERSION_DELETE = "project:version:delete";

    public static final String DATASOURCE = "datasource:view";
    public static final String DATASOURCE_CREATE_DATASOURCE = "datasource:create";
    public static final String DATASOURCE_UPDATE = "datasource:update";
    public static final String DATASOURCE_DELETE = "datasource:delete";
    public static final String DATASOURCE_LIST = "datasource:list";
    public static final String DATASOURCE_PARAM_VIEW = "datasource:param-view";

    public static final String FILE_VIEW = "resources:file:view";
    public static final String FOLDER_ONLINE_CREATE = "resources:file:create";
    public static final String FILE_ONLINE_CREATE = "resources:file:online-create";
    public static final String FILE_UPLOAD = "resources:file:upload";
    public static final String FILE_UPDATE = "resources:file:update-content";
    public static final String FILE_RENAME = "resources:file:rename";
    public static final String FILE_DOWNLOAD = "resources:file:download";
    public static final String FILE_DELETE = "resources:file:delete";

    public static final String UDF_FILE_VIEW = "resources:udf:view";
    public static final String UDF_FOLDER_ONLINE_CREATE = "resources:udf:create";
    public static final String UDF_UPLOAD = "resources:udf:upload";
    public static final String UDF_UPDATE = "resources:udf:edit";
    public static final String UDF_DOWNLOAD = "resources:udf:download";
    public static final String UDF_DELETE = "resources:udf:delete";

    public static final String UDF_FUNCTION_VIEW = "resources:udf-func:view";
    public static final String UDF_FUNCTION_CREATE = "resources:udf-func:create";
    public static final String UDF_FUNCTION_UPDATE = "resources:udf-func:update";
    public static final String UDF_FUNCTION_DELETE = "resources:udf-func:delete";

    public static final String TASK_GROUP_VIEW = "resources:task-group:view";
    public static final String TASK_GROUP_CREATE = "resources:task-group:create";
    public static final String TASK_GROUP_CLOSE = "resources:task-group:close";
    public static final String TASK_GROUP_EDIT = "resources:task-group:update";
    public static final String TASK_GROUP_VIEW_QUEUE = "resources:task-group:queue-view";

    public static final String TASK_GROUP_QUEUE = "resources:task-group-queue:view";
    public static final String TASK_GROUP_QUEUE_PRIORITY = "resources:task-group-queue:priority";
    public static final String TASK_GROUP_QUEUE_START = "resources:task-group-queue:start";

    public static final String MONITOR_MASTER_VIEW = "monitor:masters:view";
    public static final String MONITOR_WORKER_VIEW = "monitor:workers:view";
    public static final String MONITOR_DATABASES_VIEW = "monitor:databases:view";

    public static final String MONITOR_STATISTICS_VIEW = "monitor:statistics:view";

    public final static Map<ExecuteType, String> map = new HashMap<ExecuteType, String>();

    static {
        for (ExecuteType type : ExecuteType.values()) {
            switch (type) {
                case REPEAT_RUNNING:
                    map.put(type, RERUN);
                    break;
                case RECOVER_SUSPENDED_PROCESS:
                    map.put(type, RECOVERY_SUSPEND);
                    break;
                case START_FAILURE_TASK_PROCESS:
                    map.put(type, FAILED_TO_RETRY);
                    break;
                case STOP:
                    map.put(type, STOP);
                    break;
                case PAUSE:
                    map.put(type, PAUSE);
                    break;
                case NONE:
                    map.put(type, null);
                    break;
                default:
            }
        }
    }

}
