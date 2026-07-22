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

package org.apache.dolphinscheduler.e2e.models.environment;

import lombok.Data;

@Data
public class PythonEnvironment implements IEnvironment {

    private String environmentName;

    private String environmentConfig;

    private String environmentDesc;

    private String environmentWorkerGroup;

    @Override
    public String getEnvironmentName() {
        return "python-e2e";
    }

    @Override
    public String getEnvironmentConfig() {
        return "export PYTHON_LAUNCHER=/usr/bin/python3";
    }

    @Override
    public String getEnvironmentDesc() {
        return "pythonEnvDesc";
    }

    @Override
    public String getEnvironmentWorkerGroup() {
        return "default";
    }
}
