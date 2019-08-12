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

import cn.escheduler.plugin.api.Label;
import cn.escheduler.plugin.api.Stage;
import cn.escheduler.plugin.api.StageDef;
import cn.escheduler.plugin.api.StageType;
import cn.escheduler.plugin.api.StageUpgrader;
import cn.escheduler.plugin.api.impl.LocalizableMessage;
import cn.escheduler.plugin.api.impl.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the configuration options for a {@link cn.escheduler.plugin.api.Stage}.
 *
 */
public class StageDefinition implements PrivateClassLoaderDefinition {
    private final StageLibraryDefinition libraryDefinition;
    private final ClassLoader classLoader;
    private final Class<? extends Stage> klass;
    private final String name;
    private final int version;
    private final String label;
    private final String description;
    private final StageType type;
    private final List<ConfigDefinition> configDefinitions;
    private final Map<String, ConfigDefinition> configDefinitionsMap;
    private final String icon;
    private final ConfigGroupDefinition configGroupDefinition;
    private final StageUpgrader upgrader;
    private final String onlineHelpRefUrl;
    private final StageDef stageDef;
    private final boolean beta;

    // localized version
    private StageDefinition(
            StageDef stageDef,
            StageLibraryDefinition libraryDefinition,
            ClassLoader classLoader,
            Class<? extends Stage> klass,
            String name,
            int version,
            String label,
            String description,
            StageType type,
            List<ConfigDefinition> configDefinitions,
            String icon,
            ConfigGroupDefinition configGroupDefinition,
            StageUpgrader upgrader,
            String onlineHelpRefUrl,
            boolean beta
    ) {
        this.stageDef = stageDef;
        this.libraryDefinition = libraryDefinition;
        this.classLoader = classLoader;
        this.klass = klass;
        this.name = name;
        this.version = version;
        this.label = label;
        this.description = description;
        this.type = type;
        this.configDefinitions = configDefinitions;
        this.onlineHelpRefUrl = onlineHelpRefUrl;
        configDefinitionsMap = new HashMap<>();
        for (ConfigDefinition conf : configDefinitions) {
            configDefinitionsMap.put(conf.getName(), conf);
            ModelDefinition modelDefinition = conf.getModel();
            if(modelDefinition != null && modelDefinition.getConfigDefinitions() != null) {
                //Multi level complex is not allowed. So we stop at this level
                //Assumption is that the config property names are unique in the class hierarchy
                //and across complex types
                for (ConfigDefinition configDefinition : modelDefinition.getConfigDefinitions()) {
                    configDefinitionsMap.put(configDefinition.getName(), configDefinition);
                }
            }
        }
        this.icon = icon;
        this.configGroupDefinition = configGroupDefinition;
        this.upgrader = upgrader;
        this.beta = beta;
    }

    @SuppressWarnings("unchecked")
    public StageDefinition(StageDefinition def, ClassLoader classLoader) {
        stageDef = def.stageDef;
        libraryDefinition = def.libraryDefinition;
        this.classLoader = classLoader;
        try {

            klass = (Class<? extends Stage>) classLoader.loadClass(def.getClassName());
        } catch (Exception ex) {
            throw new Error(ex);
        }
        name = def.name;
        version = def.version;
        label = def.label;
        description = def.description;
        type = def.type;
        configDefinitions = def.configDefinitions;
        configDefinitionsMap = def.configDefinitionsMap;
        icon = def.icon;
        configGroupDefinition = def.configGroupDefinition;
        upgrader = def.upgrader;
        onlineHelpRefUrl = def.onlineHelpRefUrl;
        beta = def.beta;
    }

    public StageDefinition(
            StageDef stageDef,
            StageLibraryDefinition libraryDefinition,
            Class<? extends Stage> klass,
            String name,
            int version,
            String label,
            String description,
            StageType type,
            List<ConfigDefinition> configDefinitions,
            String icon,
            ConfigGroupDefinition configGroupDefinition,
            StageUpgrader upgrader,
            String onlineHelpRefUrl,
            boolean beta
    ) {
        this.stageDef = stageDef;
        this.libraryDefinition = libraryDefinition;
        this.onlineHelpRefUrl = onlineHelpRefUrl;
        this.classLoader = libraryDefinition.getClassLoader();
        this.klass = klass;
        this.name = name;
        this.version = version;
        this.label = label;
        this.description = description;
        this.type = type;
        this.configDefinitions = configDefinitions;
        configDefinitionsMap = new HashMap<>();
        for (ConfigDefinition conf : configDefinitions) {
            configDefinitionsMap.put(conf.getName(), conf);
            ModelDefinition modelDefinition = conf.getModel();
            if(modelDefinition != null && modelDefinition.getConfigDefinitions() != null) {
                //Multi level complex is not allowed. So we stop at this level
                //Assumption is that the config property names are unique in the class hierarchy
                //and across complex types
                for (ConfigDefinition configDefinition : modelDefinition.getConfigDefinitions()) {
                    configDefinitionsMap.put(configDefinition.getName(), configDefinition);
                }
            }
        }
        this.icon = icon;
        this.configGroupDefinition = configGroupDefinition;
        this.upgrader = upgrader;
        this.beta = beta;
    }

    public ConfigGroupDefinition getConfigGroupDefinition() {
        return configGroupDefinition;
    }

    public String getLibrary() {
        return libraryDefinition.getName();
    }

    public String getLibraryLabel() {
        return libraryDefinition.getLabel();
    }

    @Override
    public ClassLoader getStageClassLoader() {
        return classLoader;
    }

    public String getClassName() {
        return klass.getName();
    }

    public Class<? extends Stage> getStageClass() {
        return klass;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public StageType getType() {
        return type;
    }

    public void addConfiguration(ConfigDefinition confDef) {
        if (configDefinitionsMap.containsKey(confDef.getName())) {
            throw new IllegalArgumentException(Utils.format("Stage '{}:{}:{}', configuration definition '{}' already exists",
                    getLibrary(), getName(), getVersion(), confDef.getName()));
        }
        configDefinitionsMap.put(confDef.getName(), confDef);
        configDefinitions.add(confDef);
    }

    public List<ConfigDefinition> getConfigDefinitions() {
        return configDefinitions;
    }

    public ConfigDefinition getConfigDefinition(String configName) {
        return configDefinitionsMap.get(configName);
    }

    // This method returns not only main configs, but also all complex ones!
    public Map<String, ConfigDefinition> getConfigDefinitionsMap() {
        return configDefinitionsMap;
    }

    @Override
    public String toString() {
        return Utils.format("StageDefinition[library='{}' name='{}' version='{}' type='{}' class='{}']", getLibrary(),
                getName(), getVersion(), getType(), getStageClass());
    }

    public String getIcon() {
        return icon;
    }

    public StageUpgrader getUpgrader() {
        return upgrader;
    }

    private final static String STAGE_LABEL = "stageLabel";
    private final static String STAGE_DESCRIPTION = "stageDescription";

    private static Map<String, String> getGroupToResourceBundle(ConfigGroupDefinition configGroupDefinition) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, List<String>> entry: configGroupDefinition.getClassNameToGroupsMap().entrySet()) {
            for (String group : entry.getValue()) {
                map.put(group, entry.getKey() + "-bundle");
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static ConfigGroupDefinition localizeConfigGroupDefinition(ClassLoader classLoader,
                                                                      ConfigGroupDefinition groupDefs) {
        if (groupDefs != null) {
            Map<String, List<String>> classNameToGroupsMap = groupDefs.getClassNameToGroupsMap();
            Map<String, String> groupToDefaultLabelMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : classNameToGroupsMap.entrySet()) {
                Class groupClass;
                try {
                    groupClass = classLoader.loadClass(entry.getKey());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                boolean isLabel = Label.class.isAssignableFrom(groupClass);
                for (String group : entry.getValue()) {
                    Enum e = Enum.valueOf(groupClass, group);
                    String groupLabel = (isLabel) ? ((Label)e).getLabel() : e.name();
                    groupToDefaultLabelMap.put(group, groupLabel);
                }
            }
            Map<String, String> groupBundles = getGroupToResourceBundle(groupDefs);
            List<Map<String, String>> localizedGroups = new ArrayList<>();
            for (Map<String, String> group : groupDefs.getGroupNameToLabelMapList()) {
                String groupName = group.get("name");
                Map<String, String> localizeGroup = new HashMap<>();
                localizeGroup.put("name", groupName);
                localizeGroup.put("label", new LocalizableMessage(classLoader, groupBundles.get(groupName), groupName,
                        groupToDefaultLabelMap.get(groupName), null).getLocalized());
                localizedGroups.add(localizeGroup);
            }
            groupDefs = new ConfigGroupDefinition(groupDefs.getGroupNames(), groupDefs.getClassNameToGroupsMap(),
                    localizedGroups);
        }
        return groupDefs;
    }

    public StageDefinition localize() {
        ClassLoader classLoader = libraryDefinition.getClassLoader();
        String rbName = getClassName() + "-bundle";

        // stage label & description
        String label = new LocalizableMessage(classLoader, rbName, STAGE_LABEL, getLabel(), null).getLocalized();
        String description = new LocalizableMessage(classLoader, rbName, STAGE_DESCRIPTION, getDescription(), null)
                .getLocalized();

        // stage configs
        List<ConfigDefinition> configDefs = new ArrayList<>();
        for (ConfigDefinition configDef : getConfigDefinitions()) {
            configDefs.add(configDef.localize(classLoader, rbName));
        }

        // stage groups
        ConfigGroupDefinition groupDefs = localizeConfigGroupDefinition(classLoader, getConfigGroupDefinition());

        return new StageDefinition(
                stageDef,
                libraryDefinition,
                getStageClassLoader(),
                getStageClass(),
                getName(),
                getVersion(),
                label,
                description,
                getType(),
                configDefs,
                getIcon(),
                groupDefs,
                upgrader,
                onlineHelpRefUrl,
                beta
        );
    }

    public String getOnlineHelpRefUrl() {
        return onlineHelpRefUrl;
    }

    public StageDef getStageDef() {
        return stageDef;
    }

    public boolean isBeta() {
        return beta;
    }
}
