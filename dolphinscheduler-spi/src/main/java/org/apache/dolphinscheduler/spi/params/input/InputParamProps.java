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

package org.apache.dolphinscheduler.spi.params.input;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;
import org.apache.dolphinscheduler.spi.params.base.ResizeType;

import lombok.Data;

/**
 * front-end input component props attributes
 */
@Data
public class InputParamProps extends ParamsProps {

    /**
     * input type
     */
    private String type;

    /**
     * maximum input length
     */
    private Integer maxlength;

    /**
     * minimum input length
     */
    private Integer minlength;

    /**
     * whether it can be cleared, the default value is false
     */
    private Boolean clearable;

    /**
     * input box head icon
     */
    private String prefixIcon;

    /**
     * input box end icon
     */
    private String suffixIcon;

    /**
     * number of lines in the input box, only valid for type="textarea"
     */
    private Integer rows;

    /**
     * adaptive content height, only valid for type="textarea", objects can be passed in, such as {minRows: 2, maxRows: 6}
     */
    private Object autosize;

    /**
     * autocomplete attribute:on, off
     */
    private String autocomplete;

    /**
     * name attribute
     */
    private String name;

    /**
     * whether it is read-only, the default value is false
     */
    private Boolean readonly;

    /**
     * set maximum
     */
    private Integer max;

    /**
     * set minimum
     */
    private Integer min;

    /**
     * set the legal number interval of the input field
     */
    private Integer step;

    /**
     * control whether it can be zoomed by the user, the value is none, both, horizontal, vertical
     */
    private ResizeType resize;

    /**
     * get focus automatically, the default value is false
     */
    private Boolean autofocus;

    private String form;

    /**
     * the label text associated with the input box
     */
    private String label;

    /**
     * tabindex of the input box
     */
    private String tabindex;

    /**
     * whether to trigger the verification of the form during input, the default value is true
     */
    private Boolean validateEvent;

    /**
     * whether to display the switch password icon
     */
    private Boolean showPassword;

}
