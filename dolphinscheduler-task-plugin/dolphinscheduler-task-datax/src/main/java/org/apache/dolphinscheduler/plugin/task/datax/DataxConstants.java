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

package org.apache.dolphinscheduler.plugin.task.datax;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

public class DataxConstants {

    private DataxConstants() {
        throw new IllegalStateException("Utility class");
    }

    private static final String DATAX_YARN_JAR_CONFIG = "datax.yarn.jar";

    private static final String DATAX_YARN_BIN_CONFIG = "datax.yarn.bin";

    private static final String DATAX_YARN_DEFAULT_QUEUE_CONFIG = "datax.yarn.default.queue";

    /**
     * datax on yarn jar path
     */
    public static final String DATAX_YARN_JAR = PropertyUtils.getString(DataxConstants.DATAX_YARN_JAR_CONFIG);

    /**
     * example: HADOOP_OPTS="-Xms32m -Xmx128m" /usr/bin/yarn
     */
    public static final String DATAX_YARN_BIN = PropertyUtils.getString(DataxConstants.DATAX_YARN_BIN_CONFIG, "yarn");

    public static final String DATAX_YARN_DEFAULT_QUEUE =
            PropertyUtils.getString(DataxConstants.DATAX_YARN_DEFAULT_QUEUE_CONFIG, "default");
}
