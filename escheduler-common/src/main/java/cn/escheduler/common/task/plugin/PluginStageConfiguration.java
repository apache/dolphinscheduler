package cn.escheduler.common.task.plugin;

import java.util.List;

public class PluginStageConfiguration {
    private String libraryName;
    private String name;
    private int stageVersion;

    private List<PluginConfig> configValue;

    public PluginStageConfiguration(){

    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStageVersion() {
        return stageVersion;
    }

    public void setStageVersion(int stageVersion) {
        this.stageVersion = stageVersion;
    }

    public List<PluginConfig> getConfigValue() {
        return configValue;
    }

    public void setConfigValue(List<PluginConfig> configValue) {
        this.configValue = configValue;
    }
}
