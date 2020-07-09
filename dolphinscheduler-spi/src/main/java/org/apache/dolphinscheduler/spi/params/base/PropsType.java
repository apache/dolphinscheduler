package org.apache.dolphinscheduler.spi.params.base;

public enum PropsType {

    INPUT("input"),

    PASSWORD("password"),

    TEXTAREA("textarea");

    private String propsType;

    PropsType(String propsType) {
        this.propsType = propsType;
    }

    public String getPropsType() {
        return this.propsType;
    }
}
