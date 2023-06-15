/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.api.model;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;

import java.io.Serializable;
import java.util.Objects;

public class Parameter implements Serializable {

    private static final long serialVersionUID = -4045513703397452451L;
    /**
     * key
     */
    private String key;

    /**
     * input/output
     */
    private Direct direct;

    /**
     * data type
     */
    private DataType type;

    /**
     * value
     */
    private String value;

    public Parameter() {
    }

    public Parameter(String key, Direct direct, DataType type, String value) {
        this.key = key;
        this.direct = direct;
        this.type = type;
        this.value = value;
    }

    /**
     * getter method
     *
     * @return the key
     * @see Parameter#key
     */
    public String getKey() {
        return key;
    }

    /**
     * setter method
     *
     * @param key the key to set
     * @see Parameter#key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * getter method
     *
     * @return the value
     * @see Parameter#value
     */
    public String getValue() {
        return value;
    }

    /**
     * setter method
     *
     * @param value the value to set
     * @see Parameter#value
     */
    public void setValue(String value) {
        this.value = value;
    }

    public Direct getDirect() {
        return direct;
    }

    public void setDirect(Direct direct) {
        this.direct = direct;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
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
        Parameter parameter = (Parameter) o;
        return Objects.equals(key, parameter.key)
                && Objects.equals(value, parameter.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Parameter{"
                + "key='" + key + '\''
                + ", direct=" + direct
                + ", type=" + type
                + ", value='" + value + '\''
                + '}';
    }

}
