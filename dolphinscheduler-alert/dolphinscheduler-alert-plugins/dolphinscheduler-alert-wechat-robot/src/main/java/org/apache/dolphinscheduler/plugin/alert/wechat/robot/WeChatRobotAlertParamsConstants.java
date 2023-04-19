package org.apache.dolphinscheduler.plugin.alert.wechat.robot;

public final class WeChatRobotAlertParamsConstants {

    static final String WECHAT_ROBOT_WEB_HOOK = "$t('webhook')";
    static final String NAME_WECHAT_ROBOT_WEB_HOOK = "WebHook";

    static final String MSG_TYPE = "$t('msgType')";
    static final String NAME_MSG_TYPE = "MsgType";
    static final String MSG_TYPE_TEXT = "text";
    static final String MSG_TYPE_MARKDOWN = "markdown";

    public WeChatRobotAlertParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
