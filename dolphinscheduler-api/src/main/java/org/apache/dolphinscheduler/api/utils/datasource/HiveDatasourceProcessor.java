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

package org.apache.dolphinscheduler.api.utils.datasource;

import org.apache.dolphinscheduler.api.dto.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.api.dto.datasource.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Map;

public class HiveDatasourceProcessor extends AbstractDatasourceProcessor {

    @Override
    public String buildConnectionParams(BaseDataSourceParamDTO datasourceParam) {
        HiveDataSourceParamDTO hiveParam = (HiveDataSourceParamDTO) datasourceParam;
        StringBuilder address = new StringBuilder();
        address.append(Constants.JDBC_HIVE_2);
        for (String zkHost : hiveParam.getHost().split(",")) {
            address.append(String.format("%s:%s,", zkHost, hiveParam.getPort()));
        }
        address.deleteCharAt(address.length() - 1);
        String jdbcUrl = address.toString() + "/" + hiveParam.getDatabase();
        if (CommonUtils.getKerberosStartupState()) {
            jdbcUrl += ";principal=" + hiveParam.getPrincipal();
        }
        String separator = ";";

        Map<String, Object> parameterMap = buildCommonParamMap(address.toString(), jdbcUrl, hiveParam);
        injectKerberos(parameterMap, hiveParam);
        String otherStr = transformOther(hiveParam.getOther(), hiveParam.getType(), separator);
        if (otherStr != null) {
            parameterMap.put(OTHER, otherStr);
        }
        return JSONUtils.toJsonString(parameterMap);
    }
}
