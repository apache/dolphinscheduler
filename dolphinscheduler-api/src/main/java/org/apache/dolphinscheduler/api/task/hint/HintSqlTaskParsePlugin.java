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

package org.apache.dolphinscheduler.api.task.hint;

import org.apache.dolphinscheduler.api.task.SqlTaskParseContext;
import org.apache.dolphinscheduler.api.task.SqlTaskParsePlugin;
import org.apache.dolphinscheduler.api.task.SqlTaskParseResult;

import java.util.Collections;

public class HintSqlTaskParsePlugin implements SqlTaskParsePlugin {

    @Override
    public String name() {
        return "hint";
    }

    @Override
    public SqlTaskParseResult parse(SqlTaskParseContext context) {
        SqlTaskParseResult result = new SqlTaskParseResult();
        result.setUpstreamSet(context.hint(HintEnum.UPSTREAM.getKey()));
        result.setDownstreamSet(Collections.singleton(context.getTaskName()));
        return result;
    }
}
