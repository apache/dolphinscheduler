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

package org.apache.dolphinscheduler.common.utils;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.conf.HiveConf.ConfVars;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * hive conf utils
 */
public class HiveConfUtils {

    private HiveConfUtils() {
        throw new UnsupportedOperationException("Construct HiveConfUtils");
    }

    private static class HiveConfHandler {
        private static HiveConf singleton;

        private static Map<String,Object> hiveConfVars;

        static {
            singleton = new HiveConf();
            hiveConfVars = new HashMap<>();
            Arrays.stream(ConfVars.values()).forEach(confVar -> hiveConfVars.put(confVar.varname,confVar));
        }
    }

    /**
     * get HiveConf instance
     * @return HiveConf hiveConf
     */
    public static HiveConf getInstance() {
        return HiveConfHandler.singleton;
    }

    /**
     * get hive conf vars
     * @return
     */
    public static Map<String,Object> getHiveConfVars() {
        return HiveConfHandler.hiveConfVars;
    }

    /**
     * Determine if it belongs to a hive conf property
     * @param conf config
     * @return boolean result
     */
    public static boolean isHiveConfVar(String conf) {
        // the default hive conf var name
        String confKey = conf.split("=")[0];
        Map<String, Object> hiveConfVars = HiveConfUtils.getHiveConfVars();
        if (hiveConfVars.get(confKey) != null) {
            return true;
        }

        // the security authorization hive conf var name
        HiveConf hiveConf = HiveConfUtils.getInstance();
        String hiveAuthorizationSqlStdAuthConfigWhitelist = hiveConf.getVar(HiveConf.ConfVars.HIVE_AUTHORIZATION_SQL_STD_AUTH_CONFIG_WHITELIST);
        Pattern modWhiteListPattern = Pattern.compile(hiveAuthorizationSqlStdAuthConfigWhitelist);
        Matcher matcher = modWhiteListPattern.matcher(confKey);
        return matcher.matches();
    }

}
