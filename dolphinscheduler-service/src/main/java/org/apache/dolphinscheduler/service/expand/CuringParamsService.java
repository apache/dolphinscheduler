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

package org.apache.dolphinscheduler.service.expand;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

public interface CuringParamsService {

    /**
     * time function need expand
     * @param placeholderName
     * @return
     */
    boolean timeFunctionNeedExpand(String placeholderName);

    /**
     * time function extension
     * @param processInstanceId
     * @param timezone
     * @param placeholderName
     * @return
     */
    String timeFunctionExtension(Integer processInstanceId, String timezone, String placeholderName);

    /**
     * convert parameter placeholders
     * @param val
     * @param allParamMap
     * @return
     */
    String convertParameterPlaceholders(String val, Map<String, String> allParamMap);

    /**
     * curing global params
     * @param processInstanceId
     * @param globalParamMap
     * @param globalParamList
     * @param commandType
     * @param scheduleTime
     * @param timezone
     * @return
     */
    String curingGlobalParams(Integer processInstanceId, Map<String, String> globalParamMap,
                              List<Property> globalParamList, CommandType commandType, Date scheduleTime,
                              String timezone);

    /**
     * param parsing preparation
     * @param parameters
     * @param taskInstance
     * @param processInstance
     * @return
     */
    Map<String, Property> paramParsingPreparation(@NonNull TaskInstance taskInstance,
                                                  @NonNull AbstractParameters parameters,
                                                  @NonNull ProcessInstance processInstance);

    /**
     * preBuildBusinessParams
     * @param processInstance
     * @return
     */
    Map<String, Property> preBuildBusinessParams(ProcessInstance processInstance);

    Map<String, Property> getProjectParameterMap(long projectCode);
}
