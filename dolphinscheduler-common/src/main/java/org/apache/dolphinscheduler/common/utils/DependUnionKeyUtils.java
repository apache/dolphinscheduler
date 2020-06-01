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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

public class DependUnionKeyUtils {

    private static final String MARK_WORD_TABLE_DEPEND = Constants.SHARP + "table_depend";

    private static final String MARK_WORD_TABLE_TARGET = Constants.SHARP + "table_target";

    public static String clearIllegalChars(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        str = str.replace(" ", "");
        str = str.replace("\t", "");
        str = str.replace("\n", "");
        str = str.replace("，", ",");
        return str;
    }

    public static String removeMarkWord(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        str = str.replace(MARK_WORD_TABLE_DEPEND, "");
        str = str.replace(MARK_WORD_TABLE_TARGET, "");
        return str;
    }

    public static String buildDependTableUnionKey(String host, String database, String tableName) {
        return buildUnionKey(host, database, tableName, MARK_WORD_TABLE_DEPEND);
    }

    public static String buildDependTableUnionKey(String host, String database, List<String> tableList) {
        return buildUnionKey(host, database, tableList, MARK_WORD_TABLE_DEPEND);
    }

    public static String buildTargetTableUnionKey(String host, String database, String tableName) {
        return buildUnionKey(host, database, tableName, MARK_WORD_TABLE_TARGET);
    }

    public static String buildTargetTableUnionKey(String host, String database, List<String> tableList) {
        return buildUnionKey(host, database, tableList, MARK_WORD_TABLE_TARGET);
    }

    public static String buildTargetTableUnionKey(String host, String database, String[] tableArray) {
        return buildUnionKey(host, database, Arrays.asList(tableArray), MARK_WORD_TABLE_TARGET);
    }

    public static String buildUnionKey(String host, String database, List<String> tableList, String markWord) {
        if (CollectionUtils.isEmpty(tableList)) {
            return null;
        }

        String[] unionKeys = new String[tableList.size()];
        for (int i = 0; i < tableList.size(); i++) {
            unionKeys[i] = buildUnionKey(host, database, tableList.get(i), markWord);
        }

        return StringUtils.join(unionKeys, Constants.COMMA);
    }

    public static String buildUnionKey(String host, String database, String tableName, String markWord) {
        if (StringUtils.isEmpty(tableName)) {
            return null;
        }

        if (tableName.indexOf(Constants.DOT) > 0) {
            database = tableName.split("\\" + Constants.DOT)[0];
            tableName = tableName.split("\\" + Constants.DOT)[1];
        }

        return String.format("%s:%s:%s%s", host, database, tableName, markWord);
    }

    public static boolean existDependRelation(TaskNode taskNode, String[] dependNodeKeys) {
        AbstractParameters parameters = TaskParametersUtils.getParameters(taskNode.getType(), taskNode.getParams());
        return existDependRelation(parameters.getTargetNodeKeys(), dependNodeKeys);
    }

    public static boolean existDependRelation(String targetNodeKey, String[] dependNodeKeys) {
        if (StringUtils.isEmpty(targetNodeKey) || dependNodeKeys == null) {
            return false;
        }

        for (String dependKey : dependNodeKeys) {
            if (targetNodeKey.indexOf(dependKey.replace(MARK_WORD_TABLE_DEPEND, MARK_WORD_TABLE_TARGET)) > -1) {
                return true;
            }
        }

        return false;
    }

    public static String[] replaceMarkWordToTarget(String[] dependNodeKeys) {
        if(dependNodeKeys == null) {
            return null;
        }

        String[] targetNodeKeys = new String[dependNodeKeys.length];
        for(int i = 0; i < dependNodeKeys.length; i++) {
            targetNodeKeys[i] = replaceMarkWordToTarget(dependNodeKeys[i]);
        }
        return targetNodeKeys;
    }

    public static String replaceMarkWordToTarget(String dependNodeKey) {
        return dependNodeKey.replace(MARK_WORD_TABLE_DEPEND, MARK_WORD_TABLE_TARGET);
    }
}
