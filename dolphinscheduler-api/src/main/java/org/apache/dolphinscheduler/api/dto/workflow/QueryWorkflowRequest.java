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

package org.apache.dolphinscheduler.api.dto.workflow;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class QueryWorkflowRequest {

    @ApiModelProperty(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1", required = true)
    private int pageNo;

    @ApiModelProperty(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "10", required = true)
    private int pageSize;

    @ApiModelProperty(name = "searchVal", value = "SEARCH_VAL", dataType = "String", required = false)
    private String searchVal;

    @ApiModelProperty(name = "otherParamsJson", value = "OTHER_PARAMS_JSON", dataType = "String", required = false)
    private String otherParamsJson;

    @ApiModelProperty(name = "userId", value = "USER_ID", dataType = "Int", example = "0", required = false)
    private Integer userId;
}
