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

import cn.escheduler.plugin.api.Config;
import cn.escheduler.plugin.api.impl.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StageConfiguration implements Serializable, UserConfigurable {

    //basic info
    private final String instanceName;
    private String library;
    private String stageName;
    private int stageVersion;
    private final List<Config> configuration;
    private final Map<String, Config> configurationMap;
    private final Map<String, Object> uiInfo;

    //wiring with other components
    private final List<String> inputLanes;
    private List<String> outputLanes;
    private List<String> eventLanes;
    private List<String> outputAndEventLanes; // Lazily calculated

    private boolean systemGenerated;

    // Will be set to true if this stage is in the event path
    private boolean inEventPath;

    public StageConfiguration(
            String instanceName,
            String library,
            String stageName,
            int stageVersion,
            List<Config> configuration,
            Map<String, Object> uiInfo,
            List<String> inputLanes,
            List<String> outputLanes,
            List<String> eventLanes
    ) {
        this.instanceName = instanceName;
        this.library = library;
        this.stageName = stageName;
        this.stageVersion = stageVersion;
        this.uiInfo = (uiInfo != null) ? new HashMap<>(uiInfo) : new HashMap<>();
        this.inputLanes = inputLanes;
        this.outputLanes = outputLanes;
        this.eventLanes = eventLanes;
        this.configuration = new ArrayList<>();
        this.configurationMap = new HashMap<>();
        this.inEventPath = false;
        setConfig(configuration);
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setLibrary(String name) {
        library = name;
    }

    public String getLibrary() {
        return library;
    }

    public void setStageName(String name) {
        stageName = name;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageVersion(int version) {
        stageVersion = version;
    }

    public int getStageVersion() {
        return stageVersion;
    }

    @Override
    public List<Config> getConfiguration() {
        return new ArrayList<>(configuration);
    }

    public Map<String, Object> getUiInfo() {
        return uiInfo;
    }

    public List<String> getInputLanes() {
        return inputLanes;
    }

    public List<String> getOutputLanes() {
        return outputLanes;
    }

    public void setOutputLanes(List<String> outputLanes) {
        this.outputLanes = outputLanes;
        this.outputAndEventLanes = null;
    }

    public List<String> getEventLanes() {
        if (eventLanes == null) {
            return Collections.emptyList();
        }
        return eventLanes;
    }

    public void setEventLanes(List<String> eventLanes) {
        this.eventLanes = eventLanes;
        this.outputAndEventLanes = null;
    }

    public List<String> getOutputAndEventLanes() {
        if(outputAndEventLanes == null) {
            outputAndEventLanes = new LinkedList<>();
            outputAndEventLanes.addAll(getOutputLanes());
            outputAndEventLanes.addAll(getEventLanes());
        }

        return outputAndEventLanes;
    }

    @Override
    public Config getConfig(String name) {
        return configurationMap.get(name);
    }

    public void setConfig(List<Config> configList) {
        configuration.clear();
        configuration.addAll(configList);
        configurationMap.clear();
        for (Config conf : configuration) {
            configurationMap.put(conf.getName(), conf);
        }
    }

    public void addConfig(Config config) {
        Config prevConfig = configurationMap.put(config.getName(), config);
        if (prevConfig != null) {
            configuration.remove(prevConfig);
        }
        configuration.add(config);
    }

    public void setSystemGenerated() {
        systemGenerated = true;
    }

    public boolean isSystemGenerated() {
        return systemGenerated;
    }

    public void setInEventPath(boolean inEventPath) {
        this.inEventPath = inEventPath;
    }

    public boolean isInEventPath() {
        return inEventPath;
    }

    @Override
    public String toString() {
        return Utils.format(
                "StageConfiguration[instanceName='{}' library='{}' name='{}' version='{}' input='{}' output='{}']",
                getInstanceName(), getLibrary(), getStageName(), getStageVersion(), getInputLanes(), getOutputLanes());
    }

}
