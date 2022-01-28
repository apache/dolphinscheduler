package org.apache.dolphinscheduler.plugin.alert.telegram;

/**
 * Telegram Alert Constants
 *
 * @author pinkhello
 */
public final class TelegramAlertConstants {

    static final String PARSE_MODE_TXT = "Txt";

    static final String PARSE_MODE_MARKDOWN = "Markdown";

    static final String PARSE_MODE_MARKDOWN_V2 = "MarkdownV2";

    static final String PARSE_MODE_HTML = "Html";

    /**
     * TELEGRAM_PUSH_URL
     *
     * <pre>
     *     https://api.telegram.org/bot{botToken}/sendMessage
     * </pre>
     */
    static final String TELEGRAM_PUSH_URL = "https://api.telegram.org/bot{botToken}/sendMessage";

    private TelegramAlertConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
