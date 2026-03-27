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

package org.apache.dolphinscheduler.api.validator;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Validates global parameters: non-empty keys, no duplicates
 * <p> If globalParams is not valid, an {@link IllegalArgumentException} will be thrown. </p>
 */
@Slf4j
@Component
public class GlobalParamsValidator implements IValidator<String> {

    @Override
    public void validate(String globalParams) {
        if (StringUtils.isBlank(globalParams)) {
            return;
        }

        List<Property> params;
        try {
            params = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid globalParams", ex);
        }

        if (params == null || params.isEmpty()) {
            return;
        }

        Set<String> keys = new HashSet<>();
        for (Property p : params) {
            if (StringUtils.isBlank(p.getProp())) {
                throw new IllegalArgumentException("Global param key cannot be empty");
            }

            String key = p.getProp().trim();
            if (!keys.add(key)) {
                throw new IllegalArgumentException("Duplicate global param key: " + key);
            }
        }
    }
}
