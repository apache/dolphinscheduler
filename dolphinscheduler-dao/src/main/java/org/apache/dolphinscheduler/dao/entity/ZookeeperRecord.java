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
package org.apache.dolphinscheduler.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * monitor record for zookeeper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ZookeeperRecord {
    private String hostname;
    private int connections;
    private int watches;
    private long sent;
    private long received;
    private String mode;
    private int minLatency;
    private int avgLatency;
    private int maxLatency;
    private int nodeCount;
    /**
     * is normal or not, 1:normal
     */
    private int state;
    private Date date;
}
