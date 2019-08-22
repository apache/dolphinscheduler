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
package cn.escheduler.plugin.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalizableMessage implements LocalizableString {
    private static final Logger LOG = LoggerFactory.getLogger(LocalizableMessage.class);
    private static final Object[] NULL_ONE_ARG = {null};

    private static final Set<String> MISSING_BUNDLE_WARNS =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Set<String> MISSING_KEY_WARNS =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private final ClassLoader classLoader;
    private final String bundle;
    private final String id;
    private final String template;
    private final Object[] args;

    public LocalizableMessage(ClassLoader classLoader, String bundle, String id, String defaultTemplate, Object[] args) {
        this.classLoader = Utils.checkNotNull(classLoader, "classLoader");
        this.bundle = bundle;
        this.id = id;
        this.template = defaultTemplate;
        // we need to do this trick because of the ... syntax sugar resolution when a single value 'null' is used
        this.args = (args != null) ? args : NULL_ONE_ARG;
    }

    public LocalizableMessage(String bundle, String id, String defaultTemplate, Object[] args) {
        this(Thread.currentThread().getContextClassLoader(), bundle, id, defaultTemplate, args);
    }

    @Override
    public String getNonLocalized() {
        return Utils.format(template, args);
    }

    @Override
    public String getLocalized() {
        String templateToUse = template;
        if (bundle != null) {
            Locale locale = LocaleInContext.get();
            if (locale != null) {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle(bundle, locale, classLoader);
                    if (rb.containsKey(id)) {
                        // for Java 8, the property file is assumed to save as ISO-8859-1, but we want to support UTF-8 as well
                        // so work around by the following convert
                        templateToUse = new String(rb.getString(id).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    } else if (!MISSING_KEY_WARNS.contains(bundle + " " + id)) {
                        MISSING_KEY_WARNS.add(bundle + " " + id);
                        LOG.warn("ResourceBundle '{}' does not have key '{}' via ClassLoader '{}'", bundle, id, classLoader);
                    }
                } catch (MissingResourceException ex) {
                    if (!MISSING_BUNDLE_WARNS.contains(bundle)) {
                        MISSING_BUNDLE_WARNS.add(bundle);
                        LOG.debug("ResourceBundle '{}' not found via ClassLoader '{}'", bundle, classLoader);
                    }
                }
            }
        }
        return Utils.format(templateToUse, args);
    }

    @Override
    public String toString() {
        return getNonLocalized();
    }

}
