package org.apache.dolphinscheduler.plugin.task.tis;

import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.Arrays;
import java.util.List;

/**
 * TIS endpoint
 **/
public class TISTaskChannelFactory implements TaskChannelFactory {

    @Override
    public TaskChannel create() {
        return new TISTaskChannel();
    }

    @Override
    public String getName() {
        return "TIS";
    }

    @Override
    public List<PluginParams> getParams() {
        InputParam webHookParam = InputParam.newBuilder(TISParamsConstants.NAME_TARGET_JOB_NAME, TISParamsConstants.TARGET_JOB_NAME)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        return Arrays.asList(webHookParam);
    }
}
