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

package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlSplitter {

    private SqlSplitter() {
    }

    private static final String LINE_SEPARATOR = "\n";

    /**
     * split sql by segment separator
     * <p>The segment separator is used
     * when the data source does not support multi-segment SQL execution,
     * and the client needs to split the SQL and execute it multiple times.</p>
     * @param sql
     * @param segmentSeparator
     * @return
     */
    public static List<String> split(String sql, String segmentSeparator) {
        if (StringUtils.isBlank(segmentSeparator)) {
            return Collections.singletonList(sql);
        }

        String[] lines = sql.split(LINE_SEPARATOR);
        List<String> segments = new ArrayList<>();
        StringBuilder stmt = new StringBuilder();
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("--")) {
                continue;
            }
            stmt.append(LINE_SEPARATOR).append(line);
            if (line.trim().endsWith(segmentSeparator)) {
                segments.add(stmt.toString());
                stmt.setLength(0);
            }
        }
        if (stmt.length() > 0) {
            segments.add(stmt.toString());
        }
        return segments;
    }
}
