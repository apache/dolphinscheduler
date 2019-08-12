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
package cn.escheduler.plugin.sdk.definition;

import cn.escheduler.plugin.api.*;
import cn.escheduler.plugin.sdk.config.ConfigDefinition;
import cn.escheduler.plugin.sdk.config.ConfigGroupDefinition;
import cn.escheduler.plugin.sdk.config.StageDefinition;
import cn.escheduler.plugin.sdk.config.StageLibraryDefinition;
import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.Utils;
import org.apache.commons.lang3.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class StageDefinitionExtractor {

    private static final StageDefinitionExtractor EXTRACTOR = new StageDefinitionExtractor() {};

    public static StageDefinitionExtractor get() {
        return EXTRACTOR;
    }

    static String getStageName(Class klass) {
        return klass.getName().replace(".", "_").replace("$", "_");
    }

    public static List<String> getGroups(Class klass) {
        Set<String> set = new LinkedHashSet<>();
        addGroupsToList(klass, set);
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(klass);
        for(Class<?> superClass : allSuperclasses) {
            if(!superClass.isInterface() && superClass.isAnnotationPresent(ConfigGroups.class)) {
                addGroupsToList(superClass, set);
            }
        }
        if(set.isEmpty()) {
            set.add(""); // the default empty group
        }

        return new ArrayList<>(set);
    }

    @SuppressWarnings("unchecked")
    private static void addGroupsToList(Class<?> klass, Set<String> set) {
        ConfigGroups groups = klass.getAnnotation(ConfigGroups.class);
        if (groups != null) {
            Class<? extends Enum> groupKlass = (Class<? extends Enum>) groups.value();
            for (Enum e : groupKlass.getEnumConstants()) {
                set.add(e.name());
            }
        }
    }

    public List<ErrorMessage> validate(StageLibraryDefinition libraryDef, Class<? extends Stage> klass, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        contextMsg = Utils.formatL("{} Stage='{}'", contextMsg, klass.getSimpleName());

        StageDef sDef = klass.getAnnotation(StageDef.class);
        if (sDef == null) {
            errors.add(new ErrorMessage(DefinitionError.DEF_300, contextMsg));
        } else {
            if (!sDef.icon().isEmpty()) {
                if (klass.getClassLoader().getResource(sDef.icon()) == null) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_311, contextMsg, sDef.icon()));
                }
            }
            StageType type = extractStageType(klass);
            if (type == null) {
                errors.add(new ErrorMessage(DefinitionError.DEF_302, contextMsg));
            }

            List<String> stageGroups = getGroups(klass);

            List<ErrorMessage> configGroupErrors = ConfigGroupExtractor.get().validate(klass, contextMsg);
            errors.addAll(configGroupErrors);
            errors.addAll(ConfigGroupExtractor.get().validate(klass, contextMsg));

            List<ErrorMessage> configErrors = ConfigDefinitionExtractor.get().validate(klass, stageGroups, contextMsg);
            errors.addAll(configErrors);


            if (configErrors.isEmpty() && configGroupErrors.isEmpty()) {
                List<ConfigDefinition> configDefs = extractConfigDefinitions(libraryDef, klass, errors, contextMsg);
                ConfigGroupDefinition configGroupDef = ConfigGroupExtractor.get().extract(klass, contextMsg);
                errors.addAll(validateConfigGroups(configDefs, configGroupDef, contextMsg));
            }


        }
        return errors;
    }

    public StageDefinition extract(StageLibraryDefinition libraryDef, Class<? extends Stage> klass, Object contextMsg) {
        List<ErrorMessage> errors = validate(libraryDef, klass, contextMsg);
        if (errors.isEmpty()) {
            try {
                contextMsg = Utils.formatL("{} Stage='{}'", contextMsg, klass.getSimpleName());

                StageDef sDef = klass.getAnnotation(StageDef.class);
                String name = getStageName(klass);
                int version = sDef.version();
                String label = sDef.label();
                String description = sDef.description();
                String icon = sDef.icon();
                StageType type = extractStageType(klass);
                List<ConfigDefinition> configDefinitions = extractConfigDefinitions(libraryDef, klass, new ArrayList<ErrorMessage>(), contextMsg);
                ConfigGroupDefinition configGroupDefinition = ConfigGroupExtractor.get().extract(klass, contextMsg);
                StageUpgrader upgrader;
                try {
                    upgrader = sDef.upgrader().newInstance();
                } catch (Exception ex) {
                    throw new IllegalArgumentException(Utils.format(
                            "Could not instantiate StageUpgrader for StageDefinition '{}': {}", name, ex.toString(), ex));
                }

                String onlineHelpRefUrl = sDef.onlineHelpRefUrl();

                return new StageDefinition(
                        sDef,
                        libraryDef,
                        klass,
                        name,
                        version,
                        label,
                        description,
                        type,
                        configDefinitions,
                        icon,
                        configGroupDefinition,
                        upgrader,
                        onlineHelpRefUrl,
                        sDef.beta()
                );
            } catch (Exception e) {
                throw new IllegalStateException("Exception while extracting stage definition for " + getStageName(klass), e);
            }

        } else {
            throw new IllegalArgumentException(Utils.format("Invalid StageDefinition: {}", errors));
        }
    }

    private List<ConfigDefinition> extractConfigDefinitions(StageLibraryDefinition libraryDef,
                                                            Class<? extends Stage> klass, List<ErrorMessage> errors, Object contextMsg) {

        List<String> stageGroups = getGroups(klass);

        List<ConfigDefinition> cDefs = ConfigDefinitionExtractor.get().extract(klass, stageGroups, contextMsg);

        return cDefs;
    }

    private StageType extractStageType(Class<? extends Stage> klass) {
        StageType type;
        if (Command.class.isAssignableFrom(klass)) {
            type = StageType.COMMAND;
        } else {
            type = null;
        }
        return type;
    }

    private List<ErrorMessage> validateConfigGroups(List<ConfigDefinition> configs, ConfigGroupDefinition
            groups, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        for (ConfigDefinition config : configs) {
            if (!config.getGroup().isEmpty()) {
                if (!groups.getGroupNames().contains(config.getGroup())) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_310, contextMsg, config.getName(), config.getGroup()));
                }
            }
        }
        return errors;
    }

}
