package org.apache.dolphinscheduler.plugin.alert.webhook;

public final class WebHookAlertConstants {

    public static final String NAME_WEBHOOK_URL = "webhook";

    public static final String WEBHOOK_URL = "$t('webhook')";

    static final String CHARSET = "UTF-8";

    static final String CONTENT_KEY = "content";

    private WebHookAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
