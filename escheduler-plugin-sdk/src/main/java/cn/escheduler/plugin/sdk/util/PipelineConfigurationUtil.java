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
package cn.escheduler.plugin.sdk.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import cn.escheduler.plugin.sdk.config.StageConfiguration;
import cn.escheduler.plugin.sdk.config.StageDefinition;
import cn.escheduler.plugin.sdk.stagelibrary.StageLibraryTask;
import cn.escheduler.plugin.api.Config;
import cn.escheduler.plugin.sdk.config.ConfigDefinition;
import cn.escheduler.plugin.sdk.config.ModelDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineConfigurationUtil {

    private PipelineConfigurationUtil() {}

    public static StageConfiguration getStageConfigurationWithDefaultValues(
            StageLibraryTask stageLibraryTask,
            String library,
            String stageName,
            String stageInstanceName,
            String labelPrefix
    ) {
        StageDefinition stageDefinition = stageLibraryTask.getStage(library, stageName, false);

        if (stageDefinition == null) {
            return null;
        }

        List<Config> configurationList = new ArrayList<>();
        for (ConfigDefinition configDefinition : stageDefinition.getConfigDefinitions()) {
            configurationList.add(getConfigWithDefaultValue(configDefinition));
        }

        return new StageConfiguration(
                stageInstanceName,
                library,
                stageName,
                stageDefinition.getVersion(),
                configurationList,
                ImmutableMap.of(
                        "label", labelPrefix + stageDefinition.getLabel(),
                        "stageType", stageDefinition.getType().toString()
                ),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private static Config getConfigWithDefaultValue(ConfigDefinition configDefinition) {
        switch (configDefinition.getType()) {
            case MODEL:
                ModelDefinition modelDefinition = configDefinition.getModel();
                switch (modelDefinition.getModelType()) {
                    case FIELD_SELECTOR_MULTI_VALUE:
                        return new Config(configDefinition.getName(), Collections.emptyList());
                    case LIST_BEAN:
                        Map<String, Object> listBeanDefaultValue = new HashMap<>();
                        for (ConfigDefinition modelConfigDefinition : modelDefinition.getConfigDefinitions()) {
                            Config listBeanConfig = getConfigWithDefaultValue(modelConfigDefinition);
                            listBeanDefaultValue.put(modelConfigDefinition.getName(), listBeanConfig.getValue());
                        }
                        return new Config(configDefinition.getName(), ImmutableList.of(listBeanDefaultValue));
                    default:
                        break;
                }
                break;
            case MAP:
            case LIST:
                return new Config(configDefinition.getName(), Collections.emptyList());
            case BOOLEAN:
                return new Config(configDefinition.getName(), false);
            default:
                return new Config(configDefinition.getName(), configDefinition.getDefaultValue());
        }
        return new Config(configDefinition.getName(), configDefinition.getDefaultValue());
    }
}
