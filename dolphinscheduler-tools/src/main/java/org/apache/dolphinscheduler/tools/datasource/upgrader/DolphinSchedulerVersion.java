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

package org.apache.dolphinscheduler.tools.datasource.upgrader;

import java.util.Optional;

public enum DolphinSchedulerVersion {

    V1_3_0("1.3.0"),
    V1_3_2("1.3.2"),
    V2_0_0("2.0.0"),
    V3_2_0("3.2.0"),
    ;
    private final String versionName;

    DolphinSchedulerVersion(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public static Optional<DolphinSchedulerVersion> getVersion(String versionName) {
        for (DolphinSchedulerVersion version : DolphinSchedulerVersion.values()) {
            if (version.getVersionName().equals(versionName)) {
                return Optional.of(version);
            }
        }
        return Optional.empty();
    }
}
