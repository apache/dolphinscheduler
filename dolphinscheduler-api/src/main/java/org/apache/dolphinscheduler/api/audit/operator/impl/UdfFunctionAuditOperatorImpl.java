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

package org.apache.dolphinscheduler.api.audit.operator.impl;

import org.apache.dolphinscheduler.api.audit.operator.BaseAuditOperator;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;

import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UdfFunctionAuditOperatorImpl extends BaseAuditOperator {

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Override
    protected String getObjectNameFromReturnIdentity(Object identity) {
        int objId = NumberUtils.toInt(identity.toString(), -1);
        if (objId == -1) {
            return "";
        }

        UdfFunc obj = udfFuncMapper.selectUdfById(objId);
        return obj == null ? "" : obj.getFuncName();
    }

}
