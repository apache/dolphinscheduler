/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.dolphinscheduler.skywalking.plugin;

import org.apache.skywalking.apm.agent.core.context.tag.StringTag;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

import java.lang.reflect.Method;

public class Utils {
    public static final OfficialComponent DOLPHIN_SCHEDULER = ComponentsDefine.DOLPHIN_SCHEDULER;

    public static final String SKYWALKING_TRACING_CONTEXT = "skywalking_tracing_context";
    public static final String MASTER_PROCESS_EXECUTION_STATUS = "master_process_execution_status";

    public static final StringTag TAG_PROJECT_ID = new StringTag("project.id");

    public static final StringTag TAG_PROCESS_STATE = new StringTag("process.state");
    public static final StringTag TAG_PROCESS_INSTANCE_ID = new StringTag("process.instance.id");
    public static final StringTag TAG_PROCESS_INSTANCE_NAME = new StringTag("process.instance.name");
    public static final StringTag TAG_PROCESS_INSTANCE_HOST = new StringTag("process.instance.host");
    public static final StringTag TAG_PROCESS_DEFINITION_ID = new StringTag("process.definitionId");
    public static final StringTag TAG_PROCESS_COMMAND_TYPE = new StringTag("process.commandType");
    public static final StringTag TAG_PROCESS_WORKER_GROUP = new StringTag("process.workerGroup");
    public static final StringTag TAG_PROCESS_TIMEOUT = new StringTag("process.timeout");

    public static final StringTag TAG_TASK_STATE = new StringTag("task.state");
    public static final StringTag TAG_TASK_TYPE = new StringTag("take.type");
    public static final StringTag TAG_TASK_INSTANCE_ID = new StringTag("task.instance.id");
    public static final StringTag TAG_TASK_INSTANCE_NAME = new StringTag("task.instanceName");
    public static final StringTag TAG_TASK_INSTANCE_HOST = new StringTag("task.instance.host");
    public static final StringTag TAG_TASK_WORKER_GROUP = new StringTag("task.workerGroup");
    public static final StringTag TAG_TASK_ID = new StringTag("task.id");
    public static final StringTag TAG_TASK_EXECUTE_PATH = new StringTag("task.executePath");
    public static final StringTag TAG_TASK_LOG_PATH = new StringTag("task.logPath");
    public static final StringTag TAG_TASK_PARAMS = new StringTag("task.params");

    public static final StringTag TAG_NETTY_REMOTE_ADDRESS = new StringTag("netty.remoteAddress");

    public static final StringTag TAG_EXECUTE_METHOD = new StringTag("execute.method");

    public static String getProjectId(int id) {
        return "project_id_" + id;
    }

    public static String getProcessDefinitionId(int id) {
        return "process_definition_id_" + id;
    }

    public static String getMethodName(Method method) {
        return method.getDeclaringClass().getTypeName() + "#" + method.getName();
    }
}
