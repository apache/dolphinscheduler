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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @param udfFuncs      udf functions
     * @param tenantCode    tenant code
     * @param logger        logger
     * @return create function list
     */
    public static List<String> createFuncs(List<UdfFunc> udfFuncs, String tenantCode,Logger logger){

        if (CollectionUtils.isEmpty(udfFuncs)){
            logger.info("can't find udf function resource");
            return null;
        }
        // get  hive udf jar path
        String hiveUdfJarPath = HadoopUtils.getHdfsUdfDir(tenantCode);
        logger.info("hive udf jar path : {}" , hiveUdfJarPath);

        // is the root directory of udf defined
        if (StringUtils.isEmpty(hiveUdfJarPath)) {
            logger.error("not define hive udf jar path");
            throw new RuntimeException("hive udf jar base path not defined ");
        }
        Set<String> resources = getFuncResouces(udfFuncs);
        List<String> funcList = new ArrayList<>();

        // build jar sql
        buildJarSql(funcList, resources, hiveUdfJarPath);

        // build temp function sql
        buildTempFuncSql(funcList, udfFuncs);

        return funcList;
    }

    /**
     * build jar sql
     * @param sqls          sql list
     * @param resources     resource set
     * @param uploadPath    upload path
     */
    private static void buildJarSql(List<String> sqls, Set<String> resources, String uploadPath) {
        String defaultFS = HadoopUtils.getInstance().getConfiguration().get(Constants.FS_DEFAULTFS);
        if (!uploadPath.startsWith("hdfs:")) {
            uploadPath = defaultFS + uploadPath;
        }

        for (String resource : resources) {
            sqls.add(String.format("add jar %s/%s", uploadPath, resource));
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

    /**
     * get the resource names of all functions
     * @param udfFuncs udf function list
     * @return
     */
    private static Set<String> getFuncResouces(List<UdfFunc> udfFuncs) {
        Set<String> resources = new HashSet<>();

        for (UdfFunc udfFunc : udfFuncs) {
            resources.add(udfFunc.getResourceName());
        }

        return resources;
    }


}
