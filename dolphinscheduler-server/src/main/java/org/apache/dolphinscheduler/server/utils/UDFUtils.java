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
package org.apache.dolphinscheduler.server.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.common.utils.CollectionUtils.isNotEmpty;

/**
 *  udf utils
 */
public class UDFUtils {

    /**
     *  create function format
     */
    private static final String CREATE_FUNCTION_FORMAT = "create temporary function {0} as ''{1}''";

    /**
     * create function list
     * @param udfFuncTenantCodeMap  key is udf function,value is tenant code
     * @param logger                logger
     * @return create function list
     */
    public static List<String> createFuncs(Map<UdfFunc,String> udfFuncTenantCodeMap, Logger logger){

        if (MapUtils.isEmpty(udfFuncTenantCodeMap)){
            logger.info("can't find udf function resource");
            return null;
        }
        List<String> funcList = new ArrayList<>();

        // build jar sql
        buildJarSql(funcList, udfFuncTenantCodeMap);

        // build temp function sql
        buildTempFuncSql(funcList, udfFuncTenantCodeMap.keySet().stream().collect(Collectors.toList()));

        return funcList;
    }

    /**
     * build jar sql
     * @param sqls                  sql list
     * @param udfFuncTenantCodeMap  key is udf function,value is tenant code
     */
    private static void buildJarSql(List<String> sqls, Map<UdfFunc,String> udfFuncTenantCodeMap) {
        String defaultFS = HadoopUtils.getInstance().getConfiguration().get(Constants.FS_DEFAULTFS);
        String resourceFullName;
        Set<Map.Entry<UdfFunc,String>> entries = udfFuncTenantCodeMap.entrySet();
        for (Map.Entry<UdfFunc,String> entry:entries){
            String uploadPath = HadoopUtils.getHdfsUdfDir(entry.getValue());
            if (!uploadPath.startsWith("hdfs:")) {
                uploadPath = defaultFS + uploadPath;
            }
            resourceFullName = entry.getKey().getResourceName();
            resourceFullName = resourceFullName.startsWith("/") ? resourceFullName : String.format("/%s",resourceFullName);
            sqls.add(String.format("add jar %s%s", uploadPath, resourceFullName));
        }

    }

    /**
     * build temp function sql
     * @param sqls      sql list
     * @param udfFuncs  udf function list
     */
    private static void buildTempFuncSql(List<String> sqls, List<UdfFunc> udfFuncs) {
        if (isNotEmpty(udfFuncs)) {
            for (UdfFunc udfFunc : udfFuncs) {
                sqls.add(MessageFormat
                        .format(CREATE_FUNCTION_FORMAT, udfFunc.getFuncName(), udfFunc.getClassName()));
            }
        }
    }


}
