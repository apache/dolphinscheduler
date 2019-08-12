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

import cn.escheduler.plugin.api.ChooserValues;
import cn.escheduler.plugin.api.ConfigDef;
import cn.escheduler.plugin.api.impl.LocalizableMessage;
import cn.escheduler.plugin.api.impl.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Captures attributes related to individual configuration options
 */
public class ConfigDefinition {
    private final Field configField;
    private final String name;
    private final ConfigDef.Type type;
    private final String label;
    private final String description;
    private Object defaultValue;
    private final boolean required;
    private final String group;
    private final String fieldName;
    private String dependsOn;
    private List<Object> triggeredByValues;
    private final ModelDefinition model;
    private final int displayPosition;
    private final long min;
    private final long max;
    private final String mode;
    private final int lines;
    private Map<String, List<Object>> dependsOnMap;
    private String prefix;

    public ConfigDefinition(String name, ConfigDef.Type type, String label, String description,
                            Object defaultValue,
                            boolean required, String group, String fieldName, ModelDefinition model, String dependsOn,
                            List<Object> triggeredByValues, int displayPosition,
                            long min, long max, String mode, int lines,
                            Map<String, List<Object>> dependsOnMap) {
        this(null, name, type, label, description, defaultValue, required, group, fieldName, model,
                dependsOn, triggeredByValues, displayPosition,
                min, max, mode, lines, dependsOnMap);
    }

    public ConfigDefinition(Field configField, String name, ConfigDef.Type type, String label, String description,
                            Object defaultValue,
                            boolean required, String group, String fieldName, ModelDefinition model, String dependsOn,
                            List<Object> triggeredByValues, int displayPosition,
                            long min, long max, String mode, int lines,
                            Map<String, List<Object>> dependsOnMap) {
        this.configField = configField;
        this.name = name;
        this.type = type;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
        this.group = group;
        this.fieldName = fieldName;
        this.model = model;
        this.dependsOn = dependsOn;
        this.triggeredByValues = triggeredByValues;
        this.displayPosition = displayPosition;

        this.min = min;
        this.max = max;
        this.mode = mode;
        this.lines = lines;
        this.dependsOnMap = dependsOnMap;
    }

    public Field getConfigField() {
        return configField;
    }

    public String getName() {
        return name;
    }

    public ConfigDef.Type getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public String getGroup() { return group; }

    public ModelDefinition getModel() {
        return model;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }
    public String getDependsOn() {
        return dependsOn;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public String getMode() {
        return mode;
    }

    public int getLines() {
        return lines;
    }

    public List<Object> getTriggeredByValues() {
        return triggeredByValues;
    }

    public void setTriggeredByValues(List<Object> triggeredByValues) {
        this.triggeredByValues = triggeredByValues;
    }

    public int getDisplayPosition() {
        return displayPosition;
    }

    public Map<String, List<Object>> getDependsOnMap() {
        return dependsOnMap;
    }

    public void setDependsOnMap(Map<String, List<Object>> dependsOnMap) {
        this.dependsOnMap = dependsOnMap;
    }

    public ConfigDefinition localize(ClassLoader classLoader, String bundle) {
        String labelKey = "configLabel." + getName();
        String descriptionKey = "configDescription." + getName();

        // config label & description
        String label = new LocalizableMessage(classLoader, bundle, labelKey, getLabel(), null).getLocalized();
        String description = new LocalizableMessage(classLoader, bundle, descriptionKey, getDescription(), null)
                .getLocalized();

        // config model
        ModelDefinition model = getModel();
        if(getType() == ConfigDef.Type.MODEL) {
            switch (model.getModelType()) {
                case VALUE_CHOOSER:
                case MULTI_VALUE_CHOOSER:
                    try {
                        Class klass = classLoader.loadClass(model.getValuesProviderClass());
                        ChooserValues chooserValues = (ChooserValues) klass.newInstance();
                        List<String> values = chooserValues.getValues();
                        if (values != null) {
                            List<String> localizedValueChooserLabels = new ArrayList<>(chooserValues.getLabels());
                            String rbName = chooserValues.getResourceBundle();
                            if (rbName != null) {
                                for (int i = 0; i < values.size(); i++) {
                                    String l = new LocalizableMessage(classLoader, rbName, values.get(i),
                                            localizedValueChooserLabels.get(i), null).getLocalized();
                                    localizedValueChooserLabels.set(i, l);
                                }
                            }
                            model = ModelDefinition.localizedValueChooser(model, values, localizedValueChooserLabels);
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(Utils.format("Could not extract localization info from '{}': {}",
                                model.getValuesProviderClass(), ex.toString()), ex);
                    }
                    break;
                case LIST_BEAN:
                    List<ConfigDefinition> listBean = model.getConfigDefinitions();
                    List<ConfigDefinition> listBeanLocalize = new ArrayList<>(listBean.size());
                    for (ConfigDefinition def : listBean) {
                        listBeanLocalize.add(def.localize(classLoader, bundle));
                    }
                    model = ModelDefinition.localizedComplexField(model, listBeanLocalize);
                    break;
                default:
                    break;
            }
        }

        return new ConfigDefinition(getConfigField(), getName(), getType(), label, description, getDefaultValue(),
                isRequired(), getGroup(), getFieldName(), model, getDependsOn(), getTriggeredByValues(),
                getDisplayPosition(), getMin(),
                getMax(), getMode(), getLines(), getDependsOnMap());
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    @Override
    public String toString() {
        return Utils.format("ConfigDefinition[name='{}' type='{}' required='{}' default='{}']", getName(), getType(),
                isRequired(), getDefaultValue());
    }

    /**
     * Only checks if the fields have the same name. Do not compare against various stages!!
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ConfigDefinition)) {
            return false;
        }
        return this.fieldName.equals(((ConfigDefinition) o).fieldName);
    }

}
