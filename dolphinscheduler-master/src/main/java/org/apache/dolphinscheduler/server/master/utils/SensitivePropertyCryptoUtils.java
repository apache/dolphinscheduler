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
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.experimental.UtilityClass;

/**
 * Load-boundary decrypt for definition-time sensitive values at execution.
 * Call immediately after loading workflow/task definition rows into the run context;
 * subsequent engine logic stays on plaintext. Worker receives plaintext copies only.
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

    /** Load-boundary helper for workflow definition {@code globalParams} JSON. */
    public String decodeGlobalParams(String globalParams) {
        if (StringUtils.isEmpty(globalParams)) {
            return globalParams;
        }
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(decodeSensitiveValues(properties));
    }
}
