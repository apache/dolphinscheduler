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

package org.apache.dolphinscheduler.plugin.task.flink.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.plugin.task.flink.enums.FlinkStreamDeployMode;

import java.util.Properties;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FlinkParamsInfo {

    private String name;
    private String queue;
    private FlinkStreamDeployMode runMode;
    private String runJarPath;
    private String flinkConfDir;
    private String flinkJarPath;
    private String flinkVersion;
    private String hadoopConfDir;
    private String applicationId;
    private String flinkJobId;
    private String entryPointClassName;
    private String[] dependFiles;
    private String[] execArgs;
    private Properties confProperties;

    /** security config */
    private boolean openSecurity;

    private String krb5Path;
    private String principal;
    private String keytabPath;
    private boolean cacheUgi;

    /** checkpoint path in hdfs */
    private String hdfsPath;

    /** finished job print log dir */
    private String finishedJobLogDir;
}
