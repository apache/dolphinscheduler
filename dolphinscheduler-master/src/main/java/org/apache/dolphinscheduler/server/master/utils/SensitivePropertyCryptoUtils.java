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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import java.util.List;

import lombok.experimental.UtilityClass;

/**
 * Decrypt definition-time sensitive values for execution.
 * API/UI still mask; Worker receives plaintext copies only.
 */
@UtilityClass
public class SensitivePropertyCryptoUtils {

    public List<Property> decodeSensitiveValues(List<Property> properties) {
        return PropertySensitiveUtils.transformSensitiveValues(properties, PasswordUtils::decodePassword);
    }

    public String decodeLocalParamsInTaskParams(String taskParams) {
        return PropertySensitiveUtils.transformLocalParamsInTaskParams(taskParams,
                SensitivePropertyCryptoUtils::decodeSensitiveValues);
    }
}
