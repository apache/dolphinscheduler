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

package org.apache.dolphinscheduler.plugin.storage.api;

import java.util.Optional;

public enum StorageType {

    LOCAL(0, "LOCAL"),
    HDFS(1, "HDFS"),
    OSS(2, "OSS"),
    S3(3, "S3"),
    GCS(4, "GCS"),

    ABS(5, "ABS"),

    OBS(6, "OBS");

    private final int code;
    private final String name;

    StorageType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Optional<StorageType> getStorageType(String name) {
        for (StorageType storageType : StorageType.values()) {
            if (storageType.getName().equals(name)) {
                return Optional.of(storageType);
            }
        }
        return Optional.empty();
    }
}
