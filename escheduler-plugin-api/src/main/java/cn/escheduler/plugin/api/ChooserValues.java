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

import java.util.List;

/**
 * <code>ChooserValues</code> provide a list of values and localizable labels for stage configurations using one of
 * the following models: {@link ValueChooserModel} or {@link MultiValueChooserModel}.
 * <p/>
 * The {@link cn.escheduler.plugin.api.base.BaseEnumChooserValues} implementation is a convenience implementation
 * that handles enums classes, or a subset of an enum class, as the possible values.
 *
 */
public interface ChooserValues {

    /**
     * Returns the name of the resource bundle to get the localized labels for the values.
     *
     * @return the name of the resource bundle to get the localized labels for the values.
     */
    public String getResourceBundle();

    /**
     * Returns the list of available values to choose from.
     *
     * @return The list of available values to choose from.
     */
    public List<String> getValues();

    /**
     * Returns the list of labels for the values.
     * <p/>
     * If the resource bundle is available, the labels will be localized using it.
     *
     * @return the list of labels for the values.
     */
    public List<String> getLabels();

}