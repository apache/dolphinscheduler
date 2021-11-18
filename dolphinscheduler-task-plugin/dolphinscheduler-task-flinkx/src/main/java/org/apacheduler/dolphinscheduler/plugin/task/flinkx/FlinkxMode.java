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

import org.apache.dolphinscheduler.spi.utils.StringUtils;

public enum FlinkxMode {

    /**
     * 本地模式运行
     */
    local(0, "local"),

    /**
     * flink集群 standalone模式
     */
    standalone(1, "standalone"),

    /**
     * 在已经启动在yarn上的flink session里上运行
     */
    yarn(2, "yarn"),

    /**
     * 在yarn上单独启动flink session运行
     */
    yarnPer(3, "yarnPer");

    private int type;

    private String name;

    FlinkxMode(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public static FlinkxMode getByName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("ClusterMode name cannot be null or empty");
        }
        switch (name) {
            case "standalone":
                return standalone;
            case "yarn":
                return yarn;
            case "yarnPer":
                return yarnPer;
            default:
                return local;
        }
    }

}
