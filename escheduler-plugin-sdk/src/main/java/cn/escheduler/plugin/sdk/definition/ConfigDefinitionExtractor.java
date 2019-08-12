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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import cn.escheduler.plugin.sdk.config.ConfigDefinition;
import cn.escheduler.plugin.sdk.config.ModelDefinition;
import cn.escheduler.plugin.api.Dependency;
import cn.escheduler.plugin.api.ListBeanModel;
import cn.escheduler.plugin.api.ConfigDef;
import cn.escheduler.plugin.api.ConfigDefBean;
import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.Utils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ConfigDefinitionExtractor {

    private static final ConfigDefinitionExtractor EXTRACTOR = new ConfigDefinitionExtractor() {};

    private Set<String> cycles = new HashSet<>();

    public static ConfigDefinitionExtractor get() {
        return EXTRACTOR;
    }

    public List<ErrorMessage> validate(Class klass, List<String> stageGroups, Object contextMsg) {
        return validate("", klass, stageGroups, true, false, false, contextMsg);
    }

    public List<ErrorMessage> validateComplexField(String configPrefix, Class klass, List<String> stageGroups,
                                                   Object contextMsg) {
        return validate(configPrefix, klass, stageGroups, true, false, true, contextMsg);
    }

    @VisibleForTesting
    Set<String> getCycles() {
        return cycles;
    }

    private List<ErrorMessage> validate(String configPrefix, Class klass, List<String> stageGroups,
                                        boolean validateDependencies, boolean isBean, boolean isComplexField, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        boolean noConfigs = true;
        for (Field field : klass.getFields()) {
            if (field.getAnnotation(ConfigDef.class) != null && field.getAnnotation(ConfigDefBean.class) != null) {
                errors.add(new ErrorMessage(DefinitionError.DEF_152, contextMsg, field.getName()));
            } else {
                if (field.getAnnotation(ConfigDef.class) != null || field.getAnnotation(ConfigDefBean.class) != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_151, contextMsg, klass.getSimpleName(), field.getName()));
                    }
                    if (Modifier.isFinal(field.getModifiers())) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_154, contextMsg, klass.getSimpleName(), field.getName()));
                    }
                }
                if (field.getAnnotation(ConfigDef.class) != null) {
                    noConfigs = false;
                    List<ErrorMessage> subErrors = validateConfigDef(configPrefix, stageGroups, field, isComplexField,
                            Utils.formatL("{} Field='{}'", contextMsg,
                                    field.getName()));
                    errors.addAll(subErrors);
                } else if (field.getAnnotation(ConfigDefBean.class) != null) {
                    noConfigs = false;
                    List<ErrorMessage> subErrors = validateConfigDefBean(configPrefix + field.getName() + ".", field,
                            stageGroups, isComplexField, Utils.formatL("{} BeanField='{}'", contextMsg, field.getName()));
                    errors.addAll(subErrors);
                }
            }
        }
        if (isBean && noConfigs) {
            errors.add(new ErrorMessage(DefinitionError.DEF_160, contextMsg));
        }
        if (errors.isEmpty() & validateDependencies) {
            errors.addAll(validateDependencies(getConfigDefinitions(configPrefix, klass, stageGroups, contextMsg),
                    contextMsg));
        }
        return errors;
    }

    private static String resolveGroup(List<String> parentGroups, String group, Object contextMsg, List<ErrorMessage> errors) {
        if (group.startsWith("#")) {
            try {
                int pos = Integer.parseInt(group.substring(1).trim());
                if (pos >= 0 && pos < parentGroups.size()) {
                    group = parentGroups.get(pos);
                } else {
                    errors.add(new ErrorMessage(DefinitionError.DEF_163, contextMsg, pos, parentGroups.size() - 1));
                }
            } catch (NumberFormatException ex) {
                errors.add(new ErrorMessage(DefinitionError.DEF_164, contextMsg, ex.toString()));
            }
        } else {
            if (!parentGroups.contains(group)) {
                errors.add(new ErrorMessage(DefinitionError.DEF_165, contextMsg, group, parentGroups));
            }
        }
        return group;
    }

    public static List<String> getGroups(Field field, List<String> parentGroups, Object contextMsg,
                                         List<ErrorMessage> errors) {
        List<String> list = new ArrayList<>();
        ConfigDefBean configDefBean = field.getAnnotation(ConfigDefBean.class);
        if (configDefBean != null) {
            String[] groups = configDefBean.groups();
            if (groups.length > 0) {
                for (String group : groups) {
                    list.add(resolveGroup(parentGroups, group, contextMsg, errors));
                }
            } else {
                // no groups in the annotation, we propagate all parent groups then
                list.addAll(parentGroups);
            }
        } else {
            throw new IllegalArgumentException(Utils.format("{} is not annotated with ConfigDefBean", contextMsg));
        }
        return list;
    }

    private List<ConfigDefinition> getConfigDefinitions(String configPrefix, Class klass, List<String> stageGroups,
                                                        Object contextMsg) {
        List<ConfigDefinition> defs = new ArrayList<>();
        for (Field field : klass.getFields()) {
            if (field.getAnnotation(ConfigDef.class) != null) {
                defs.add(extractConfigDef(configPrefix, stageGroups, field, Utils.formatL("{} Field='{}'", contextMsg,
                        field.getName())));
            } else if (field.getAnnotation(ConfigDefBean.class) != null) {
                List<String> beanGroups = getGroups(field, stageGroups, contextMsg, new ArrayList<ErrorMessage>());
                defs.addAll(extract(configPrefix + field.getName() + ".", field.getType(), beanGroups, true,
                        Utils.formatL("{} BeanField='{}'", contextMsg, field.getName())));
            }
        }
        return defs;
    }

    public List<ConfigDefinition> extract(Class klass, List<String> stageGroups, Object contextMsg) {
        return extract("", klass, stageGroups, contextMsg);
    }

    public List<ConfigDefinition> extract(String configPrefix, Class klass, List<String> stageGroups, Object contextMsg) {
        List<ConfigDefinition> defs = extract(configPrefix, klass, stageGroups, false, contextMsg);
        resolveDependencies("", defs, contextMsg);
        return defs;
    }

    private List<ConfigDefinition> extract(String configPrefix, Class klass, List<String> stageGroups, boolean isBean,
                                           Object contextMsg) {
        List<ErrorMessage> errors = validate(configPrefix, klass, stageGroups, false, isBean, false, contextMsg);
        if (errors.isEmpty()) {
            return getConfigDefinitions(configPrefix, klass, stageGroups, contextMsg);
        } else {
            throw new IllegalArgumentException(Utils.format("Invalid ConfigDefinition: {}", errors));
        }
    }

    private List<ErrorMessage> validateDependencies(List<ConfigDefinition>  defs, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        Map<String, ConfigDefinition> definitionsMap = new HashMap<>();
        for (ConfigDefinition def : defs) {
            definitionsMap.put(def.getName(), def);
        }
        for (ConfigDefinition def : defs) {
            for (Map.Entry<String, List<Object>> dependency : def.getDependsOnMap().entrySet()) {
                String dependsOn = dependency.getKey();
                if (StringUtils.isEmpty(dependsOn)) {
                    continue;
                }
                ConfigDefinition dependsOnDef = definitionsMap.get(dependsOn);
                if (dependsOnDef == null) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_153, contextMsg, def.getName(), dependsOn));
                } else {
                    // evaluate dependsOn triggers
                    for (Object trigger : dependency.getValue()) {
                        errors.addAll(ConfigValueExtractor.get().validate(dependsOnDef.getConfigField(), dependsOnDef.getType(),
                                (String) trigger, contextMsg, true));
                    }
                }
            }
        }
        return errors;
    }

    void resolveDependencies(String configPrefix, List<ConfigDefinition>  defs, Object contextMsg) {
        Map<String, ConfigDefinition> definitionsMap = new HashMap<>();
        Map<String, Map<String, Set<Object>>> dependencyMap = new HashMap<>();
        Map<String, Boolean> isFullyProcessed = new HashMap<>();
        for (ConfigDefinition def : defs) {
            definitionsMap.put(def.getName(), def);
            dependencyMap.put(def.getName(), new HashMap<String, Set<Object>>());
            isFullyProcessed.put(def.getName(), false);
        }

        cycles.clear();

        for (ConfigDefinition def : defs) {
            String dependsOnKey = def.getDependsOn();
            if (!StringUtils.isEmpty(dependsOnKey)) {
                verifyDependencyExists(definitionsMap, def, dependsOnKey, contextMsg);
                ConfigDefinition dependsOnDef = definitionsMap.get(dependsOnKey);
                // evaluate dependsOn triggers
                ConfigDef annotation = def.getConfigField().getAnnotation(ConfigDef.class);
                Set<Object> triggers = new HashSet<>();
                for (String trigger : annotation.triggeredByValue()) {
                    triggers.add(ConfigValueExtractor.get().extract(dependsOnDef.getConfigField(), dependsOnDef.getType(),
                            trigger, contextMsg, true));
                }
                dependencyMap.get(def.getName()).put(dependsOnDef.getName(), triggers);
            }
            // Add direct dependencies to dependencyMap
            if (!def.getDependsOnMap().isEmpty()) {
                // Copy same as above.
                for (Map.Entry<String, List<Object>> dependsOn : def.getDependsOnMap().entrySet()) {
                    dependsOnKey = dependsOn.getKey();
                    if (!StringUtils.isEmpty(dependsOnKey)) {
                        verifyDependencyExists(definitionsMap, def, dependsOnKey, contextMsg);
                        Set<Object> triggers = new HashSet<>();
                        ConfigDefinition dependsOnDef = definitionsMap.get(dependsOnKey);
                        for (Object trigger : dependsOn.getValue()) {
                            triggers.add(ConfigValueExtractor.get().extract(dependsOnDef.getConfigField(), dependsOnDef.getType(),
                                    (String) trigger, contextMsg, true));
                        }
                        Map<String, Set<Object>> dependencies = dependencyMap.get(def.getName());
                        if (dependencies.containsKey(dependsOnKey)) {
                            dependencies.get(dependsOnKey).addAll(triggers);
                        } else {
                            dependencies.put(dependsOnKey, triggers);
                        }
                    }
                }
            }
        }

        for (ConfigDefinition def : defs) {

            if (isFullyProcessed.get(def.getName())) {
                continue;
            }
            // Now find all indirect dependencies
            Deque<StackNode> stack = new ArrayDeque<>();
            stack.push(new StackNode(def, new LinkedHashSet<String>()));
            while (!stack.isEmpty()) {
                StackNode current = stack.peek();
                // We processed this one's dependencies before, don't bother adding its children
                // The dependencies of this one have all been processed
                if (current.childrenAddedToStack) {
                    stack.pop();
                    Map<String, Set<Object>> currentDependencies = dependencyMap.get(current.def.getName());
                    Set<String> children = new HashSet<>(current.def.getDependsOnMap().keySet());
                    for (String child : children) {
                        if (StringUtils.isEmpty(child)) {
                            continue;
                        }
                        Map<String, Set<Object>> depsOfChild = dependencyMap.get(child);
                        for (Map.Entry<String, Set<Object>> depOfChild : depsOfChild.entrySet()) {
                            if (currentDependencies.containsKey(depOfChild.getKey())) {
                                // Add only the common trigger values,
                                // since it has to be one of those for both these to be triggered.
                                Set<Object> currentTriggers = currentDependencies.get(depOfChild.getKey());
                                Set<Object> childTriggers = depOfChild.getValue();
                                currentDependencies.put(depOfChild.getKey(), Sets.intersection(currentTriggers, childTriggers));
                            } else {
                                currentDependencies.put(depOfChild.getKey(), new HashSet<>(depOfChild.getValue()));
                            }
                        }
                    }
                    isFullyProcessed.put(current.def.getName(), true);
                } else {
                    Set<String> children = current.def.getDependsOnMap().keySet();
                    String dependsOn = current.def.getDependsOn();
                    LinkedHashSet<String> dependencyAncestors = new LinkedHashSet<>(current.ancestors);
                    dependencyAncestors.add(current.def.getName());
                    if (!StringUtils.isEmpty(dependsOn)
                            && !isFullyProcessed.get(current.def.getDependsOn())
                            && !detectCycle(dependencyAncestors, cycles, dependsOn)) {
                        stack.push(new StackNode(definitionsMap.get(current.def.getDependsOn()), dependencyAncestors));
                    }
                    for (String child : children) {
                        if (!StringUtils.isEmpty(child)
                                && !isFullyProcessed.get(child)
                                && !detectCycle(dependencyAncestors, cycles, child)) {
                            stack.push(new StackNode(definitionsMap.get(child), dependencyAncestors));
                        }
                    }
                    current.childrenAddedToStack = true;
                }
            }
        }
        Preconditions.checkState(cycles.isEmpty(),
                "The following cycles were detected in the configuration dependencies:\n" + Joiner.on("\n").join(cycles));
        for (Map.Entry<String, Map<String, Set<Object>>> entry : dependencyMap.entrySet()) {
            Map<String, List<Object>> dependencies = new HashMap<>();
            definitionsMap.get(entry.getKey()).setDependsOnMap(dependencies);
            for (Map.Entry<String, Set<Object>> trigger : entry.getValue().entrySet()) {
                List<Object> triggerValues = new ArrayList<>();
                triggerValues.addAll(trigger.getValue());
                dependencies.put(trigger.getKey(), triggerValues);
            }
            definitionsMap.get(entry.getKey()).setDependsOn("");
        }
    }

    /**
     * Verify that the config definition's dependency actually maps to a valid config definition
     */
    private void verifyDependencyExists(
            Map<String, ConfigDefinition> definitionsMap,
            ConfigDefinition def,
            String dependsOnKey,
            Object contextMsg
    ) {
        Preconditions.checkState(definitionsMap.containsKey(dependsOnKey),
                Utils.format("Error while processing {} ConfigDef='{}'. Dependency='{}' does not exist.",
                        contextMsg, def.getName(), dependsOnKey));
    }

    /**
     * Returns true if child creates a dependency with any member(s) of dependencyAncestors.
     * Also adds the stringified cycle to the cycles list
     */
    private boolean detectCycle(LinkedHashSet<String> dependencyAncestors, Set<String> cycles, final String child) {
        if (dependencyAncestors.contains(child)) {
            // Find index of the child in the ancestors list
            int index = -1;
            for (String s : dependencyAncestors) {
                index++;
                if (s.equals(child)) {
                    break;
                }
            }
            // The cycle starts from the first time the child is seen in the ancestors list
            // and continues till the end of the list, followed by the child again.
            cycles.add(Joiner.on(" -> ").join(Iterables.skip(dependencyAncestors, index)) + " -> " + child);
            return true;
        }
        return false;
    }

    List<ErrorMessage> validateConfigDef(String configPrefix, List<String> stageGroups, Field field,
                                         boolean isComplexField, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        ConfigDef annotation = field.getAnnotation(ConfigDef.class);
        errors.addAll(ConfigValueExtractor.get().validate(field, annotation, contextMsg));
        if (annotation.type() == ConfigDef.Type.MODEL && field.getAnnotation(ListBeanModel.class) != null && isComplexField) {
            errors.add(new ErrorMessage(DefinitionError.DEF_161, contextMsg,  field.getName()));
        } else {
            List<ErrorMessage> modelErrors = ModelDefinitionExtractor.get().validate(configPrefix + field.getName() + ".",
                    field, contextMsg);
            errors.addAll(modelErrors);
            if (annotation.type() != ConfigDef.Type.NUMBER &&
                    (annotation.min() != Long.MIN_VALUE || annotation.max() != Long.MAX_VALUE)) {
                errors.add(new ErrorMessage(DefinitionError.DEF_155, contextMsg, field.getName()));
            }
            errors.addAll(validateDependsOnName(configPrefix, annotation.dependsOn(),
                    Utils.formatL("{} Field='{}'", contextMsg, field.getName())));
        }
        return errors;
    }

    @SuppressWarnings("unchecked")
    List<ErrorMessage> validateConfigDefBean(String configPrefix, Field field, List<String> stageGroups,
                                             boolean isComplexField, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        Class klass = field.getType();
        try {
            if (klass.isPrimitive()) {
                errors.add(new ErrorMessage(DefinitionError.DEF_162, contextMsg, klass.getSimpleName()));
            } else {
                klass.getConstructor();
                List<String> beanGroups = getGroups(field, stageGroups, contextMsg, errors);
                errors.addAll(validate(configPrefix, klass, beanGroups, false, true, isComplexField, contextMsg));
            }
        } catch (NoSuchMethodException ex) {
            errors.add(new ErrorMessage(DefinitionError.DEF_156, contextMsg, klass.getSimpleName()));
        }
        return errors;
    }

    @SuppressWarnings("unchecked")
    ConfigDefinition extractConfigDef(String configPrefix, List<String> stageGroups, Field field, Object contextMsg) {
        List<ErrorMessage> errors = validateConfigDef(configPrefix, stageGroups, field, false, contextMsg);
        if (errors.isEmpty()) {
            ConfigDefinition def = null;
            ConfigDef annotation = field.getAnnotation(ConfigDef.class);
            if (annotation != null) {
                String name = field.getName();
                ConfigDef.Type type = annotation.type();
                String label = annotation.label();
                String description = annotation.description();
                Object defaultValue = ConfigValueExtractor.get().extract(field, annotation, contextMsg);
                boolean required = annotation.required();
                String group = annotation.group();
                group = resolveGroup(stageGroups, group, contextMsg, errors);
                String fieldName = field.getName();
                String dependsOn = resolveDependsOn(configPrefix, annotation.dependsOn());
                List<Object> triggeredByValues = null;  // done at resolveDependencies() invocation
                // done at resolveDependencies() invocation - keys are inserted now, values in resolveDependencies
                Map<String, List<Object>> dependsOnMap = new HashMap<>();
                dependsOnMap.put(dependsOn, (List) Arrays.asList(annotation.triggeredByValue()));
                Dependency[] dependencies = annotation.dependencies();
                for (Dependency dependency : dependencies) {
                    if (!StringUtils.isEmpty(dependency.configName())) {
                        dependsOnMap.put(resolveDependsOn(configPrefix, dependency.configName()), (List) Arrays.asList(dependency.triggeredByValues()));
                    }
                }
                ModelDefinition model = ModelDefinitionExtractor.get().extract(configPrefix + field.getName() + ".",
                        field, contextMsg);
                if (model != null) {
                    defaultValue = model.getModelType().prepareDefault(defaultValue);
                }
                int displayPosition = annotation.displayPosition();
                long min = annotation.min();
                long max = annotation.max();
                String mode = (annotation.mode() != null) ? getMimeString(annotation.mode()) : null;
                int lines = annotation.lines();

                def = new ConfigDefinition(field, configPrefix + name, type, label, description, defaultValue, required, group,
                        fieldName, model, dependsOn, triggeredByValues, displayPosition,
                        min, max, mode, lines, dependsOnMap);
            }
            return def;
        } else {
            throw new IllegalArgumentException(Utils.format("Invalid ConfigDefinition: {}", errors));
        }
    }

    private List<ErrorMessage> validateDependsOnName(String configPrefix, String dependsOn, Object contextMsg) {
        List<ErrorMessage> errors = new ArrayList<>();
        if (!dependsOn.isEmpty()) {
            if (dependsOn.startsWith("^")) {
                if (dependsOn.substring(1).contains("^")) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_157, contextMsg));
                }
            } else if (dependsOn.endsWith("^")) {
                boolean gaps = false;
                for (int i = dependsOn.indexOf("^"); !gaps && i < dependsOn.length(); i++) {
                    gaps = dependsOn.charAt(i) != '^';
                }
                if (gaps) {
                    errors.add(new ErrorMessage(DefinitionError.DEF_158, contextMsg));
                } else {
                    int relativeCount = dependsOn.length() - dependsOn.indexOf("^");
                    int dotCount = configPrefix.split("\\.").length;
                    if (relativeCount > dotCount) {
                        errors.add(new ErrorMessage(DefinitionError.DEF_159, contextMsg, relativeCount, dotCount, configPrefix));
                    }
                }
            }
        }
        return  errors;
    }

    private String resolveDependsOn(String configPrefix, String dependsOn) {
        if (!dependsOn.isEmpty()) {
            if (dependsOn.startsWith("^")) {
                //is absolute from the top
                dependsOn = dependsOn.substring(1);
            } else if (dependsOn.endsWith("^")) {
                configPrefix = configPrefix.substring(0, configPrefix.length() - 1);
                //is relative backwards based on the ^ count
                int relativeCount = dependsOn.length() - dependsOn.indexOf("^");
                while (relativeCount > 0) {
                    int pos = configPrefix.lastIndexOf(".");
                    configPrefix = (pos == -1) ? "" : configPrefix.substring(0, pos);
                    relativeCount--;
                }
                if (!configPrefix.isEmpty()) {
                    configPrefix += ".";
                }
                dependsOn = configPrefix + dependsOn.substring(0, dependsOn.indexOf("^"));
            } else {
                dependsOn = configPrefix + dependsOn;
            }
        }
        return  dependsOn;
    }

    private String getMimeString(ConfigDef.Mode mode) {
        switch(mode) {
            case JSON:
                return "application/json";
            case PLAIN_TEXT:
                return "text/plain";
            case PYTHON:
                return "text/x-python";
            case JAVASCRIPT:
                return "text/javascript";
            case RUBY:
                return "text/x-ruby";
            case JAVA:
                return "text/x-java";
            case GROOVY:
                return "text/x-groovy";
            case SCALA:
                return "text/x-scala";
            case SQL:
                return "text/x-sql";
            case SHELL:
                return "text/x-sh";
            default:
                return null;
        }
    }

    private static final Set<ConfigDef.Type> TYPES_SUPPORTING_ELS = ImmutableSet.of(ConfigDef.Type.LIST,
            ConfigDef.Type.MAP,
            ConfigDef.Type.NUMBER,
            ConfigDef.Type.STRING,
            ConfigDef.Type.TEXT,
            ConfigDef.Type.CREDENTIAL
    );

    private class StackNode {
        final ConfigDefinition def;
        boolean childrenAddedToStack;
        final LinkedHashSet<String> ancestors;

        StackNode(ConfigDefinition def, LinkedHashSet<String> ancestors) {
            this.def = def;
            this.childrenAddedToStack = false;
            this.ancestors = ancestors;
        }

    }

}
