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

package org.apache.dolphinscheduler.spi.params.checkbox;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * front-end checkbox component props attributes
 */
public class CheckboxParamProps extends ParamsProps {

    /**
     * the minimum number of checkboxes that can be checked
     */
    private Integer min;

    /**
     * the maximum number of checkboxes that can be checked
     */
    private Integer max;

    /**
     * the color of the text when the Checkbox in the form of a button is activated
     */
    private String textColor;

    /**
     * the fill color and border color of the Checkbox in the form of a button when activated
     */
    private String fill;

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

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }
}
