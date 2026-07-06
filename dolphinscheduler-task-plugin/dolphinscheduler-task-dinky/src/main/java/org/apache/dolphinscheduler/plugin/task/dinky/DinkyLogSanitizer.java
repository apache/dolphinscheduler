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

package org.apache.dolphinscheduler.plugin.task.dinky;

import org.apache.dolphinscheduler.plugin.task.api.log.SensitiveDataConverter;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class DinkyLogSanitizer {

    private DinkyLogSanitizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    static String summarizeParameters(final DinkyParameters parameters) {
        if (parameters == null) {
            return "null";
        }
        return "address=" + sanitizeMessage(parameters.getAddress())
                + ", taskId=" + sanitizeMessage(parameters.getTaskId())
                + ", online=" + parameters.isOnline()
                + ", localParamKeys=" + summarizeLocalParamKeys(parameters.getLocalParams());
    }

    static String summarizeVariables(final Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return "size=0, keys=[]";
        }
        Set<String> keys = new TreeSet<>(variables.keySet());
        return "size=" + variables.size() + ", keys=" + keys;
    }

    static String sanitizeMessage(final Object message) {
        if (message == null) {
            return null;
        }
        return SensitiveDataConverter.maskSensitiveData(String.valueOf(message));
    }

    private static Set<String> summarizeLocalParamKeys(final List<Property> localParams) {
        Set<String> keys = new TreeSet<>();
        if (localParams == null) {
            return keys;
        }
        for (Property property : localParams) {
            if (property != null && property.getProp() != null) {
                keys.add(property.getProp());
            }
        }
        return keys;
    }
}
