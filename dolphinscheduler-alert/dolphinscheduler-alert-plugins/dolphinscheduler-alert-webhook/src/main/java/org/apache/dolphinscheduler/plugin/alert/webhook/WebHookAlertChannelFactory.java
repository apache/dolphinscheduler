package org.apache.dolphinscheduler.plugin.alert.webhook;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.ShowType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;
import java.util.Arrays;
import java.util.List;

@AutoService(AlertChannelFactory.class)
public final class WebHookAlertChannelFactory implements AlertChannelFactory {
    @Override
    public String name() {
        return "WebHook";
    }

    @Override
    public List<PluginParams> params() {
        InputParam url = InputParam.newBuilder(WebHookAlertConstants.NAME_WEBHOOK_URL, WebHookAlertConstants.WEBHOOK_URL)
                .setPlaceholder("input request webhook URL")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam showType = RadioParam.newBuilder(AlertConstants.NAME_SHOW_TYPE, AlertConstants.SHOW_TYPE)
                .addParamsOptions(new ParamsOptions(ShowType.MARKDOWN.getDescp(), ShowType.MARKDOWN.getDescp(), false))
                .addParamsOptions(new ParamsOptions(ShowType.TEXT.getDescp(), ShowType.TEXT.getDescp(), false))
                .setValue(ShowType.MARKDOWN.getDescp())
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        return Arrays.asList(url, showType);
    }

    @Override
    public AlertChannel create() {
        return new WebHookAlertChannel();
    }
}
