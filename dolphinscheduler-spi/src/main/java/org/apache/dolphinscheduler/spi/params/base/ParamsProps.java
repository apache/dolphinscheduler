package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * this props field in form-create`s json rule
 */
public class ParamsProps {
    private PropsType propsType;

    private String placeholder;

    private int rows;

    @JsonProperty("type")
    public PropsType getPropsType() {
        return propsType;
    }

    public ParamsProps setPropsType(PropsType propsType) {
        this.propsType = propsType;
        return this;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public ParamsProps setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @JsonProperty("rows")
    public int getRows() {
        return rows;
    }

    public ParamsProps setRows(int rows) {
        this.rows = rows;
        return this;
    }
}
