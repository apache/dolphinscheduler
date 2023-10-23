package org.apache.dolphinscheduler.plugin.alert.feishu;

public enum FeiShuType {

    CUSTOM_ROBOT(1, "CUSTOM ROBOT(FOR GROUP CHAT)/自定义机器人(用于群聊)"),
    APPLIANCE_ROBOT(2, "APPLIANCE ROBOT(FOR PERSONAL WORK)/应用机器人(用于个人工作)"),
    ;

    private final int code;
    private final String descp;

    FeiShuType(int code, String descp) {
        this.code = code;
        this.descp = descp;
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
