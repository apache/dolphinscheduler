package cn.escheduler.common.plugin;

import cn.escheduler.plugin.api.StageType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StageDisplayInfo {
    private String libraryName;
    private String libraryLabel;
    private String name;
    private String label;
    private StageType type;
    private String iconBase64;
    private String defaultConfigurationJson;
    private String configurationDefinitionJson;
    private int stageVersion;
    private List<Map<String, String>> groupNames;

    public StageDisplayInfo() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryName, libraryLabel, name, label, type, iconBase64);
    }

    @Override
    public String toString() {
        return label + " (lib=" + libraryLabel + ", name=" + name + ", type=" + type + ")";
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getLibraryLabel() {
        return libraryLabel;
    }

    public void setLibraryLabel(String libraryLabel) {
        this.libraryLabel = libraryLabel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public StageType getType() {
        return type;
    }

    public void setType(StageType type) {
        this.type = type;
    }

    public String getIconBase64() {
        return iconBase64;
    }

    public void setIconBase64(String iconBase64) {
        this.iconBase64 = iconBase64;
    }

    public String getDefaultConfigurationJson() {
        return defaultConfigurationJson;
    }

    public void setDefaultConfigurationJson(String defaultConfigurationJson) {
        this.defaultConfigurationJson = defaultConfigurationJson;
    }

    public List<Map<String, String>> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<Map<String, String>> groupNames) {
        this.groupNames = groupNames;
    }

    public int getStageVersion() {
        return stageVersion;
    }

    public void setStageVersion(int stageVersion) {
        this.stageVersion = stageVersion;
    }

    public String getConfigurationDefinitionJson() {
        return configurationDefinitionJson;
    }

    public void setConfigurationDefinitionJson(String configurationDefinitionJson) {
        this.configurationDefinitionJson = configurationDefinitionJson;
    }
}