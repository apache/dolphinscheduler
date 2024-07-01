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

package org.apache.dolphinscheduler.common.constants;

/**
 * The key org.apache.dolphinscheduler.dao.entity.Command#commandParam
 */
public class CommandKeyConstants {

    /**
     * command parameter keys
     */
    public static final String CMD_PARAM_RECOVER_PROCESS_ID_STRING = "ProcessInstanceId";

    public static final String CMD_PARAM_RECOVERY_START_NODE_STRING = "StartNodeIdList";

    public static final String CMD_PARAM_RECOVERY_WAITING_THREAD = "WaitingThreadInstanceId";

    public static final String CMD_PARAM_SUB_PROCESS = "processInstanceId";

    public static final String CMD_PARAM_EMPTY_SUB_PROCESS = "0";

    public static final String CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID = "parentProcessInstanceId";

    public static final String CMD_PARAM_SUB_PROCESS_DEFINE_CODE = "processDefinitionCode";

    public static final String CMD_PARAM_START_NODES = "StartNodeList";

    public static final String CMD_PARAM_START_PARAMS = "StartParams";

    public static final String CMD_PARAM_FATHER_PARAMS = "fatherParams";

    public static final String CMD_DYNAMIC_START_PARAMS = "dynamicParams";

    /**
     * complement data start date
     */
    public static final String CMD_PARAM_COMPLEMENT_DATA_START_DATE = "complementStartDate";

    /**
     * complement data end date
     */
    public static final String CMD_PARAM_COMPLEMENT_DATA_END_DATE = "complementEndDate";

    /**
     * complement data Schedule date
     */
    public static final String CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST = "complementScheduleDateList";
}
