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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Validator for the list of start parameters (Property list).
 * <p> If startParamList is not valid, an {@link IllegalArgumentException} will be thrown. </p>
 */
@Slf4j
@Component
public class StartParamListValidator implements IValidator<List<Property>> {

    @Override
    public void validate(List<Property> startParamList) {
        if (CollectionUtils.isEmpty(startParamList)) {
            return;
        }

        Set<String> keys = new HashSet<>();
        for (Property param : startParamList) {
            if (StringUtils.isBlank(param.getProp())) {
                throw new IllegalArgumentException("Parameter key cannot be empty");
            }

            String key = param.getProp().trim();
            if (keys.contains(key)) {
                throw new IllegalArgumentException("Duplicate parameter key: " + key);
            }
            keys.add(key);
        }
    }
}
