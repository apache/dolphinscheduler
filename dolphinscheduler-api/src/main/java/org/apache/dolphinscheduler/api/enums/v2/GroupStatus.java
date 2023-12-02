package org.apache.dolphinscheduler.api.enums.v2;

import org.apache.dolphinscheduler.api.enums.Status;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Optional;

public enum GroupStatus {
    NOT_ALLOW_TO_DELETE_DEFAULT_ALARM_GROUP(130030, "Not allow to delete the default alarm group ", "不能删除默认告警组"),
    NOT_ALLOW_TO_UPDATE_GLOBAL_ALARM_GROUP(130032, "Not allow to update the global alert group ", "不能更新全局告警组"),
    ;
    private final int code;
    private final String enMsg;
    private final String zhMsg;

    GroupStatus(int code, String enMsg, String zhMsg) {
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
