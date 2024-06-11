package org.apache.dolphinscheduler.plugin.trigger.simple;

import org.apache.dolphinscheduler.plugin.trigger.api.TriggerChannel;
import org.apache.dolphinscheduler.plugin.trigger.api.TriggerChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.ArrayList;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(TriggerChannelFactory.class)
public class SimpleTriggerChannelFactory implements TriggerChannelFactory {

    @Override
    public TriggerChannel create() {
        return new SimpleTriggerChannel();
    }

    @Override
    public String getName() {
        return "SIMPLE";
    }

    @Override
    public List<PluginParams> getParams() {
        List<PluginParams> paramsList = new ArrayList<>();

        InputParam nodeName = InputParam.newBuilder("name", "$t('Node name')")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam runFlag = RadioParam.newBuilder("runFlag", "RUN_FLAG")
                .addParamsOptions(new ParamsOptions("NORMAL", "NORMAL", false))
                .addParamsOptions(new ParamsOptions("FORBIDDEN", "FORBIDDEN", false))
                .build();

        paramsList.add(nodeName);
        paramsList.add(runFlag);
        return paramsList;
    }
}
