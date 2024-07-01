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

package org.apache.dolphinscheduler.plugin.task.api.loop.template;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class LoopTaskYamlDefinition implements Serializable {

    // todo: support multiple services
    private LoopTaskServiceYamlDefinition service;

    @Data
    public static class LoopTaskServiceYamlDefinition implements Serializable {

        private String name;
        private String type;
        private LoopTaskAPIYamlDefinition api;
    }

    @Data
    public static class LoopTaskAPIYamlDefinition implements Serializable {

        private LoopTaskSubmitMethodYamlDefinition submit;
        private LoopTaskQueryStateYamlDefinition queryState;
        private LoopTaskCancelYamlDefinition cancel;
    }

    @Data
    @SuppressWarnings("checkstyle:ModifierOrder")
    public static abstract class LoopTaskMethodYamlDefinition {

        private String url;
        private String method;
        private String dataType;
        private Map<String, String> httpHeaders;
        private Map<String, Object> requestParams;
        private Map<String, Object> requestBody;
    }

    @Data
    public static class LoopTaskSubmitMethodYamlDefinition extends LoopTaskMethodYamlDefinition {

        /**
         * Used to extract the given params from the task params.
         */
        private Map<String, String> taskParamsExtractJPath;

        /**
         * Used to extract task instance id from response
         */
        private String taskInstanceIdJPath;
    }

    @Data
    public static class LoopTaskQueryStateYamlDefinition extends LoopTaskMethodYamlDefinition {

        /**
         * Used to extract taskInstance finished state from response
         * todo: we need to support the function to calculate the finished state
         */
        private String taskInstanceFinishedJPath;
    }

    @Data
    public static class LoopTaskCancelYamlDefinition extends LoopTaskMethodYamlDefinition {
    }

}
