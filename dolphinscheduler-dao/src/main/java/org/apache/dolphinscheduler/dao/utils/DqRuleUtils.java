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

package org.apache.dolphinscheduler.dao.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;

import java.util.List;
import java.util.Map;

/**
 * DqRuleUtils
 */
public class DqRuleUtils {

    private DqRuleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<DqRuleInputEntry> transformInputEntry(List<DqRuleInputEntry> ruleInputEntryList) {
        for (DqRuleInputEntry dqRuleInputEntry : ruleInputEntryList) {
            Map<String, Object> valuesMap =
                    JSONUtils.toMap(dqRuleInputEntry.getValuesMap(), String.class, Object.class);
            if (valuesMap != null) {

                if (valuesMap.get(dqRuleInputEntry.getField()) != null) {
                    String value = String.valueOf(valuesMap.get(dqRuleInputEntry.getField()));
                    dqRuleInputEntry.setValue(value);
                }

                if (valuesMap.get("is_show") != null) {
                    dqRuleInputEntry.setIsShow(Boolean.parseBoolean(String.valueOf(valuesMap.get("is_show"))));
                }

                if (valuesMap.get("can_edit") != null) {
                    dqRuleInputEntry.setCanEdit(Boolean.parseBoolean(String.valueOf(valuesMap.get("can_edit"))));
                }
            }
        }

        return ruleInputEntryList;
    }
}
