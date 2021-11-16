package org.apache.dolphinscheduler.plugin.alert.ewechat;

public final class EWeChatAlertConstants {
    private EWeChatAlertConstants() {
        throw new IllegalStateException(EWeChatAlertConstants.class.getName());
    }

    static final String COLON = "：";

    static final String SUCCESS_FLAG = "success";

    static final String MARKDOWN_QUOTE = ">";

    static final String MARKDOWN_ENTER = "\n";

    static final String CHARSET = "UTF-8";

    static final String FIRST_DASH = "-----------------------------------------------------------------\n";

    static final String DASH_BREAK = "-----------------------------------------------------------------\n";

    static final String BOLD_PATTERN = "**%s**";

    static final String INFO_PATTERN = "# <font color=\"info\">%s</font>%n";

    static final String ERROR_PATTERN = "# `%s`%n";

    static final String COMMENT_PATTERN = "<font color=\"comment\">%s</font>%n";

}
