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

package org.apacheduler.dolphinscheduler.plugin.task.flinkx;

public class FlinkxConstants {

    private FlinkxConstants() {
        throw new UnsupportedOperationException("Construct FlinkxConstants");
    }

    public static final String FLINKX_MODE = "-mode";

    public static final String FLINKX_JOB_ID = "-jobid";

    public static final String FLINKX_JOB = "-job";

    public static final String FLINKX_CONF = "-flinkconf";

    public static final String FLINKX_LOCAL_PATH = "${FLINKX_HOME}/flinkconf";

    public static final String FLINKX_CLUSTER_PATH = "${FLINK_HOME}/conf";

    public static final String FLINKX_PLUGIN = "-pluginRoot";

    public static final String FLINKX_PULGIN_PATH = "${FLINKX_HOME}/syncplugins";

}
