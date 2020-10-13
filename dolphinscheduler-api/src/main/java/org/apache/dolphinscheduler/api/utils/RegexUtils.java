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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface AlertPluginInstanceMapper extends BaseMapper<AlertPluginInstance> {

    /**
     * query all alert plugin instance
     *
     * @return AlertPluginInstance list
     */
    List<AlertPluginInstance> queryAllAlertPluginInstanceList();

    /**
     * query by alert group id
     *
     * @param alertGroupId
     * @return AlertPluginInstance list
     */
    List<AlertPluginInstance> queryByAlertGroupId(@Param("alertGroupId") int alertGroupId);

package org.apache.dolphinscheduler.api.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is Regex expression utils.
 */
public class RegexUtils {

    /**
     * check number regex expression
     */
    private static final String CHECK_NUMBER = "^-?\\d+(\\.\\d+)?$";

    private RegexUtils() {
    }

    /**
     * check if the input is number
     *
     * @param str input
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile(CHECK_NUMBER);
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
