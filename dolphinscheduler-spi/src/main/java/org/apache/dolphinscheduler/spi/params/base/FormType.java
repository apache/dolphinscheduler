package org.apache.dolphinscheduler.spi.params.base;

public enum FormType {

    INPUT("input"),

    RADIO("radio");

    private String formType;

    FormType(String formType) {
        this.formType = formType;
    }

    public String getFormType() {
        return this.formType;
    }
}
