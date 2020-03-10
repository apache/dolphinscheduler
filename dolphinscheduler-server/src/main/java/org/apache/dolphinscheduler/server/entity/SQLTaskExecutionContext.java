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

package org.apache.dolphinscheduler.server.entity;

import org.apache.dolphinscheduler.dao.entity.UdfFunc;

import java.io.Serializable;
import java.util.List;

/**
 *  SQL Task ExecutionContext
 */
public class SQLTaskExecutionContext implements Serializable {


    /**
     * warningGroupId
     */
    private int warningGroupId;

    /**
     * connectionParams
     */
    private String connectionParams;
    /**
     * udf function list
     */
    private List<UdfFunc> udfFuncList;


    public int getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(int warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public List<UdfFunc> getUdfFuncList() {
        return udfFuncList;
    }

    public void setUdfFuncList(List<UdfFunc> udfFuncList) {
        this.udfFuncList = udfFuncList;
    }

    public String getConnectionParams() {
        return connectionParams;
    }

    public void setConnectionParams(String connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public String toString() {
        return "SQLTaskExecutionContext{" +
                "warningGroupId=" + warningGroupId +
                ", connectionParams='" + connectionParams + '\'' +
                ", udfFuncList=" + udfFuncList +
                '}';
    }
}
