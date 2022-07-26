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

package org.apache.dolphinscheduler.api.dto;

import lombok.Getter;
import lombok.Setter;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.Map;

@Getter
@Setter
public class CreateWorkflowDefinitionResponse extends Result {

    private ProcessDefinition data;

    public CreateWorkflowDefinitionResponse(Map<String, Object> result) {
        super();
        this.setCode(((Status) result.get(Constants.STATUS)).getCode());
        this.setData(result.get(Constants.DATA_LIST));
        this.setMsg((String) result.get(Constants.MSG));
    }

}
