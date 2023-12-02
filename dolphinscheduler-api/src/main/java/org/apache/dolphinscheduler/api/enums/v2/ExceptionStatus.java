package org.apache.dolphinscheduler.api.enums.v2;

import org.apache.dolphinscheduler.api.enums.Status;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

public enum ExceptionStatus {
    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),

    /*token error*/
    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    QUERY_ACCESSTOKEN_BY_USER_ERROR(70016, "query access token by user error", "查询访问指定用户的token错误"),

    CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
    QUERY_ALL_ALERTGROUP_ERROR(10028, "query all alertgroup error", "查询告警组错误"),
    LIST_PAGING_ALERT_GROUP_ERROR(10029, "list paging alert group error", "分页查询告警组错误"),
    UPDATE_ALERT_GROUP_ERROR(10030, "update alert group error", "更新告警组错误"),
    DELETE_ALERT_GROUP_ERROR(10031, "delete alert group error", "删除告警组错误"),
    QUERY_ALERT_GROUP_ERROR(10180, "query alert group error", "查询告警组错误"),

    UPDATE_ALERT_PLUGIN_INSTANCE_ERROR(110005, "update alert plugin instance error", "更新告警组和告警组插件实例错误"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR(110006, "delete alert plugin instance error", "删除告警组和告警组插件实例错误"),
    GET_ALERT_PLUGIN_INSTANCE_ERROR(110007, "get alert plugin instance error", "获取告警组和告警组插件实例错误"),
    CREATE_ALERT_PLUGIN_INSTANCE_ERROR(110008, "create alert plugin instance error", "创建告警组和告警组插件实例错误"),
    QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR(110009, "query all alert plugin instance error", "查询所有告警实例失败"),
    LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR(110011, "query plugin instance page error", "分页查询告警实例失败"),
    SEND_TEST_ALERT_PLUGIN_INSTANCE_ERROR(110016, "send test alert plugin instance error", "发送测试告警错误"),
    ;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    ExceptionStatus (int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }

    /**
     * Retrieve Status enum entity by status code.
     */
    public static Optional<Status> findStatusBy(int code) {
        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
