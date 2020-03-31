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
package org.apache.dolphinscheduler.common.utils.placeholder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * placeholder utils
 */
public class PlaceholderUtils {

    private static final Logger logger = LoggerFactory.getLogger(PlaceholderUtils.class);

    /**
     * Prefix of the position to be replaced
     */
    public static final String PLACEHOLDER_PREFIX = "${";

    /**
     * The suffix of the position to be replaced
     */

    public static final String PLACEHOLDER_SUFFIX = "}";


    /**
     * Replaces all placeholders of format {@code ${name}} with the value returned
     * from the supplied {@link PropertyPlaceholderHelper.PlaceholderResolver}.
     *
     * @param value the value containing the placeholders to be replaced
     * @param paramsMap placeholder data dictionary
     * @param ignoreUnresolvablePlaceholders ignoreUnresolvablePlaceholders
     * @return the supplied value with placeholders replaced inline
     */
    public static String replacePlaceholders(String value,
                                             Map<String, String> paramsMap,
                                             boolean ignoreUnresolvablePlaceholders) {
        //replacement tool， parameter key will be replaced by value,if can't match , will throw an exception
        PropertyPlaceholderHelper strictHelper = getPropertyPlaceholderHelper(false);

        //Non-strict replacement tool implementation, when the position to be replaced does not get the corresponding value, the current position is ignored, and the next position is replaced.
        PropertyPlaceholderHelper nonStrictHelper = getPropertyPlaceholderHelper(true);

        PropertyPlaceholderHelper helper = (ignoreUnresolvablePlaceholders ? nonStrictHelper : strictHelper);

        //the PlaceholderResolver to use for replacement
        return helper.replacePlaceholders(value, new PropertyPlaceholderResolver(value, paramsMap));
    }

    /**
     * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
     * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
     * be ignored ({@code true}) or cause an exception ({@code false})
     * @return PropertyPlaceholderHelper
     */
    public static PropertyPlaceholderHelper getPropertyPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {

        return new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, null, ignoreUnresolvablePlaceholders);
    }

    /**
     * Placeholder replacement resolver
     */
    private static class PropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final String value;

        private final Map<String, String> paramsMap;

        public PropertyPlaceholderResolver(String value, Map<String, String> paramsMap) {
            this.value = value;
            this.paramsMap = paramsMap;
        }

        @Override
        public String resolvePlaceholder(String placeholderName) {
            try {
                return paramsMap.get(placeholderName);
            } catch (Exception ex) {
                logger.error("resolve placeholder '{}' in [ {} ]" , placeholderName, value, ex);
                return null;
            }
        }
    }


}
