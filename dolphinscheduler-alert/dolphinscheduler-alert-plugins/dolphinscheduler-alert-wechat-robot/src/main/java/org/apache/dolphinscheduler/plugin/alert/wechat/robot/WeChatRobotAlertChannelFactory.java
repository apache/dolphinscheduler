package org.apache.dolphinscheduler.plugin.alert.wechat.robot;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

@AutoService(AlertChannelFactory.class)
public class WeChatRobotAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "WeChatRobot";
    }

    @Override
    public AlertChannel create() {
        return new WeChatRobotAlertChannel();
    }

    @Override
    public List<PluginParams> params() {
        InputParam webHookParam = InputParam
                .newBuilder(WeChatRobotAlertParamsConstants.NAME_WECHAT_ROBOT_WEB_HOOK,
                        WeChatRobotAlertParamsConstants.WECHAT_ROBOT_WEB_HOOK)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        RadioParam msgTypeParam = RadioParam
                .newBuilder(WeChatRobotAlertParamsConstants.NAME_MSG_TYPE, WeChatRobotAlertParamsConstants.MSG_TYPE)
                .addParamsOptions(new ParamsOptions(WeChatRobotAlertParamsConstants.MSG_TYPE_TEXT,
                        WeChatRobotAlertParamsConstants.MSG_TYPE_TEXT, false))
                .addParamsOptions(new ParamsOptions(WeChatRobotAlertParamsConstants.MSG_TYPE_MARKDOWN,
                        WeChatRobotAlertParamsConstants.MSG_TYPE_MARKDOWN, false))
                .setValue(WeChatRobotAlertParamsConstants.MSG_TYPE_TEXT)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        return Arrays.asList(webHookParam, msgTypeParam);
    }
}
