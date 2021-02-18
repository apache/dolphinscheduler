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

package org.apache.dolphinscheduler.service.zk;

import static org.apache.dolphinscheduler.common.Constants.COLON;

import org.apache.dolphinscheduler.common.model.WorkerZkNode;
import org.apache.dolphinscheduler.common.utils.StringUtils;

/**
 * zookeeper node handler
 */
public class ZookeeperNodeHandler {

    private ZookeeperNodeHandler() {
        throw new UnsupportedOperationException("Construct ZookeeperNodeHandler");
    }

    /**
     * generate worker zookeeper node name
     *
     * @param address address
     * @param weight weight
     * @param workerStartTime workerStartTime
     * @return worker address:weight:startTime
     */
    public static String generateWorkerZkNodeName(String address, String weight, long workerStartTime) {
        StringBuilder workerZkNodeNameBuilder = new StringBuilder(address);
        workerZkNodeNameBuilder.append(COLON);
        workerZkNodeNameBuilder.append(weight);
        workerZkNodeNameBuilder.append(COLON);
        workerZkNodeNameBuilder.append(workerStartTime);
        return workerZkNodeNameBuilder.toString();
    }

    /**
     * get worker info according to zookeeper node
     *
     * @param zkNode zookeeper node
     * @return worker zookeeper node
     */
    public static WorkerZkNode getWorkerZkNodeName(String zkNode) {
        if (StringUtils.isBlank(zkNode)) {
            return null;
        }
        String[] split = zkNode.split(COLON);
        return new WorkerZkNode(split[0], split[1], split[2], split[3]);
    }

    /**
     * get worker address
     *
     * @param workerZkNode worker zookeeper node
     * @return worker address
     */
    public static String getWorkerAddress(WorkerZkNode workerZkNode) {
        return workerZkNode == null ? null : workerZkNode.getAddressHost() + COLON + workerZkNode.getAddressPort();
    }

    /**
     * get worker address
     *
     * @param zkNode zookeeper node
     * @return worker address
     */
    public static String getWorkerAddress(String zkNode) {
        return getWorkerAddress(getWorkerZkNodeName(zkNode));
    }

    /**
     * get worker address and weight
     *
     * @param workerZkNode worker zookeeper node
     * @return worker address:weight
     */
    public static String getWorkerAddressAndWeight(WorkerZkNode workerZkNode) {
        return workerZkNode == null ? null : getWorkerAddress(workerZkNode) + COLON + workerZkNode.getWeight();
    }

    /**
     * get worker address and weight
     *
     * @param zkNode zookeeper node
     * @return worker address:weight
     */
    public static String getWorkerAddressAndWeight(String zkNode) {
        return getWorkerAddressAndWeight(getWorkerZkNodeName(zkNode));
    }

    /**
     * get worker startTime
     * @param workerZkNode worker zookeeper node
     * @return worker startTime
     */
    public static String getWorkerStartTime(WorkerZkNode workerZkNode) {
        return workerZkNode == null ? null : workerZkNode.getStartTime();
    }

}
