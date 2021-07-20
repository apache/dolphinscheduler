package org.apache.dolphinscheduler.common.process;

import java.io.Serializable;
import java.util.Objects;

public class Column implements Serializable {

    private String name;

    private String type;

    public Column() {
    }

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Column column = (Column) o;
        return Objects.equals(name, column.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
