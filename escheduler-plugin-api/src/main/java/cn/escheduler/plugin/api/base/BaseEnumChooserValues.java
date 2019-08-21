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
package cn.escheduler.plugin.api.base;

import cn.escheduler.plugin.api.ChooserValues;
import cn.escheduler.plugin.api.Label;
import cn.escheduler.plugin.api.impl.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base {@link ChooserValues} implementation for enums.
 */
public abstract class BaseEnumChooserValues<T extends Enum> implements ChooserValues {
    private String resourceBundle;
    private List<String> values;
    private List<String> labels;

    /**
     * Creates a <code>ChooserValues</code> with all the enum values of the given enum class.
     *
     * @param klass enum class.
     */
    @SuppressWarnings("unchecked")
    public BaseEnumChooserValues(Class<? extends Enum> klass) {
        this((T[])klass.getEnumConstants());
    }

    /**
     * Creates a <code>ChooserValues</code> with the specified enum values.
     *
     * @param enums enums for the <code>ChooserValues</code>.
     */
    @SuppressWarnings("unchecked")
    public BaseEnumChooserValues(T ... enums) {
        Utils.checkNotNull(enums, "enums");
        Utils.checkArgument(enums.length > 0, "array enum cannot have zero elements");
        resourceBundle = enums[0].getClass().getName() + "-bundle";
        boolean isEnumWithLabels = enums[0] instanceof Label;
        values = new ArrayList<>(enums.length);
        labels = new ArrayList<>(enums.length);
        for (T e : enums) {
            String value = e.name();
            values.add(value);
            String label = isEnumWithLabels ? ((Label)e).getLabel() : value;
            labels.add(label);
        }
        values = Collections.unmodifiableList(values);
        labels = Collections.unmodifiableList(labels);
    }

    @Override
    public String getResourceBundle() {
        return resourceBundle;
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public List<String> getLabels() {
        return labels;
    }
}
