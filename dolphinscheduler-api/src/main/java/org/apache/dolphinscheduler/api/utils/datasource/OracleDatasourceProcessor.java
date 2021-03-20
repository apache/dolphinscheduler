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

import org.apache.dolphinscheduler.api.dto.datasource.OracleDatasourceParamDTO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleDatasourceProcessor extends AbstractDatasourceProcessor<OracleDatasourceParamDTO> {

    private static final Logger logger = LoggerFactory.getLogger(OracleDatasourceProcessor.class);

    @Override
    public String buildConnectionParams(OracleDatasourceParamDTO datasourceParam) {
        String address;
        if (DbConnectType.ORACLE_SID.equals(datasourceParam.getConnectType())) {
            address = String.format("%s%s:%s",
                    Constants.JDBC_ORACLE_SID, datasourceParam.getHost(), datasourceParam.getPort());
        } else {
            address = String.format("%s%s:%s",
                    Constants.JDBC_ORACLE_SERVICE_NAME, datasourceParam.getHost(), datasourceParam.getPort());
        }
        String jdbcUrl = address + "/" + datasourceParam.getDatabase();
        String separator = "&";

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put(Constants.ORACLE_DB_CONNECT_TYPE, datasourceParam.getConnectType());
        parameterMap.put(TYPE, datasourceParam.getConnectType());
        parameterMap.putAll(buildCommonParamMap(address, jdbcUrl, datasourceParam));

        String otherStr = transformOther(datasourceParam.getOther(), datasourceParam.getType(), separator);
        if (otherStr != null) {
            parameterMap.put(Constants.OTHER, otherStr);
        }
        return JSONUtils.toJsonString(parameterMap);
    }
}
