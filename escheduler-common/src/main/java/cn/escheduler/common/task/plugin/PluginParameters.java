package cn.escheduler.common.task.plugin;

import cn.escheduler.common.task.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

public class PluginParameters extends AbstractParameters {
    private PluginStageConfiguration stageConfig;

    @Override
    public boolean checkParameters() {
        return true;
    }

    @Override
    public List<String> getResourceFilesList() {
        return new ArrayList<>();
    }

    public PluginStageConfiguration getStageConfig() {
        return stageConfig;
    }

    public void setStageConfig(PluginStageConfiguration stageConfig) {
        this.stageConfig = stageConfig;
    }
}