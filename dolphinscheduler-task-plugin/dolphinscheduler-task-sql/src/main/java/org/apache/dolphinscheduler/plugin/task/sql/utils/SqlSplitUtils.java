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

package org.apache.dolphinscheduler.plugin.task.sql.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.CharMatcher;

public class SqlSplitUtils {

    private static final String UNIX_SQL_SEPARATOR = ";\n";
    private static final String WINDOWS_SQL_SEPARATOR = ";\r\n";

    /**
     * remove trailing ; from a sql sentence
     */
    public static String removeTrailingSemicolons(String sql) {
        return CharMatcher.is(';').trimTrailingFrom(sql);
    }

    /**
     * split sql to submit sql.
     * e.g.
     * <pre>
     *     select * from table1\n;select * from table2\n;select * from table2\r\n;
     * </pre>
     * will be split to
     * <pre>
     *     select * from table1
     *     select * from table2
     * </pre>
     */
    public static List<String> splitSql(String sql) {

        return Arrays.stream(sql.replaceAll(WINDOWS_SQL_SEPARATOR, UNIX_SQL_SEPARATOR).split(UNIX_SQL_SEPARATOR))
                .map(subSql -> removeTrailingSemicolons(subSql.trim()))
                .filter(subSql -> !subSql.isEmpty() && !subSql.startsWith("--"))
                .collect(Collectors.toList());
    }

}
