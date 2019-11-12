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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Utility class for working with Strings that have placeholder values in them. A placeholder takes the form
 * {@code ${name}}. Using {@code PropertyPlaceholderHelper} these placeholders can be substituted for
 * user-supplied values. <p> Values for substitution can be supplied using a {@link Properties} instance or
 * using a {@link PlaceholderResolver}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 3.0
 */
public class PropertyPlaceholderHelper {

    private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<String, String>(4);

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }


    private final String placeholderPrefix;

    private final String placeholderSuffix;

    private final String simplePrefix;

    private final String valueSeparator;

    private final boolean ignoreUnresolvablePlaceholders;


    /**
     * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
     * Unresolvable placeholders are ignored.
     * @param placeholderPrefix the prefix that denotes the start of a placeholder
     * @param placeholderSuffix the suffix that denotes the end of a placeholder
     */
    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix) {
        this(placeholderPrefix, placeholderSuffix, null, true);
    }

    /**
     * Creates a new {@code PropertyPlaceholderHelper} that uses the supplied prefix and suffix.
     * @param placeholderPrefix the prefix that denotes the start of a placeholder
     * @param placeholderSuffix the suffix that denotes the end of a placeholder
     * @param valueSeparator the separating character between the placeholder variable
     * and the associated default value, if any
     * @param ignoreUnresolvablePlaceholders indicates whether unresolvable placeholders should
     * be ignored ({@code true}) or cause an exception ({@code false})
     */
    public PropertyPlaceholderHelper(String placeholderPrefix, String placeholderSuffix,
                                     String valueSeparator, boolean ignoreUnresolvablePlaceholders) {

        notNull(placeholderPrefix, "'placeholderPrefix' must not be null");
        notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        }
        else {
            this.simplePrefix = this.placeholderPrefix;
        }
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }


    /**
     * Replaces all placeholders of format {@code ${name}} with the corresponding
     * property from the supplied {@link Properties}.
     * @param value the value containing the placeholders to be replaced
     * @param properties the {@code Properties} to use for replacement
     * @return the supplied value with placeholders replaced inline
     */
    public String replacePlaceholders(String value, final Properties properties) {
        notNull(properties, "'properties' must not be null");
        return replacePlaceholders(value, new PlaceholderResolver() {
            @Override
            public String resolvePlaceholder(String placeholderName) {
                return properties.getProperty(placeholderName);
            }
        });
    }

    /**
     * Replaces all placeholders of format {@code ${name}} with the value returned
     * from the supplied {@link PlaceholderResolver}.
     * @param value the value containing the placeholders to be replaced
     * @param placeholderResolver the {@code PlaceholderResolver} to use for replacement
     * @return the supplied value with placeholders replaced inline
     */
    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
        notNull(value, "'value' must not be null");
        return parseStringValue(value, placeholderResolver, new HashSet<String>());
    }

    protected String parseStringValue(
            String value, PlaceholderResolver placeholderResolver, Set<String> visitedPlaceholders) {

        StringBuilder result = new StringBuilder(value);

        int startIndex = value.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex != -1) {
                String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                            "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
                }
                // Recursive invocation, parsing placeholders contained in the placeholder key.
                placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholders);
                // Now obtain the value for the fully resolved key...
                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if (propVal == null && this.valueSeparator != null) {
                    int separatorIndex = placeholder.indexOf(this.valueSeparator);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0, separatorIndex);
                        String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }
                if (propVal != null) {
                    // Recursive invocation, parsing placeholders contained in the
                    // previously resolved placeholder value.
                    propVal = parseStringValue(propVal, placeholderResolver, visitedPlaceholders);
                    result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Resolved placeholder '" + placeholder + "'");
                    }
                    startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
                }
                else if (this.ignoreUnresolvablePlaceholders) {
                    // Proceed with unprocessed value.
                    startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
                }
                else {
                    throw new IllegalArgumentException("Could not resolve placeholder '" +
                            placeholder + "'" + " in value \"" + value + "\"");
                }
                visitedPlaceholders.remove(originalPlaceholder);
            }
            else {
                startIndex = -1;
            }
        }

        return result.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                }
                else {
                    return index;
                }
            }
            else if (substringMatch(buf, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            }
            else {
                index++;
            }
        }
        return -1;
    }


    /**
     * Strategy interface used to resolve replacement values for placeholders contained in Strings.
     */
    public interface PlaceholderResolver {

        /**
         * Resolve the supplied placeholder name to the replacement value.
         * @param placeholderName the name of the placeholder to resolve
         * @return the replacement value, or {@code null} if no replacement is to be made
         */
        String resolvePlaceholder(String placeholderName);
    }

    /**
     * Test whether the given string matches the given substring
     * at the given index.
     * @param str the original string (or StringBuilder)
     * @param index the index in the original string to start matching against
     * @param substring the substring to match at the given index
     * @return whether the given string matches the given substring
     */
    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        for (int j = 0; j < substring.length(); j++) {
            int i = index + j;
            if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assert that an object is not {@code null}.
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }


}

