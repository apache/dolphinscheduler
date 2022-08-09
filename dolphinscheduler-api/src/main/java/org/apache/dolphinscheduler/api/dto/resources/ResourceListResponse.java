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

package org.apache.dolphinscheduler.api.dto.resources;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;

import java.util.List;
import java.util.Map;

/**
 * resource list response
 */
public class ResourceListResponse extends Result<List<ResourceComponent>> {

    private List<ResourceComponent> data;

    public ResourceListResponse(Result<Object> result) {
        super();
        this.setCode(result.getCode());
        this.setMsg(result.getMsg());
        this.setData((List<ResourceComponent>) result.getData());
    }

    public ResourceListResponse(Map<String, Object> result) {
        super();
        Status status = (Status) result.get(Constants.STATUS);
        if (null != status) {
            this.setCode(status.getCode());
        }
        this.setMsg((String) result.get(Constants.MSG));
        this.setData((List<ResourceComponent>) result.get(Constants.DATA_LIST));
    }

    @Override
    public List<ResourceComponent> getData() {
        return data;
    }

    @Override
    public void setData(List<ResourceComponent> data) {
        this.data = data;
    }
}
