package org.apache.dolphinscheduler.spi.params.base;

/**
 * param datetype
 */
public enum DataType {

    STRING("string"),

    NUMBER("number");

    private String dataType;

    DataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return this.dataType;
    }

}
