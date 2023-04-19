package org.apache.dolphinscheduler.plugin.alert.wechat.robot;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;

import java.util.Map;

public class WeChatRobotAlertChannel implements AlertChannel {

    @Override
    public AlertResult process(AlertInfo info) {
        AlertData alertData = info.getAlertData();
        Map<String, String> params = info.getAlertParams();
        if (null == params) {
            return new AlertResult("false", "WeChat Robot params is null");
        }
        return new WeChatRobotSender(params).sendGroupChatMsg(alertData.getTitle(), alertData.getContent());
    }
}
