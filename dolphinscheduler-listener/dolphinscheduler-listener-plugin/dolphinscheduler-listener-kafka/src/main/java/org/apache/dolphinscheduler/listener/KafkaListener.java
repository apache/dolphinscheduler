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

package org.apache.dolphinscheduler.listener;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.listener.event.ServerDownListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskCreateListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskEndListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskFailListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskRemoveListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskStartListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskUpdateListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowCreateListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowEndListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowFailListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowRemoveListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowStartListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowUpdateListenerEvent;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaListener implements ListenerPlugin {

    private final Map<String, KafkaProducer<String, String>> kafkaProducers = new HashMap<>();

    @Override
    public String name() {
        return "KafkaListener";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam hostParam = InputParam.newBuilder("servers", "bootstrap.servers")
                .setPlaceholder("please input servers")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam topicParam = InputParam.newBuilder("topic", "topic")
                .setPlaceholder("please input topic")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        InputParam usernameParam = InputParam.newBuilder("username", "username")
                .setPlaceholder("please input username")
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        InputParam passwordParam = InputParam.newBuilder("password", "password")
                .setPlaceholder("please input password")
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        paramsList.add(hostParam);
        paramsList.add(topicParam);
        paramsList.add(usernameParam);
        paramsList.add(passwordParam);
        return paramsList;
    }

    @Override
    public void onServerDown(ServerDownListenerEvent serverDownListenerEvent) {
        sendEvent(serverDownListenerEvent.getListenerInstanceParams(), ServerDownListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(serverDownListenerEvent));
    }

    @Override
    public void onWorkflowAdded(WorkflowCreateListenerEvent workflowCreateEvent) {
        sendEvent(workflowCreateEvent.getListenerInstanceParams(), WorkflowCreateListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowCreateEvent));
    }

    @Override
    public void onWorkflowUpdate(WorkflowUpdateListenerEvent workflowUpdateEvent) {
        sendEvent(workflowUpdateEvent.getListenerInstanceParams(), WorkflowUpdateListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowUpdateEvent));
    }

    @Override
    public void onWorkflowRemoved(WorkflowRemoveListenerEvent workflowRemovedEvent) {
        sendEvent(workflowRemovedEvent.getListenerInstanceParams(), WorkflowRemoveListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowRemovedEvent));
    }

    @Override
    public void onWorkflowStart(WorkflowStartListenerEvent workflowStartEvent) {
        sendEvent(workflowStartEvent.getListenerInstanceParams(), WorkflowStartListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowStartEvent));

    }

    @Override
    public void onWorkflowEnd(WorkflowEndListenerEvent workflowEndEvent) {
        sendEvent(workflowEndEvent.getListenerInstanceParams(), WorkflowEndListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowEndEvent));
    }

    @Override
    public void onWorkflowFail(WorkflowFailListenerEvent workflowErrorEvent) {
        sendEvent(workflowErrorEvent.getListenerInstanceParams(), WorkflowFailListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(workflowErrorEvent));
    }

    @Override
    public void onTaskAdded(TaskCreateListenerEvent taskAddedEvent) {
        sendEvent(taskAddedEvent.getListenerInstanceParams(), TaskCreateListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskAddedEvent));
    }

    @Override
    public void onTaskUpdate(TaskUpdateListenerEvent taskUpdateEvent) {
        sendEvent(taskUpdateEvent.getListenerInstanceParams(), TaskUpdateListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskUpdateEvent));
    }

    @Override
    public void onTaskRemoved(TaskRemoveListenerEvent taskRemovedEvent) {
        sendEvent(taskRemovedEvent.getListenerInstanceParams(), TaskRemoveListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskRemovedEvent));
    }

    @Override
    public void onTaskStart(TaskStartListenerEvent taskStartEvent) {
        sendEvent(taskStartEvent.getListenerInstanceParams(), TaskStartListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskStartEvent));
    }

    @Override
    public void onTaskEnd(TaskEndListenerEvent taskEndEvent) {
        sendEvent(taskEndEvent.getListenerInstanceParams(), TaskEndListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskEndEvent));
    }

    @Override
    public void onTaskFail(TaskFailListenerEvent taskErrorEvent) {
        sendEvent(taskErrorEvent.getListenerInstanceParams(), TaskFailListenerEvent.class.getSimpleName(),
                JSONUtils.toJsonString(taskErrorEvent));
    }

    private void sendEvent(Map<String, String> listenerInstanceParams, String key, String value) {
        String uniqueId = uniqueId(listenerInstanceParams);
        if (!kafkaProducers.containsKey(uniqueId)) {
            String kafkaBroker = listenerInstanceParams.get("servers");
            String username = listenerInstanceParams.get("username");
            String password = listenerInstanceParams.get("password");
            Map<String, Object> configurations = new HashMap<>();
            // TODO: when use username/password, throws exception: Unable to find LoginModule class:
            // org.apache.kafka.common.security.plain.PlainLoginModule
            configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
            configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                configurations.put("sasl.jaas.config", String.format(
                        "org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
                        username, password));
                configurations.put("security.protocol", "SASL_PLAINTEXT");
                configurations.put("sasl.mechanism", "PLAIN");
            }
            KafkaProducer<String, String> producer = new KafkaProducer<>(configurations);
            kafkaProducers.put(uniqueId, producer);

        }
        KafkaProducer<String, String> producer = kafkaProducers.get(uniqueId);
        String topic = listenerInstanceParams.get("topic");
        producer.send(new ProducerRecord<>(topic, key, value), (recordMetadata, e) -> {
            if (e != null) {
                throw new RuntimeException(e);
            }
        });
    }

    private String uniqueId(Map<String, String> listenerInstanceParams) {
        String kafkaBroker = listenerInstanceParams.get("servers");
        String topic = listenerInstanceParams.get("topic");
        String username = listenerInstanceParams.getOrDefault("username", "foo");
        String password = listenerInstanceParams.getOrDefault("password", "foo");
        return String.format("broker=%s&topic=%s&username=%s&password=%s", kafkaBroker, topic, username, password);
    }
}
