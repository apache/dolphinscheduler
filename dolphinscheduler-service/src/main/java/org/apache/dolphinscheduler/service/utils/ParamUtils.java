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

package org.apache.dolphinscheduler.service.utils;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_FATHER_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

/**
 * Param Utility class
 */
public class ParamUtils {

    /**
     * convert globalParams string to global parameter map
     * @param globalParams  globalParams
     * @return parameter map
     */
    public static Map<String, String> getGlobalParamMap(String globalParams) {
        List<Property> propList;
        Map<String, String> globalParamMap = new HashMap<>();
        if (!Strings.isNullOrEmpty(globalParams)) {
            propList = JSONUtils.toList(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }
        return globalParamMap;
    }

    /**
     * Get sub workflow parameters
     * @param instanceMap process instance map
     * @param parentProcessInstance  parent process instance
     * @param fatherParams fatherParams
     * @return sub workflow parameters
     */
    public static String getSubWorkFlowParam(ProcessInstanceMap instanceMap, ProcessInstance parentProcessInstance,
                                             Map<String, String> fatherParams) {
        // set sub work process command
        String processMapStr = JSONUtils.toJsonString(instanceMap);
        Map<String, String> cmdParam = JSONUtils.toMap(processMapStr);
        if (parentProcessInstance.isComplementData()) {
            Map<String, String> parentParam = JSONUtils.toMap(parentProcessInstance.getCommandParam());
            String endTime = parentParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE);
            String startTime = parentParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE);
            String scheduleTime = parentParam.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
            if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_END_DATE, endTime);
                cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_START_DATE, startTime);
            }
            if (StringUtils.isNotEmpty(scheduleTime)) {
                cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, scheduleTime);
            }
            processMapStr = JSONUtils.toJsonString(cmdParam);
        }
        if (MapUtils.isNotEmpty(fatherParams)) {
            cmdParam.put(CMD_PARAM_FATHER_PARAMS, JSONUtils.toJsonString(fatherParams));
            processMapStr = JSONUtils.toJsonString(cmdParam);
        }
        return processMapStr;
    }

}
