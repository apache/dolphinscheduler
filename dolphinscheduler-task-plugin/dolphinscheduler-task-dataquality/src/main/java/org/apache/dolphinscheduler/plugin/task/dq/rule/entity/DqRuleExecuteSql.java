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

package org.apache.dolphinscheduler.plugin.task.dq.rule.entity;

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DqRuleExecuteSql implements Serializable {

    /**
     * primary key
     */
    private Integer id;
    /**
     * index，ensure the execution order of sql
     */
    private int index;
    /**
     * SQL Statement
     */
    private String sql;
    /**
     * table alias name
     */
    private String tableAlias;
    /**
     * input entry type: default,statistics,comparison,check
     */
    private int type = ExecuteSqlType.MIDDLE.getCode();
    /**
     * is error output sql
     */
    private boolean isErrorOutputSql;
    /**
     * create_time
     */
    private Date createTime;
    /**
     * update_time
     */
    private Date updateTime;
}
