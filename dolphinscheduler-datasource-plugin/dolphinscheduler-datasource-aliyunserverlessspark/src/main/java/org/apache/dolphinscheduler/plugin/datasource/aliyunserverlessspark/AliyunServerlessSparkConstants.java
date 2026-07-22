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

package org.apache.dolphinscheduler.plugin.datasource.aliyunserverlessspark;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AliyunServerlessSparkConstants {

    public String ENDPOINT_TEMPLATE = "emr-serverless-spark.%s.aliyuncs.com";

    public String DEFAULT_ENGINE = "esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)";

    public String ENV_PROD = "production";

    public String ENV_DEV = "dev";

    public String ENTRY_POINT_ARGUMENTS_DELIMITER = "#";

    public String ENV_KEY = "environment";

    public String WORKFLOW_KEY = "workflow";

    public String WORKFLOW_VALUE = "true";

}
