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

package org.apache.dolphinscheduler.data.quality.configuration;

import org.apache.dolphinscheduler.data.quality.utils.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * ConfigurationParserTest
 */
public class ConfigurationParserTest {

    @Test
    public void testConfigurationValidate(){
        Assert.assertEquals(1,verifyConfigurationValidate());
    }

    private int verifyConfigurationValidate() {
        int flag = 1;
        try {
            String parameterStr = "{\"name\":\"\\u81EA\\u5B9A\\u4E49SQL\",\"connectors\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"123456\",\"driver\":\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"table\":\"test1\",\"url\":\"jdbc:mysql://localhost:3306/test\"} }],\"writers\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\",\"password\":\"Test@123!\",\"driver\":\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"table\":\"t_ds_dqs_result\",\"url\":\"jdbc:mysql://localhost:3306/dolphinscheduler?characterEncoding=UTF-8&allowMultiQueries=true\",\"sql\":\"SELECT 1 as rule_type,'\\u81EA\\u5B9A\\u4E49SQL' as rule_name,18 as process_definition_id,64 as process_instance_id,70 as task_instance_id,mySum AS statistics_value, total_count.total AS comparison_value,0 as check_type,6 as threshold, 0 as operator, 0 as failure_strategy, '2021-01-31 15:00:07' as create_time,'2021-01-31 15:00:07' as update_time from ( select sum(c4) as mySum from test1 ) tmp1 join total_count\"} }],\"executors\":[{\"index\":\"1\",\"execute.sql\":\"SELECT COUNT(*) AS total FROM test1 WHERE (c3 != '55')\",\"table.alias\":\"total_count\"}]}";
            DataQualityConfiguration dataQualityConfiguration = JsonUtil.fromJson(parameterStr,DataQualityConfiguration.class);
            dataQualityConfiguration.validate();
        } catch (Exception e) {
            flag = 0;
            e.printStackTrace();
        }
        return flag;
    }
}
