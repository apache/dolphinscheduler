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
package cn.escheduler.server.utils;

import cn.escheduler.common.process.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql task utils
 */
public class SqlTaskUtils {
    // special characters need to be escaped, ${} needs to be escaped
    private static final String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";
    private static final Logger logger = LoggerFactory.getLogger(SqlTaskUtils.class);

    /**
     *  print replace sql
     * @param content
     * @param formatSql
     * @param sqlParamsMap
     */
    public static void printReplacedSql(String content, String formatSql, Map<Integer,Property> sqlParamsMap){
        //parameter print style
        logger.info("after replace sql , preparing : {}" , formatSql);
        StringBuffer logPrint = new StringBuffer("replaced sql , parameters:");
        for(int i=1;i<=sqlParamsMap.size();i++){
            logPrint.append(sqlParamsMap.get(i).getValue()+"("+sqlParamsMap.get(i).getType()+")");
        }
        logger.info(logPrint.toString());
    }

    /**
     * regular expressions match the contents between two specified strings
     * @param content
     * @param sqlParamsMap
     * @param paramsPropsMap
     */
    public static void setSqlParamsMap(String content, Map<Integer,Property> sqlParamsMap, Map<String,Property> paramsPropsMap){
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        int index = 1;
        while (m.find()) {

            String paramName = m.group(1);
            Property prop =  paramsPropsMap.get(paramName);

            logger.info(paramName);
            logger.info(""+m.start());
            logger.info(""+m.end());

            logger.info(content.substring(m.start(),m.end()));

            sqlParamsMap.put(index,prop);
            index ++;
        }
    }

    /**
     * get format sql
     * @param sql
     * @return
     */
    public static String getFormatSql(String sql) {
        return sql.replaceAll(rgex,"?");
    }

    /**
     * get format sql
     * @param sql
     * @param paramIndexMap
     * @return
     */
    public static String getFormatSql(String sql,Map<Integer,Boolean> paramIndexMap) {
        return replaceAll(sql,paramIndexMap);
    }

    /**
     * replace sql
     * @param sql
     * @param paramIndexMap
     * @return
     */
    public static String replaceAll(String sql,Map<Integer,Boolean> paramIndexMap) {
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(sql);
        boolean result = m.find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            do {
                m.appendReplacement(sb, "?");
                paramIndexMap.put(sb.length()-1,true);
                logger.info("{}",sb.length()-1);
                result = m.find();
            } while (result);
            m.appendTail(sb);

            return sb.toString();
        }
        return sql.toString();
    }

    /**
     * get query string to print the complete SQL statement
     * @param sqlTemplate
     * @param parameterValues
     * @return
     */
    public static String getQueryString(String sqlTemplate, ArrayList parameterValues) {
        int len = sqlTemplate.length();
        StringBuffer t = new StringBuffer(len * 2);

        if (parameterValues != null) {
            int i = 1, limit = 0, base = 0;

            while ((limit = sqlTemplate.indexOf('?', limit)) != -1) {
                logger.info("index {} value is {}",i,limit);
                t.append(sqlTemplate.substring(base, limit));
                t.append(parameterValues.get(i));
                i++;
                limit++;
                base = limit;
            }
            if (base < len) {
                t.append(sqlTemplate.substring(base));
            }
        }
        return t.toString();
    }

    /**
     * get query string to print the complete SQL statement
     * @param sqlTemplate
     * @param parameterValues
     * @param paramIndexMap
     * @return
     */
    public static String getQueryString(String sqlTemplate, ArrayList parameterValues,Map<Integer,Boolean> paramIndexMap) {
        int len = sqlTemplate.length();
        StringBuffer t = new StringBuffer(len * 2);

        if (parameterValues != null) {
            int i = 1, limit = 0, base = 0;
            if (parameterValues.size() > 0 && paramIndexMap.size() > 0) {
                while ((limit = sqlTemplate.indexOf('?', limit)) != -1) {
                    logger.info("index {} value is {}",i,limit);
                    if (paramIndexMap.get(limit)) {
                        t.append(sqlTemplate.substring(base, limit));
                        t.append(parameterValues.get(i));
                        i++;
                        limit++;
                        base = limit;
                    } else {
                        limit ++;
                    }

                }
            }

            if (base < len) {
                t.append(sqlTemplate.substring(base));
            }
        }
        return t.toString();
    }
}
