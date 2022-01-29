package org.apache.dolphinscheduler.plugin.alert.telegram;


public final class TelegramParamsConstants {

    static final String TELEGRAM_WEB_HOOK = "$t('webHook')";
    static final String NAME_TELEGRAM_WEB_HOOK = "webHook";

    static final String TELEGRAM_BOT_TOKEN = "botToken";
    static final String NAME_TELEGRAM_BOT_TOKEN = "botToken";

    static final String TELEGRAM_CHAT_ID = "chatId";
    static final String NAME_TELEGRAM_CHAT_ID = "chatId";

    static final String TELEGRAM_PARSE_MODE = "parseMode";
    static final String NAME_TELEGRAM_PARSE_MODE = "parseMode";

    static final String TELEGRAM_PROXY_ENABLE = "$t('isEnableProxy')";
    static final String NAME_TELEGRAM_PROXY_ENABLE = "IsEnableProxy";

    static final String TELEGRAM_PROXY = "$t('proxy')";
    static final String NAME_TELEGRAM_PROXY = "Proxy";

    static final String TELEGRAM_PORT = "$t('port')";
    static final String NAME_TELEGRAM_PORT = "Port";

    static final String TELEGRAM_USER = "$t('user')";
    static final String NAME_TELEGRAM_USER = "User";

    static final String TELEGRAM_PASSWORD = "$t('password')";
    static final String NAME_TELEGRAM_PASSWORD = "Password";

    private TelegramParamsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
