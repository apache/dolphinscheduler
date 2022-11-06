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

import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskRemoteHostDTO {

    @Schema(example = "app01", description = "TASK_REMOTE_HOST_NAME", required = true)
    private String name;

    @Schema(example = "127.0.0.1", description = "TASK_REMOTE_HOST_IP", required = true)
    private String ip;

    @Schema(example = "22", implementation = int.class, description = "TASK_REMOTE_HOST_PORT", required = true)
    private Integer port;

    @Schema(example = "foo", description = "TASK_REMOTE_HOST_ACCOUNT", required = true)
    private String account;

    @Schema(example = "foo", description = "TASK_REMOTE_HOST_PASSWORD", required = true)
    private String password;

    @Schema(example = "this is a demo host", description = "TASK_REMOTE_HOST_DESC")
    private String description;

}
