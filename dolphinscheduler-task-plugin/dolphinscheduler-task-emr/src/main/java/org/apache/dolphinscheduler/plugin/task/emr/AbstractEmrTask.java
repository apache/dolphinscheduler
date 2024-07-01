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

package org.apache.dolphinscheduler.plugin.task.emr;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_SETTERS_FOR_GETTERS;

import org.apache.dolphinscheduler.authentication.aws.AmazonElasticMapReduceClientFactory;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.Map;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * ERM Task abstract base class
 *
 * @since v3.1.0
 */
@Slf4j
public abstract class AbstractEmrTask extends AbstractRemoteTask {

    final TaskExecutionContext taskExecutionContext;
    EmrParameters emrParameters;
    AmazonElasticMapReduce emrClient;
    String clusterId;

    /**
     * config ObjectMapper features and propertyNamingStrategy
     * use UpperCamelCaseStrategy support capital letters parse
     *
     * @see PropertyNamingStrategy.UpperCamelCaseStrategy
     */
    static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(REQUIRE_SETTERS_FOR_GETTERS, true)
            .setTimeZone(TimeZone.getDefault())
            .setPropertyNamingStrategy(new PropertyNamingStrategy.UpperCamelCaseStrategy());

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected AbstractEmrTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        emrParameters = JSONUtils.parseObject(taskParams, EmrParameters.class);
        log.info("Initialize emr task params:{}", JSONUtils.toPrettyJsonString(taskParams));
        if (emrParameters == null || !emrParameters.checkParameters()) {
            throw new EmrTaskException("emr task params is not valid");
        }
        emrClient = createEmrClient();
    }

    @Override
    public AbstractParameters getParameters() {
        return emrParameters;
    }

    protected AmazonElasticMapReduce createEmrClient() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("aws.emr.", "");
        return AmazonElasticMapReduceClientFactory.createAmazonElasticMapReduceClient(awsProperties);
    }
}
