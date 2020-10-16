package org.apache.dolphinscheduler.plugin.alert.wechat;

/**
 * WeChatAlertConstants
 */
public class WeChatAlertConstants {

    public static final String MARKDOWN_QUOTE = ">";

    public static final String MARKDOWN_ENTER = "\n";

    public static final String CHARSET = "UTF-8";

    public static final Boolean enable=Boolean.parseBoolean(System.getProperty("enterprise.wechat.enable","false"));
}
