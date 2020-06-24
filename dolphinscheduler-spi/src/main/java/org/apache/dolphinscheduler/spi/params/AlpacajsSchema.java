package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Base on alpacajs, this class define the alpacajs schema params
 */
public class AlpacajsSchema {

    /**
     * alpacajs schema default value
     */
    private String defaultValue;

    /**
     * the values can be select
     */
    private List<String> enumValues;

    /**
     * data type (string,number...)
     */
    private String type;

    /**
     * param title (label)
     */
    private String title;

    /**
     * is required
     */
    private boolean required;

    @JsonProperty("default")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @JsonProperty("enum")
    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
