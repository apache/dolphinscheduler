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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import java.io.IOException;
import java.util.Map;

public interface IShellInterceptorBuilder<T extends IShellInterceptorBuilder<T, Y>, Y extends IShellInterceptor> {

    T newBuilder();

    T newBuilder(T builder);

    T shellDirectory(String directory);

    T shellName(String shellFilename);

    T runUser(String systemUser);

    T cpuQuota(Integer cpuQuota);

    T memoryQuota(Integer memoryQuota);

    T appendSystemEnv(String envFiles);

    T appendCustomEnvScript(String customEnvScript);

    T k8sConfigYaml(String k8sConfigYaml);

    T properties(Map<String, String> propertyMap);

    T sudoMode(boolean sudoEnable);

    T appendScript(String script);

    Y build() throws IOException;
}
