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
package cn.escheduler.plugin.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for configuration variables that can hold multiple values from a given list of values.
 * <p/>
 * The configuration definition must be of type {@link cn.escheduler.plugin.api.ConfigDef.Type#MODEL}.
 * <p/>
 * The configuration variable must be of type <code>java.util.List&lt;String></code> or
 * <code>java.util.List&lt;Enum></code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.FIELD)
public @interface MultiValueChooserModel {

    /**
     * Indicates the <code>ChooserValues</code> class providing the valid values.
     */
    Class<? extends ChooserValues> value();

}
