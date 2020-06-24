package org.apache.dolphinscheduler.spi.params;

public enum FormType {

    TEXT("text"),

    RADIO("radio"),

    PASSWORD("password");

    private String formType;

    FormType(String formType) {
        this.formType = formType;
    }

    public String getFormType() {
        return this.formType;
    }
}
