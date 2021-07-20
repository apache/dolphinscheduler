package org.apache.dolphinscheduler.common.enums;

import org.apache.dolphinscheduler.common.utils.StringUtils;

public enum FlinkxMode {

    /**
     * 本地模式运行
     */
    local(0 , "local"),

    /**
     * flink集群 standalone模式
     */
    standalone(1, "standalone"),

    /**
     * 在已经启动在yarn上的flink session里上运行
     */
    yarn(2, "yarn"),

    /**
     * 在yarn上单独启动flink session运行
     */
    yarnPer(3, "yarnPer");

    private int type;

    private String name;

    FlinkxMode(int type, String name){
        this.type = type;
        this.name = name;
    }

    public static FlinkxMode getByName(String name){
        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("ClusterMode name cannot be null or empty");
        }
        switch (name){
            case "standalone": return standalone;
            case "yarn": return yarn;
            case "yarnPer": return yarnPer;
            default: return local;
        }
    }

}
