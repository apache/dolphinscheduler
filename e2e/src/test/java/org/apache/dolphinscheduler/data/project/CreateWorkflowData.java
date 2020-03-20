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
package org.apache.dolphinscheduler.data.project;

public class CreateWorkflowData {
    /**
     * create workflow data
     */
    //input shell task name
    public static final String SHELL_TASK_NAME = "shell_task_selenium_1";

    //input shell task description
    public static final String SHELL_TASK_DESCRIPTION = "shell task description test";

    //input timeout
    public static final String INPUT_TIMEOUT = "60";

    //input shell script
    public static final String SHELL_SCRIPT = "echo 1111111";

    //input custom parameters
    public static final String INPUT_CUSTOM_PARAMETERS = "selenium_parameter";

    //input custom parameters value
    public static final String INPUT_CUSTOM_PARAMETERS_VALUE = "selenium_parameter_123";

    //input add custom parameters
    public static final String INPUT_ADD_CUSTOM_PARAMETERS = "selenium_parameter_delete";

    //input add custom parameters value
    public static final String INPUT_ADD_CUSTOM_PARAMETERS_VALUE = "selenium_parameter_delete_456";

    //workflow define title
    public static final String WORKFLOW_TITLE = "工作流定义 - DolphinScheduler";

    //create workflow title
    public static final String CREATE_WORKFLOW_TITLE = "创建流程定义 - DolphinScheduler";


    /**
     * save workflow data
     */
    //input  workflow name
    public static final String INPUT_WORKFLOW_NAME = "selenium_shell_1";

    //input  workflow description
    public static final String INPUT_WORKFLOW_DESCRIPTION = "test selenium_shell_1 description";

    //input workflow timeout
    public static final String INPUT_WORKFLOW_TIMEOUT = "30";

    //input workflow  global parameters
    public static final String INPUT_WORKFLOW_GLOBAL_PARAMETERS = "selenium_global_parameters_1";

    //input workflow  global parameters value
    public static final String INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES = "selenium_global_parameters_value_1";

    //input to  add workflow  global parameters
    public static final String INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS = "selenium_global_parameters_2";

    //input to add workflow  global parameters value
    public static final String INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES = "selenium_global_parameters_value_2";
}
