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

import org.apache.dolphinscheduler.api.dto.datasource.ClickHouseDatasourceParamDTO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Map;

public class ClickHouseDatasourceProcessor extends AbstractDatasourceProcessor<ClickHouseDatasourceParamDTO> {

    @Override
    public String buildConnectionParams(ClickHouseDatasourceParamDTO datasourceParam) {
        String address = String.format("%s%s:%s/", Constants.JDBC_CLICKHOUSE, datasourceParam.getHost(), datasourceParam.getPort());
        String jdbcUrl = address + "/" + datasourceParam.getDatabase();
        String separator = "&";

        Map<String, Object> parameterMap = buildCommonParamMap(address, jdbcUrl, datasourceParam);
        String otherStr = transformOther(datasourceParam.getOther(), datasourceParam.getType(), separator);
        if (otherStr != null) {
            parameterMap.put(Constants.OTHER, otherStr);
        }

        return JSONUtils.toJsonString(parameterMap);
    }
}
