package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Validate {

    private boolean required = false;

    private String message;

    private String type = DataType.STRING.getDataType();

    private String trigger = TriggerType.BLUR.getTriggerType();

    private Double min;

    private Double max;

    public static Validate buildValidate() {
        return new Validate();
    }

    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    public Validate setRequired(boolean required) {
        this.required = required;
        return this;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public Validate setMessage(String message) {
        this.message = message;
        return this;
    }

    @JsonProperty("trigger")
    public String getTrigger() {
        return trigger;
    }

    public Validate setTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    @JsonProperty("min")
    public Double getMin() {
        return min;
    }

    public Validate setMin(Double min) {
        this.min = min;
        return this;
    }

    @JsonProperty("max")
    public Double getMax() {
        return max;
    }

    public Validate setMax(Double max) {
        this.max = max;
        return this;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    public Validate setType(String type) {
        this.type = type;
        return this;
    }
}
