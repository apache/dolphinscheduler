package org.apache.dolphinscheduler.spi.params;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Atest {

    @JsonProperty("name")
    private String name;

    @JsonProperty
    private int age;

    public Atest(String name, int age) {
        this.age = age;
        this.name = name;
    }
}
