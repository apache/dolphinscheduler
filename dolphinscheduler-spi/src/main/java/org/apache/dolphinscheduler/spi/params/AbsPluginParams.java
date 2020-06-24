package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static java.util.Objects.requireNonNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextParam.class, name = "_type"),
        @JsonSubTypes.Type(value = RadioParam.class, name = "_type"),
})
public class AbsPluginParams {

    /**
     * values can be select
     */
    private String name;

    protected AlpacajsSchema alpacajsSchema;

    protected AlpacajsOptions alpacajsOptions;

    public AbsPluginParams(String name) {
        requireNonNull(name , "name is null");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("schema")
    public AlpacajsSchema getAlpacajsSchema() {
        return alpacajsSchema;
    }

    public AlpacajsOptions getAlpacajsOptions() {
        return alpacajsOptions;
    }

}
