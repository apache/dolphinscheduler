package org.apache.dolphinscheduler.plugin.alert.ewechat;

public final class EWeChatParamsConstants {

    private EWeChatParamsConstants() {
        throw new IllegalStateException(EWeChatParamsConstants.class.getName());
    }

    static final String EWE_CHAT_PROXY_ENABLE = "$t('isEnableProxy')";

    static final String NAME_EWE_CHAT_PROXY_ENABLE = "IsEnableProxy";

    static final String EWE_CHAT_WEB_HOOK = "$t('webhook')";

    static final String NAME_EWE_CHAT_WEB_HOOK = "WebHook";

    static final String EWE_CHAT_PROXY = "$t('proxy')";

    static final String NAME_EWE_CHAT_PROXY = "Proxy";

    static final String EWE_CHAT_PORT = "$t('port')";

    static final String NAME_EWE_CHAT_PORT = "Port";

    static final String EWE_CHAT_USER = "$t('user')";

    static final String NAME_EWE_CHAT_USER = "User";

    static final String EWE_CHAT_PASSWORD = "$t('password')";

    static final String NAME_EWE_CHAT_PASSWORD = "Password";

}
