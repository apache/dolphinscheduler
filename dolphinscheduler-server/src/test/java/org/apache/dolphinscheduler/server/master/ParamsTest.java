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
package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 *  user define param
 */
public class ParamsTest {

    private static  final Logger logger = LoggerFactory.getLogger(ParamsTest.class);


    @Test
    public void systemParamsTest()throws Exception{
        String command = "${system.biz.date}";

        // start process
        Map<String,String> timeParams = BusinessTimeUtils
                .getBusinessTime(CommandType.START_PROCESS,
                        new Date());

        command = ParameterUtils.convertParameterPlaceholders(command, timeParams);

        logger.info("start process : {}",command);


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -5);


        command = "${system.biz.date}";
        // complement data
        timeParams = BusinessTimeUtils
                .getBusinessTime(CommandType.COMPLEMENT_DATA,
                        calendar.getTime());
        command = ParameterUtils.convertParameterPlaceholders(command, timeParams);
        logger.info("complement data : {}",command);

    }

    @Test
    public void convertTest()throws Exception{
        Map<String,Property> globalParams = new HashMap<>();
        Property property = new Property();
        property.setProp("global_param");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("${system.biz.date}");
        globalParams.put("global_param",property);

        Map<String,String> globalParamsMap = new HashMap<>();
        globalParamsMap.put("global_param","${system.biz.date}");


        Map<String,Property> localParams = new HashMap<>();
        Property localProperty = new Property();
        localProperty.setProp("local_param");
        localProperty.setDirect(Direct.IN);
        localProperty.setType(DataType.VARCHAR);
        localProperty.setValue("${global_param}");
        localParams.put("local_param", localProperty);

        Map<String, Property> paramsMap = ParamUtils.convert(globalParams, globalParamsMap,
                localParams, CommandType.START_PROCESS, new Date());
        logger.info(JSON.toJSONString(paramsMap));


    }
}