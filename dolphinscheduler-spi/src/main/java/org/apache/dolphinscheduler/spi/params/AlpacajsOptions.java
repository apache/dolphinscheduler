package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Base on alpacajs, this class define the alpacajs options params
 */
public class AlpacajsOptions {

    /**
     * alpacajs option type (text,radio,checkbox...)
     */
    private String type;

    /**
     * param placeholder
     */
    private String placeholder;

    /**
     * is readOnly
     */
    private boolean readOnly;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @JsonProperty("readonly")
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
