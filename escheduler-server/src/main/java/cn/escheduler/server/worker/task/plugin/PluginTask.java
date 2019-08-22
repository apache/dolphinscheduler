package cn.escheduler.server.worker.task.plugin;

import cn.escheduler.common.plugin.PluginManager;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.plugin.PluginParameters;
import cn.escheduler.plugin.api.Command;
import cn.escheduler.plugin.api.ConfigIssue;
import cn.escheduler.plugin.api.Stage;
import cn.escheduler.plugin.sdk.creation.ContextInfoCreator;
import cn.escheduler.plugin.sdk.validation.Issue;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginTask extends AbstractTask {
    /**
     *  sdc parameters
     */
    private PluginParameters pluginParameters;

    public PluginTask(TaskProps props, Logger logger) {
        super(props, logger);

        this.pluginParameters = JSONObject.parseObject(props.getTaskParams(), PluginParameters.class);

        if (!pluginParameters.checkParameters()) {
            throw new RuntimeException("sdc task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);
        logger.info("sdc task params {}", taskProps.getTaskParams());

        if (pluginParameters.getStageConfig() == null) {
            logger.error("Stage Config is missing");
            exitStatusCode = -1;
            return;
        }

        PluginManager instance = PluginManager.getInstance();
        Map<String, Object> pipelineConstants = new HashMap<>();
        List<Issue> errors = new ArrayList<>();
        Stage stage = null;
        try {
            stage = instance.getStageInstance(pluginParameters.getStageConfig(), pipelineConstants, errors);
            if (!errors.isEmpty()) {
                errors.forEach(error ->
                        logger.error("Get Stage error: {}", error)
                );
                exitStatusCode = -1;
                return;
            }

            // init source
            Command.Context srcContext = ContextInfoCreator.createContext(pluginParameters.getStageConfig().getName() + "-instance", false, logger);
            Stage.Info srcInfo = ContextInfoCreator.createInfo(pluginParameters.getStageConfig().getName(), pluginParameters.getStageConfig().getStageVersion(), pluginParameters.getStageConfig().getName() + "-instance");
            List<ConfigIssue> srcInitResult = stage.init(srcInfo, srcContext);

            if (!srcInitResult.isEmpty()) {
                srcInitResult.forEach(error ->
                        logger.error("Init Source Stage error: {}", error)
                );
                exitStatusCode = -1;
                return;
            }

            ((Command)stage).run();

            exitStatusCode = 0;
            return;
        } finally {
            if (stage != null) {
                stage.destroy();
            }
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return pluginParameters;
    }
}