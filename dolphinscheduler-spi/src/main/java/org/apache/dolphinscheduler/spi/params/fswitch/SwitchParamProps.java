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

package org.apache.dolphinscheduler.spi.params.fswitch;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

/**
 * front-end switch component props attributes
 */
public class SwitchParamProps extends ParamsProps {

    /**
     * the width of the switch (pixels)
     */
    private Integer width;

    /**
     * the class name of the icon displayed when the switch is turned on, setting this option will ignore active-text
     */
    private String activeIconClass;

    /**
     * the class name of the icon displayed when the switch is closed, setting this option will ignore inactive-text
     */
    private String inactiveIconClass;

    /**
     * text description when switch is turned on
     */
    private String activeText;

    /**
     * text description when switch is closed
     */
    private String inactiveText;

    /**
     * value when switch is turned on
     */
    private Object activeValue;

    /**
     * value when the switch is closed
     */
    private Object inactiveValue;

    /**
     * the background color when the switch is turned on
     */
    private String activeColor;

    /**
     * the background color when the switch is closed
     */
    private String inactiveColor;

    /**
     * name attribute
     */
    private String name;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getActiveIconClass() {
        return activeIconClass;
    }

    public void setActiveIconClass(String activeIconClass) {
        this.activeIconClass = activeIconClass;
    }

    public String getInactiveIconClass() {
        return inactiveIconClass;
    }

    public void setInactiveIconClass(String inactiveIconClass) {
        this.inactiveIconClass = inactiveIconClass;
    }

    public String getActiveText() {
        return activeText;
    }

    public void setActiveText(String activeText) {
        this.activeText = activeText;
    }

    public String getInactiveText() {
        return inactiveText;
    }

    public void setInactiveText(String inactiveText) {
        this.inactiveText = inactiveText;
    }

    public Object getActiveValue() {
        return activeValue;
    }

    public void setActiveValue(Object activeValue) {
        this.activeValue = activeValue;
    }

    public Object getInactiveValue() {
        return inactiveValue;
    }

    public void setInactiveValue(Object inactiveValue) {
        this.inactiveValue = inactiveValue;
    }

    public String getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(String activeColor) {
        this.activeColor = activeColor;
    }

    public String getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(String inactiveColor) {
        this.inactiveColor = inactiveColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
