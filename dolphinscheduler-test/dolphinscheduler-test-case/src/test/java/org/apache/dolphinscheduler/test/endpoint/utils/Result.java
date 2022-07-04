package org.apache.dolphinscheduler.test.endpoint.utils;


import java.text.MessageFormat;

public class Result<T> {
    /**
     * status
     */
    private Integer code;

    /**
     * message
     */
    private String msg;

    /**
     * data
     */
    private T data;

    private String failed;
    private String success;

    public String getFailed() {
        return failed;
    }

    public String getSuccess() {
        return success;
    }

    public Result() {
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Result(Status status) {
        if (status != null) {
            this.code = status.getCode();
            this.msg = status.getMsg();
        }
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * Call this function if there is success
     *
     * @param data data
     * @param <T> type
     * @return resule
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data);
    }

    public static Result success() {
        return success(null);
    }

    public boolean isSuccess() {
        return this.isStatus(Status.SUCCESS);
    }

    public boolean isFailed() {
        return !this.isSuccess();
    }

    public boolean isStatus(Status status) {
        return this.code != null && this.code.equals(status.getCode());
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @return result
     */
    public static <T> Result<T> error(Status status) {
        return new Result<>(status);
    }

    /**
     * Call this function if there is any error
     *
     * @param status status
     * @param args args
     * @return result
     */
    public static <T> Result<T> errorWithArgs(Status status, Object... args) {
        return new Result<>(status.getCode(), MessageFormat.format(status.getMsg(), args));
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Status{"
                + "code='" + code
                + '\'' + ", msg='"
                + msg + '\''
                + ", data=" + data
                + '}';
    }

    public Boolean checkResult() {
        return this.code == Status.SUCCESS.getCode();
    }
}
