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
