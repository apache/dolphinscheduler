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

package org.apache.dolphinscheduler.spi.params.select;

import org.apache.dolphinscheduler.spi.params.base.ParamsProps;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * front-end select component props attributes
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SelectParamProps extends ParamsProps {

    /**
     * whether to select multiple, the default value is false
     */
    private Boolean multiple;

    /**
     * as the key name that uniquely identifies the value, it is required when the binding value is the object type
     */
    private String valueKey;

    /**
     * input box size, optional value medium/small/mini
     */
    private String size;

    /**
     * whether the option can be cleared, the default value is false
     */
    private Boolean clearable;

    /**
     * whether to display the selected value in the form of text when multiple selections, the default value is false
     */
    private Boolean collapseTags;

    /**
     * the maximum number of items that the user can select when multiple selections are made, if it is 0, there is no limit
     */
    private Integer multipleLimit;

    /**
     * select input name attribute
     */
    private String name;

    /**
     * select input autocomplete attribute, the default value is off
     */
    private String autocomplete;

    /**
     * whether it is searchable, the default value is false
     */
    private Boolean filterable;

    /**
     * whether to allow users to create new entries, it needs to be used with filterable, the default value is false
     */
    private Boolean allowCreate;

    /**
     * the text displayed when there is no match for the search criteria
     */
    private String noMatchText;

    /**
     * the text displayed when the option is empty
     */
    private String noDataText;

    /**
     * Select the class name of the drop-down box
     */
    private String popperClass;

    /**
     * when multiple selection and searchable, whether to keep the current search keywords after selecting an option, the default value is false
     */
    private Boolean reserveKeyword;

    /**
     * press Enter in the input box to select the first match. need to be used with filterable or remote, the default value is false
     */
    private Boolean defaultFirstOption;

    /**
     * whether to insert a pop-up box into the body element. when there is a problem with the positioning of the pop-up box, this property can be set to false
     */
    private Boolean popperAppendToBody;

    /**
     * for non-searchable Select, whether to automatically pop up the option menu after the input box gets the focus, the default value is false
     */
    private Boolean automaticDropdown;

}
