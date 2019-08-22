package cn.escheduler.common.plugin;

import cn.escheduler.common.task.plugin.PluginStageConfiguration;
import cn.escheduler.common.utils.JSONUtils;
import cn.escheduler.plugin.api.Config;
import cn.escheduler.plugin.api.Stage;
import cn.escheduler.plugin.api.impl.LocaleInContext;
import cn.escheduler.plugin.sdk.config.StageConfiguration;
import cn.escheduler.plugin.sdk.config.StageDefinition;
import cn.escheduler.plugin.sdk.creation.ConfigInjector;
import cn.escheduler.plugin.sdk.stagelibrary.ClassLoaderStageLibraryTask;
import cn.escheduler.plugin.sdk.stagelibrary.DirClassLoader;
import cn.escheduler.plugin.sdk.util.PipelineConfigurationUtil;
import cn.escheduler.plugin.sdk.validation.Issue;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import cn.escheduler.common.Constants;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.escheduler.common.utils.PropertyUtils.getString;

public class PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private static volatile PluginManager INSTANCE = null;
    private ClassLoaderStageLibraryTask stageLibraryTask;
    private Map<String, Map<String, StageDefinition>> allStagesMap;
    private Map<String, List<StageDisplayInfo>> localizedStageDisplayInfos;

    private PluginManager() {
    }

    public static PluginManager getInstance() {
        if (INSTANCE == null) {
            synchronized (PluginManager.class) {
                // when more than two threads run into the first null check same time, to avoid instanced more than one time, it needs to be checked again.
                if (INSTANCE == null) {
                    PluginManager tempInstance = new PluginManager();
                    //finish QuartzExecutors init
                    tempInstance.init();
                    INSTANCE = tempInstance;
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        String pluginPath = getString(Constants.PLUGIN_PATH);
        File pluginDir = new File(pluginPath);
        if (!pluginDir.isDirectory()) {
            logger.warn("SDC plugin directory not exists! {}", pluginPath);
        } else {
            List<DirClassLoader> classLoaders = Arrays.stream(pluginDir.listFiles(f -> f.isDirectory())).map(x -> {
                DirClassLoader dirClassLoader = new DirClassLoader(x, PluginManager.class.getClassLoader());
                return dirClassLoader;
            }).collect(Collectors.toList());

            stageLibraryTask = new ClassLoaderStageLibraryTask(classLoaders);
            try {
                stageLibraryTask.initTask();
            } catch (Exception e) {
                logger.error("Stage Library Task init error, IGNORE for now.", e);
            }
            // init allStagesMap
            allStagesMap = new HashMap<>();
            stageLibraryTask.getStages().stream().forEach(l -> {
                logger.info("loaded library stage: {}", l);
                String libName = l.getLibrary();
                String stageName = l.getName();
                if (!allStagesMap.containsKey(libName)) {
                    allStagesMap.put(libName, new HashMap<>());
                }
                allStagesMap.get(libName).put(stageName, l);
            });
        }

        localizedStageDisplayInfos = ImmutableMap.of(
                Locale.ENGLISH.getLanguage(), getStageDisplayInfos(Locale.ENGLISH),
                Locale.CHINESE.getLanguage(), getStageDisplayInfos(Locale.CHINESE)
        );
    }

    private List<StageDisplayInfo> getStageDisplayInfos(Locale locale) {
        Locale currentLocale = LocaleInContext.get();
        try {
            LocaleInContext.set(locale);
            List<StageDisplayInfo> stageDisplayInfos = new ArrayList<>();
            allStagesMap.entrySet().forEach(libMapEntry -> {
                String libName = libMapEntry.getKey();
                libMapEntry.getValue().entrySet().forEach(stageMapEntry -> {
                    String stageName = stageMapEntry.getKey();
                    StageDefinition stage = stageMapEntry.getValue().localize();
                    StageDisplayInfo displayInfo = new StageDisplayInfo();
                    displayInfo.setLibraryName(libName);
                    displayInfo.setLibraryLabel(stage.getLibraryLabel());
                    displayInfo.setName(stageName);
                    displayInfo.setLabel(stage.getLabel());
                    displayInfo.setType(stage.getType());
                    try {
                        String iconBase64 = Base64.getEncoder().encodeToString(
                                IOUtils.toByteArray(stage.getStageClassLoader().getResource(stage.getIcon()))
                        );
                        displayInfo.setIconBase64(iconBase64);
                    } catch (IOException e) {
                        logger.warn("read stage icon failed. ", e);
                    }
                    StageConfiguration defaultStageConfiguration = PipelineConfigurationUtil.getStageConfigurationWithDefaultValues(
                            stageLibraryTask,
                            libName,
                            stageName,
                            stageName + "-instance",
                            ""
                    );
                    displayInfo.setDefaultConfigurationJson(JSONUtils.toJson(defaultStageConfiguration.getConfiguration()));
                    List<Map<String, String>> groupNames = stage.getConfigGroupDefinition().getGroupNameToLabelMapList();
                    displayInfo.setGroupNames(groupNames);
                    displayInfo.setStageVersion(stage.getVersion());
                    displayInfo.setConfigurationDefinitionJson(
                            JSONObject.toJSONString(stage.getConfigDefinitions(), fastJsonConfigDefinitionFilter)
                    );

                    stageDisplayInfos.add(displayInfo);
                });
            });
            return stageDisplayInfos;
        } finally {
            LocaleInContext.set(currentLocale);
        }
    }

    private PropertyFilter fastJsonConfigDefinitionFilter = new PropertyFilter() {
        private Set<String> ignoreFieldNames = ImmutableSet.of(
                "dependsOn",
                "triggeredByValues",
                "elConstantDefinitions",
                "elConstantDefinitionsIdx",
                "elDefs",
                "elFunctionDefinitions",
                "elFunctionDefinitionsIdx",
                "annotatedType",
                "annotations",
                "configField",
                "configDefinitionsAsMap"
        );
        @Override
        public boolean apply(Object source, String name, Object value) {
            if (ignoreFieldNames.contains(name)) {
                return false;
            }
            return true;
        }
    };


    public List<StageDisplayInfo> getAllStages(Locale locale) {
        String targetLanguage = Locale.ENGLISH.getLanguage();
        if (localizedStageDisplayInfos.containsKey(locale.getLanguage())) {
            targetLanguage = locale.getLanguage();
        }
        return localizedStageDisplayInfos.get(targetLanguage);
    }

    public StageDefinition getStageDefinition(PluginStageConfiguration stageConfig) {
        return allStagesMap.get(stageConfig.getLibraryName()).get(stageConfig.getName());
    }

    public Stage getStageInstance(PluginStageConfiguration stageConfig, Map<String, Object> pipelineConstants, List<Issue> errors) throws IllegalAccessException, InstantiationException {
        StageDefinition stage = getStageDefinition(stageConfig);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(stage.getStageClassLoader());
            StageConfiguration config = PipelineConfigurationUtil.getStageConfigurationWithDefaultValues(
                    stageLibraryTask,
                    stage.getLibrary(),
                    stage.getName(),
                    stage.getName() + "-instance",
                    ""
            );

            stageConfig.getConfigValue().forEach(item ->
                    config.addConfig(new Config(item.getName(), item.getValue()))
            );

            Stage instance = stage.getStageClass().newInstance();
            ConfigInjector.get().injectStage(instance, stage, config, pipelineConstants, errors);
            return instance;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}