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

package org.apache.dolphinscheduler.plugin.task.dinky;

/**
 * Custom DinkyTaskConstants
 */
public class DinkyTaskConstants {

    private DinkyTaskConstants() {
        throw new IllegalStateException("Utility class");
    }

    private static final String API_ROUTE = "/openapi/";
    public static final String SUBMIT_TASK = API_ROUTE + "submitTask";
    public static final String ONLINE_TASK = API_ROUTE + "onLineTask";
    public static final String SAVEPOINT_TASK = API_ROUTE + "savepointTask";
    public static final String GET_JOB_INFO = API_ROUTE + "getJobInstance";
    public static final int API_ERROR = 1;
    public static final String API_VERSION_ERROR_TIPS =
            "Please check that the dinky version is greater than or equal to 0.6.5";
    public static final String API_RESULT_DATAS = "datas";

    public static final String SAVEPOINT_CANCEL = "cancel";

    public static final String PARAM_TASK_ID = "id";
    public static final String PARAM_JSON_TASK_ID = "taskId";
    public static final String PARAM_SAVEPOINT_TYPE = "type";
    public static final String PARAM_JOB_INSTANCE_ID = "id";

    public static final String STATUS_FINISHED = "FINISHED";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    public static final long SLEEP_MILLIS = 3000;

}
