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
package cn.escheduler.plugin.sdk.config;


import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigGroupDefinition {
    private final Set<String> groupNames;
    private final Map<String, List<String>> classNameToGroupsMap;
    private final List<Map<String, String>> groupNameToLabelMapList;

    public ConfigGroupDefinition(Set<String> groupsNames, Map<String, List<String>> classNameToGroupsMap,
                                 List<Map<String, String>> groupNameToLabelMap) {
        this.groupNames = groupsNames;
        this.classNameToGroupsMap = classNameToGroupsMap;
        this.groupNameToLabelMapList = groupNameToLabelMap;
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }

    public Map<String, List<String>> getClassNameToGroupsMap() {
        return classNameToGroupsMap;
    }

    public List<Map<String, String>> getGroupNameToLabelMapList() {
        return groupNameToLabelMapList;
    }

}
