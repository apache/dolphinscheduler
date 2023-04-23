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

package org.apache.dolphinscheduler.plugin.kubeflow;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;

public class KubeflowHelper {

    protected final Logger log =
            LoggerFactory.getLogger(KubeflowHelper.class);

    private final String clusterConfigPath;

    private int messageIndex = 0;

    public KubeflowHelper(String clusterConfigPath) {
        this.clusterConfigPath = clusterConfigPath;
    }

    public String buildSubmitCommand(String yamlFilePATH) {
        List<String> args = new ArrayList<>();
        args.add(String.format(COMMAND.SET_CONFIG, clusterConfigPath));
        args.add(String.format(COMMAND.APPLY, yamlFilePATH));
        return String.join("\n", args);
    }

    public String buildGetCommand(String yamlFilePATH) {
        List<String> args = new ArrayList<>();
        args.add(String.format(COMMAND.SET_CONFIG, clusterConfigPath));
        args.add(String.format(COMMAND.GET, yamlFilePATH));
        return String.join("\n", args);
    }

    public String buildDeleteCommand(String yamlFilePATH) {
        List<String> args = new ArrayList<>();
        args.add(String.format(COMMAND.SET_CONFIG, clusterConfigPath));
        args.add(String.format(COMMAND.DELETE, yamlFilePATH));
        return String.join("\n", args);
    }

    public String parseGetMessage(String message) {
        JsonNode data = JSONUtils.parseObject(message);
        if (!data.has("status")) {
            return "";
        }
        JsonNode status = data.get("status");

        String lastConditionType = "";
        if (status.has("conditions")) {
            JsonNode conditions = status.get("conditions");
            for (int x = messageIndex; x < conditions.size(); x = x + 1) {
                JsonNode condition = conditions.get(x);
                String stepMessage = condition.toString();
                log.info(stepMessage);
            }
            messageIndex = conditions.size();
            JsonNode lastCondition = conditions.get(conditions.size() - 1);
            lastConditionType = lastCondition.has("type") ? lastCondition.get("type").asText() : "";
        }
        String phase;
        if (status.has("phase")) {
            phase = status.get("phase").asText();
        } else if (StringUtils.isNotEmpty(lastConditionType)) {
            phase = lastConditionType;
        } else {
            phase = "";
        }
        return phase;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationIds {

        boolean isAlreadySubmitted;
    }

    public static class STATUS {

        public static final HashSet<String> SUCCESS_SET = Sets.newHashSet("Succeeded", "Available", "Bound");
        public static final HashSet<String> FAILED_SET = Sets.newHashSet("Failed");

    }

    public static class CONSTANTS {

        public static final int TRACK_INTERVAL = 3000;
        public static final String YAML_FILE_PATH = "kubeflow.yaml";
        public static final String CLUSTER_CONFIG_PATH = ".cluster.yaml";
    }

    public static class COMMAND {

        public static final String SET_CONFIG = "export KUBECONFIG=%s";
        public static final String APPLY = "kubectl apply -f %s";
        public static final String GET = "kubectl get -f %s -o json";
        public static final String DELETE = "kubectl delete -f %s";

    }
}
