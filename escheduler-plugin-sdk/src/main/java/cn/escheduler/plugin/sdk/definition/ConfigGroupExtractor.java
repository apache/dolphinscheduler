/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.definition;

import cn.escheduler.plugin.sdk.config.ConfigGroupDefinition;
import cn.escheduler.plugin.api.ConfigGroups;
import cn.escheduler.plugin.api.Label;
import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.Utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ConfigGroupExtractor {

    private static final ConfigGroupExtractor EXTRACTOR = new ConfigGroupExtractor() {};

    public static ConfigGroupExtractor get() {
        return EXTRACTOR;
    }

    public List<ErrorMessage> validate(Class klass, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        List<ConfigGroups> allConfigGroups = getAllConfigGroups(klass);
        Set<String> allGroupNames = new HashSet<>();
        if (!allConfigGroups.isEmpty()) {
            for (ConfigGroups configGroups : allConfigGroups) {
                Class<? extends Label> gKlass = configGroups.value();
                if (!gKlass.isEnum()) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_100, contextMsg, gKlass.getSimpleName()));
                } else {
                    for (Label label : gKlass.getEnumConstants()) {
                        String groupName = label.toString();
                        if (allGroupNames.contains(groupName)) {
                            errors.add(new ErrorMessage(DefinitionError.DEF_101, contextMsg, groupName));
                        }
                        allGroupNames.add(groupName);
                    }
                }
            }
        }
        return errors;
    }

    public ConfigGroupDefinition extract(Class klass, Object contextMsg) {
        List<ErrorMessage> errors = validate(klass, contextMsg);
        if (errors.isEmpty()) {
            List<ConfigGroups> allConfigGroups = getAllConfigGroups(klass);
            Set<String> allGroupNames = new HashSet<>();
            Map<String, List<String>> classNameToGroupsMap = new HashMap<>();
            List<Map<String, String>> groupNameToLabelMapList = new ArrayList<>();
            if (!allConfigGroups.isEmpty()) {
                for (ConfigGroups configGroups : allConfigGroups) {
                    Class<? extends Label> gKlass = configGroups.value();
                    List<String> groupNames = new ArrayList<>();
                    classNameToGroupsMap.put(gKlass.getName(), groupNames);
                    for (Label label : gKlass.getEnumConstants()) {
                        String groupName = label.toString();
                        Map<String, String> groupNameToLabelMap = new LinkedHashMap<>();
                        allGroupNames.add(groupName);
                        groupNames.add(groupName);
                        groupNameToLabelMap.put("name", groupName);
                        groupNameToLabelMap.put("label", label.getLabel());
                        groupNameToLabelMapList.add(groupNameToLabelMap);
                    }
                }
            }
            return new ConfigGroupDefinition(allGroupNames, classNameToGroupsMap, groupNameToLabelMapList);
        } else {
            throw new IllegalArgumentException(Utils.format("Invalid ConfigGroup definition: {}", errors));
        }
    }

    @SuppressWarnings("unchecked")
    private List<ConfigGroups> getAllConfigGroups(Class klass) {
        List<ConfigGroups> groups;
        if (klass == Object.class) {
            groups = new ArrayList<>();
        } else {
            groups = getAllConfigGroups(klass.getSuperclass());
            Annotation annotation = klass.getAnnotation(ConfigGroups.class);
            if (annotation != null) {
                groups.add((ConfigGroups)annotation);
            }
        }
        return groups;
    }
}
