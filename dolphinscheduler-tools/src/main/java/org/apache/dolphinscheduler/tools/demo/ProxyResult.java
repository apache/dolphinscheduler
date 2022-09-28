package org.apache.dolphinscheduler.tools.demo;


public class ProxyResult<T> {
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

    public ProxyResult() {
    }

    public ProxyResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public ProxyResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public static <T> ProxyResult<T> success(T data) {
        return new ProxyResult<>(0, "success", data);
    }

    public static ProxyResult success() {
        return success(null);
    }
    public boolean isSuccess() {
        if( code == 0){
            return true;
        }
        return false;
    }

    public boolean isFailed() {
        return !this.isSuccess();
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

}