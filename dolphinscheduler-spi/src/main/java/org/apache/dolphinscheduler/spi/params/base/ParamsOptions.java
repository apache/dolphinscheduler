package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The options field in form-create`s json rule
 * Set radio, select, checkbox and other component option options
 *
 */
public class ParamsOptions {

    private String label;

    private Object value;

    /**
     * is can be select
     */
    private boolean disabled;

    public ParamsOptions(String label, Object value, boolean disabled) {
        this.label = label;
        this.value = value;
        this.disabled = disabled;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public ParamsOptions setLabel(String label) {
        this.label = label;
        return this;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public ParamsOptions setValue(Object value) {
        this.value = value;
        return this;
    }

    @JsonProperty("disabled")
    public boolean isDisabled() {
        return disabled;
    }

    public ParamsOptions setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
