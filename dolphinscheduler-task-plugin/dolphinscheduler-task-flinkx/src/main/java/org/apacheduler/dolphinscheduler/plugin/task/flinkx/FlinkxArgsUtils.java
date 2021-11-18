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

import java.util.ArrayList;
import java.util.List;

public class FlinkxArgsUtils {

    private FlinkxArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> buildArgs(String flinkxJobId, String jsonFilePath, FlinkxParameters param) {
        List<String> args = new ArrayList<>();
        args.add(FlinkxConstants.FLINKX_MODE);
        args.add(param.getDeployMode());

        args.add(FlinkxConstants.FLINKX_JOB_ID);
        args.add(flinkxJobId);

        args.add(FlinkxConstants.FLINKX_JOB);
        args.add(jsonFilePath);

        if (FlinkxMode.local.name().equalsIgnoreCase(param.getDeployMode())) {
            args.add(FlinkxConstants.FLINKX_CONF);
            args.add(FlinkxConstants.FLINKX_LOCAL_PATH);
        } else {
            args.add(FlinkxConstants.FLINKX_CONF);
            args.add(FlinkxConstants.FLINKX_CLUSTER_PATH);
        }

        args.add(FlinkxConstants.FLINKX_PLUGIN);
        args.add(FlinkxConstants.FLINKX_PULGIN_PATH);

        return args;
    }
}
