package org.apache.dolphinscheduler.remote.command.listener;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.ResponseMessageBuilder;

import lombok.Data;
import lombok.ToString;

/**
 * @author wxn
 * @date 2023/7/31
 */
@Data
@ToString
public class ListenerResponse<T> implements ResponseMessageBuilder {

    private boolean success;
    private String message;
    private T data;
    @Override
    public MessageType getCommandType() {
        return MessageType.RESPONSE;
    }

    public ListenerResponse() {
    }

    public ListenerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ListenerResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ListenerResponse<T> success() {
        return new ListenerResponse<>(true, "成功", null);
    }

    public static <T> ListenerResponse<T> success(T data) {
        return new ListenerResponse<>(true, "成功", data);
    }

    public static <T> ListenerResponse<T> fail(String msg) {
        return new ListenerResponse<>(false, msg, null);
    }
}
