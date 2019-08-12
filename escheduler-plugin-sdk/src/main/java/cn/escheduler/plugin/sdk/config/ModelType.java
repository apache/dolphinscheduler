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

import java.util.Collections;

public enum ModelType {
    FIELD_SELECTOR_MULTI_VALUE(new EmptyListDefaultPreparer()),
    FIELD_SELECTOR(new NopDefaultPreparer()),
    VALUE_CHOOSER(new NopDefaultPreparer()),
    MULTI_VALUE_CHOOSER(new NopDefaultPreparer()),
    PREDICATE(new EmptyMapDefaultPreparer()),
    LIST_BEAN(new EmptyListDefaultPreparer()),

    ;

    private final DefaultPreparer defaultPreparer;

    ModelType(DefaultPreparer defaultPreparer) {
        this.defaultPreparer = defaultPreparer;
    }

    public Object prepareDefault(Object defaultValue) {
        return defaultPreparer.prepare(defaultValue);
    }

    private interface DefaultPreparer {
        public Object prepare(Object defaultValue);
    }

    private static class NopDefaultPreparer implements DefaultPreparer {
        @Override
        public Object prepare(Object defaultValue) {
            return defaultValue;
        }
    }

    private static class EmptyListDefaultPreparer implements DefaultPreparer {
        @Override
        public Object prepare(Object defaultValue) {
            return Collections.emptyList();
        }
    }

    private static class EmptyMapDefaultPreparer implements DefaultPreparer {
        @Override
        public Object prepare(Object defaultValue) {
            return Collections.emptyMap();
        }
    }

}
