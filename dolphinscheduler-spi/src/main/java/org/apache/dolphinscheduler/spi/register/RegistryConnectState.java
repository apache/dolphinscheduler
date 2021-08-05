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

package org.apache.dolphinscheduler.spi.register;

/**
 * All registry connection status must be converted to this
 */
public enum RegistryConnectState {
    CONNECTED("connected", 1),
    RECONNECTED("reconnected", 2),
    SUSPENDED("suspended", 3),
    LOST("lost", 4);

    private String description;

    private int state;

    RegistryConnectState(String description, int state) {
        this.description = description;
        this.state = state;
    }
}
