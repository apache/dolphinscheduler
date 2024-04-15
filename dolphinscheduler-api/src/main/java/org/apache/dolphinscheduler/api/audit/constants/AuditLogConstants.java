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

package org.apache.dolphinscheduler.api.audit.constants;

public final class AuditLogConstants {

    private AuditLogConstants() {
        throw new UnsupportedOperationException("Construct Constants");
    }

    public static final String CODE = "code";
    public static final String CODES = "codes";
    public static final String VERSION = "version";
    public static final String PROCESS_DEFINITION_CODE = "processDefinitionCode";
    public static final String PROCESS_DEFINITION_CODES = "processDefinitionCodes";
    public static final String PROCESS_INSTANCE_IDS = "processInstanceIds";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String WORKFLOW_DEFINITION_CODE = "workflowDefinitionCode";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String USER_ID = "userId";
    public static final String QUEUE_ID = "queueId";
    public static final String PRIORITY = "priority";
    public static final String CLUSTER_CODE = "clusterCode";
    public static final String ENVIRONMENT_CODE = "environmentCode";
    public static final String ALIAS = "alias";
    public static final String FILE_NAME = "fileName";
    public static final String FULL_NAME = "fullName";
    public static final String FUNC_NAME = "funcName";
    public static final String UDF_FUNC_ID = "udfFuncId";

}
