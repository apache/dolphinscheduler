package org.apache.dolphinscheduler.alert.api;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public enum AlertInputTips {

    PASSWORD( "if enable use authentication, you need input password", "如果开启鉴权校验，则需要输入密码"),
    USERNAME( "if enable use authentication, you need input user", "如果开启鉴权校验，则需要输入账号"),
    RECEIVERS("please input receivers", "请输入收件人"),
    RECEIVERS("please input receivers", "请输入收件人"),
    ;

    private final String enMsg;
    private final String zhMsg;

    AlertInputTips(String enMsg, String zhMsg) {
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}
