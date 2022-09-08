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

package org.apache.dolphinscheduler.plugin.task.dq.rule.entity;

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DqRuleInputEntry implements Serializable {

    /**
     * primary key
     */
    private int id;
    /**
     * form field name
     */
    private String field;
    /**
     * form type
      */
    private String type;
    /**
     * form title
     */
    private String title;
    /**
     * default value，can be null
     */
    private String value;
    /**
     * default options，can be null
     *  [{label:"",value:""}]
     */
    private String options;
    /**
     * ${field}
     */
    private String placeholder;
    /**
     * the source type of options，use default options or other
     */
    private int optionSourceType = OptionSourceType.DEFAULT.getCode();
    /**
     * input entry type: string，array，number .etc
     */
    private int valueType = ValueType.NUMBER.getCode();
    /**
     * input entry type: default,statistics,comparison
     */
    private int inputType = InputType.DEFAULT.getCode();
    /**
     * whether to display on the front end
     */
    private Boolean isShow;
    /**
     * whether to edit on the front end
     */
    private Boolean canEdit;
    /**
     * is emit event
     */
    private Boolean isEmit;
    /**
     * is validate
     */
    private Boolean isValidate;
    /**
     * values map
     */
    private String valuesMap;
    /**
     * values map
     */
    private Integer index;
    /**
     * create_time
     */
    private Date createTime;
    /**
     * update_time
     */
    private Date updateTime;
}
