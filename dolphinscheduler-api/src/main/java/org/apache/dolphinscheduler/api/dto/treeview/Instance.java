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

package org.apache.dolphinscheduler.api.dto.treeview;

import java.util.Date;

import lombok.Data;

@Data
public class Instance {

    private Integer id;

    /**
     * node name
     */
    private String name;

    /**
     * node code
     */
    private long code;

    /**
     * node type
     */
    private String type;

    /**
     * node status
     */
    private String state;

    /**
     * node start time
     */
    private Date startTime;

    /**
     * node end time
     */
    private Date endTime;

    /**
     * node running on which host
     */
    private String host;

    /**
     * node duration
     */
    private String duration;

    private long subflowCode;

    public Instance() {
    }

    public Instance(int id, String name, long code, String type) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public Instance(int id, String name, long code, String type, String state, Date startTime, Date endTime,
                    String host, String duration, long subflowCode) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.type = type;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.host = host;
        this.duration = duration;
        this.subflowCode = subflowCode;
    }

    public Instance(int id, String name, long code, String type, String state, Date startTime, Date endTime,
                    String host, String duration) {
        this(id, name, code, type, state, startTime, endTime, host, duration, 0);
    }
}
