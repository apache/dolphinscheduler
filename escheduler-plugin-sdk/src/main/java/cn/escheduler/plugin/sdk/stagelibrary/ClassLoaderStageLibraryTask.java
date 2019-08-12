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
package cn.escheduler.plugin.sdk.stagelibrary;

import cn.escheduler.plugin.api.impl.LocaleInContext;
import cn.escheduler.plugin.api.impl.Utils;
import cn.escheduler.plugin.sdk.config.StageDefinition;
import cn.escheduler.plugin.sdk.config.StageLibraryDefinition;
import cn.escheduler.plugin.sdk.definition.StageDefinitionExtractor;
import cn.escheduler.plugin.sdk.definition.StageLibraryDefinitionExtractor;
import cn.escheduler.plugin.sdk.json.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ClassLoaderStageLibraryTask implements StageLibraryTask {
    public static final String IGNORE_STAGE_DEFINITIONS = "ignore.stage.definitions";

    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderStageLibraryTask.class);

    private List<? extends ClassLoader> stageClassLoaders;
    private List<StageLibraryDefinition> stageLibraries;
    private Map<String, StageDefinition> stageMap;
    private List<StageDefinition> stageList;
    private LoadingCache<Locale, List<StageDefinition>> localizedStageList;
    private ObjectMapper json;

    public ClassLoaderStageLibraryTask(List<? extends ClassLoader> stageLibraryClassLoaders) {
        this.stageClassLoaders = stageLibraryClassLoaders;
    }

    public void initTask() {
        // Load all stages and other objects from the libraries
        json = ObjectMapperFactory.get();
        stageLibraries = new ArrayList<>();
        stageList = new ArrayList<>();
        stageMap = new HashMap<>();
        loadStages();
        stageLibraries = ImmutableList.copyOf(stageLibraries);
        stageList = ImmutableList.copyOf(stageList);
        stageMap = ImmutableMap.copyOf(stageMap);

        // localization cache for definitions
        localizedStageList = CacheBuilder.newBuilder().build(new CacheLoader<Locale, List<StageDefinition>>() {
            @Override
            public List<StageDefinition> load(Locale key) throws Exception {
                List<StageDefinition> list = new ArrayList<>();
                for (StageDefinition stage : stageList) {
                    list.add(stage.localize());
                }
                return list;
            }
        });
        validateStageVersions(stageList);
    }

    String getPropertyFromLibraryProperties(ClassLoader cl, String property, String defaultValue) throws IOException {
        try (InputStream is = cl.getResourceAsStream(StageLibraryDefinitionExtractor.DATA_COLLECTOR_LIBRARY_PROPERTIES)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty(property, defaultValue);
            }
        }

        return null;
    }

    Set<String> loadIgnoreStagesList(StageLibraryDefinition libDef) throws IOException {
        Set<String> ignoreStages = new HashSet<>();

        String ignore = getPropertyFromLibraryProperties(libDef.getClassLoader(), IGNORE_STAGE_DEFINITIONS, "");
        if(!StringUtils.isEmpty(ignore)) {
            ignoreStages.addAll(Splitter.on(",").trimResults().splitToList(ignore));
        }

        return ignoreStages;
    }

    List<String> removeIgnoreStagesFromList(StageLibraryDefinition libDef, List<String> stages) throws IOException {
        List<String> list = new ArrayList<>();
        Set<String> ignoreStages = loadIgnoreStagesList(libDef);
        Iterator<String> iterator = stages.iterator();
        while (iterator.hasNext()) {
            String stage = iterator.next();
            if (ignoreStages.contains(stage)) {
                LOG.debug("Ignoring stage class '{}' from library '{}'", stage, libDef.getName());
            } else {
                list.add(stage);
            }
        }
        return list;
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    void loadStages() {
        if (LOG.isDebugEnabled()) {
            for (ClassLoader cl : stageClassLoaders) {
                LOG.debug("Found stage library '{}'", StageLibraryUtils.getLibraryName(cl));
            }
        }

        try {
            int libs = 0;
            int stages = 0;
            long start = System.currentTimeMillis();
            LocaleInContext.set(Locale.getDefault());
            for (ClassLoader cl : stageClassLoaders) {
                try {
                    // Load stages from the stage library
                    StageLibraryDefinition libDef = StageLibraryDefinitionExtractor.get().extract(cl);
                    libDef.setVersion(getPropertyFromLibraryProperties(cl, "version", ""));
                    LOG.debug("Loading stages and plugins from library '{}' on version {}", libDef.getName(), libDef.getVersion());
                    stageLibraries.add(libDef);
                    libs++;

                    // Load Stages
                    for(Class klass : loadClassesFromResource(libDef, cl, STAGES_DEFINITION_RESOURCE)) {
                        stages++;
                        StageDefinition stage = StageDefinitionExtractor.get().extract(libDef, klass, Utils.formatL("Library='{}'", libDef.getName()));
                        String key = createKey(libDef.getName(), stage.getName());
                        LOG.debug("Loaded stage '{}'  version {}", key, stage.getVersion());
                        stageList.add(stage);
                        stageMap.put(key, stage);
                    }

                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(
                            Utils.format("Could not load stages definition from '{}', {}", cl, ex.toString()), ex);
                }
            }
            LOG.info(
                    "Loaded '{}' libraries with a total of '{}' stages in '{}ms'",
                    libs,
                    stages,
                    System.currentTimeMillis() - start
            );
        } finally {
            LocaleInContext.set(null);
        }
    }

    private <T> List<Class<? extends T>> loadClassesFromResource(
            StageLibraryDefinition libDef,
            ClassLoader cl,
            String resourceName
    ) throws IOException, ClassNotFoundException {
        Set<String> dedup = new HashSet<>();
        List<Class<? extends T>> list = new ArrayList<>();

        // Load all resource files with given name
        Enumeration<URL> resources = cl.getResources(resourceName);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            try (InputStream is = url.openStream()) {
                List<String> plugins = json.readValue(is, List.class);
                plugins = removeIgnoreStagesFromList(libDef, plugins);
                for (String className : plugins) {
                    if(dedup.contains(className)) {
                        throw new IllegalStateException(Utils.format(
                                "Library '{}' contains more than one definition for '{}'",
                                libDef.getName(), className));
                    }
                    dedup.add(className);
                    list.add((Class<? extends T>) cl.loadClass(className));
                }
            }
        }

        return list;
    }

    @VisibleForTesting
    void validateStageVersions(List<StageDefinition> stageList) {
        boolean err = false;
        Map<String, Set<Integer>> stageVersions = new HashMap<>();
        for (StageDefinition stage : stageList) {
            Set<Integer> versions = stageVersions.get(stage.getName());
            if (versions == null) {
                versions = new HashSet<>();
                stageVersions.put(stage.getName(), versions);
            }
            versions.add(stage.getVersion());
            err |= versions.size() > 1;
        }
        if (err) {
            List<String> errors = new ArrayList<>();
            for (Map.Entry<String, Set<Integer>> entry : stageVersions.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (StageDefinition stage : stageList) {
                        if (stage.getName().equals(entry.getKey())) {
                            errors.add(Utils.format("Stage='{}' Version='{}' Library='{}'", stage.getName(), stage.getVersion(),
                                    stage.getLibrary()));
                        }
                    }
                }
            }
            LOG.error("There cannot be 2 different versions of the same stage: {}", errors);
            throw new RuntimeException(Utils.format("There cannot be 2 different versions of the same stage: {}", errors));
        }
    }

    private String createKey(String library, String name) {
        return library + ":" + name;
    }

    @Override
    public List<StageDefinition> getStages() {
        try {
            return (LocaleInContext.get() == null) ? stageList : localizedStageList.get(LocaleInContext.get());
        } catch (ExecutionException ex) {
            LOG.warn("Error loading locale '{}', {}", LocaleInContext.get(), ex.toString(), ex);
            return stageList;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public StageDefinition getStage(String library, String name, boolean forExecution) {
        StageDefinition def = stageMap.get(createKey(library, name));
        return def;
    }

     @Override
    public List<StageLibraryDefinition> getLoadedStageLibraries() {
        return stageLibraries;
    }
}
