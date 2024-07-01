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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.extract.master.IStreamingTaskOperator;
import org.apache.dolphinscheduler.extract.master.transportor.StreamingTaskTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.StreamingTaskTriggerResponse;
import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteThreadPool;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StreamingTaskOperatorImpl implements IStreamingTaskOperator {

    @Autowired
    private StreamTaskExecuteThreadPool streamTaskExecuteThreadPool;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Override
    public StreamingTaskTriggerResponse triggerStreamingTask(StreamingTaskTriggerRequest streamingTaskTriggerRequest) {
        log.info("Receive triggerStreamingTask request: {}", streamingTaskTriggerRequest);

        TaskDefinition taskDefinition =
                taskDefinitionDao.findTaskDefinition(streamingTaskTriggerRequest.getTaskDefinitionCode(),
                        streamingTaskTriggerRequest.getTaskDefinitionVersion());
        if (taskDefinition == null) {
            log.error("Cannot find the Streaming TaskDefinition: {}", streamingTaskTriggerRequest);
            return StreamingTaskTriggerResponse.fail("Cannot find the Streaming TaskDefinition");
        }
        streamTaskExecuteThreadPool.execute(new StreamTaskExecuteRunnable(taskDefinition, streamingTaskTriggerRequest));
        return StreamingTaskTriggerResponse.success();
    }

}
