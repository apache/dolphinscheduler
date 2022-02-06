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

package org.apache.dolphinscheduler.spi.params.input.number;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * front-end input number component props attributes
 */
public class InputNumberParamProps extends ParamsProps {

    /**
     * set the minimum value allowed by the counter
     */
    private Integer min;

    /**
     * set the maximum value allowed by the counter
     */
    private Integer max;

    /**
     * counter step
     */
    private Integer step;

    /**
     * numerical accuracy
     */
    private Integer precision;

    /**
     * whether to use the control button, the default value is true
     */
    private Boolean controls;

    /**
     * control button position, the default value is right
     */
    private String controlsPosition;

    /**
     * name attribute
     */
    private String name;

    /**
     * the label text associated with the input box
     */
    private String label;

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    public Boolean getControls() {
        return controls;
    }

    public void setControls(Boolean controls) {
        this.controls = controls;
    }

    public String getControlsPosition() {
        return controlsPosition;
    }

    public void setControlsPosition(String controlsPosition) {
        this.controlsPosition = controlsPosition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
