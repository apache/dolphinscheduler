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

import cn.escheduler.plugin.api.impl.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelDefinition {
    private final ModelType modelType;
    private final String valuesProviderClass;
    private final List<ConfigDefinition> configDefinitions;
    private final Map<String, ConfigDefinition> configDefinitionsAsMap;
    private List<String> values;
    private List<String> labels;
    private final Class listBeanClass;
    private final String filteringConfig;

    public static ModelDefinition localizedValueChooser(
            ModelDefinition model,
            List<String> values,
            List<String> labels
    ) {
        return new ModelDefinition(
                model.getModelType(),
                model.getValuesProviderClass(),
                values,
                labels,
                model.getListBeanClass(),
                model.getConfigDefinitions(),
                model.getFilteringConfig()
        );
    }

    public static ModelDefinition localizedComplexField(
            ModelDefinition model,
            List<ConfigDefinition> configDefs
    ) {
        return new ModelDefinition(
                model.getModelType(),
                model.getValuesProviderClass(),
                model.getValues(),
                model.getLabels(),
                model.getListBeanClass(),
                configDefs,
                model.getFilteringConfig()
        );
    }

    public ModelDefinition(
            ModelType modelType,
            String valuesProviderClass,
            List<String> values,
            List<String> labels,
            Class listBeanClass,
            List<ConfigDefinition> configDefinitions,
            String filteringConfig
    ) {
        this.modelType = modelType;
        this.valuesProviderClass = valuesProviderClass;
        this.configDefinitions = configDefinitions;
        configDefinitionsAsMap = new HashMap<>();
        if (configDefinitions != null) {
            for (ConfigDefinition def : configDefinitions) {
                configDefinitionsAsMap.put(def.getName(), def);
            }
        }
        this.values = values;
        this.labels = labels;
        this.listBeanClass = listBeanClass;
        this.filteringConfig = filteringConfig;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public List<String> getValues() {
        return values;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getValuesProviderClass() {
        return valuesProviderClass;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Class getListBeanClass() {
        return listBeanClass;
    }

    public List<ConfigDefinition> getConfigDefinitions() {
        return configDefinitions;
    }

    public Map<String, ConfigDefinition> getConfigDefinitionsAsMap() {
        return configDefinitionsAsMap;
    }

    public String getFilteringConfig() {
        return filteringConfig;
    }

    @Override
    public String toString() {
        return Utils.format(
                "ModelDefinition[type='{}' valuesProviderClass='{}' values='{}' filteringConfig='{}']",
                getModelType(),
                getValuesProviderClass(),
                getValues(),
                getFilteringConfig()
        );
    }

}
