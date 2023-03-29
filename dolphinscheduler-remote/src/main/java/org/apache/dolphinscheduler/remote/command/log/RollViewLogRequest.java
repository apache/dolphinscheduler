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

package org.apache.dolphinscheduler.remote.command.log;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.RequestMessageBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  roll view log request command
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RollViewLogRequest implements RequestMessageBuilder {

    /**
     *  log path
     */
    private String path;

    /**
     *  skip line number
     */
    private int skipLineNum;

    /**
     *  query line number
     */
    private int limit;

    @Override
    public MessageType getCommandType() {
        return MessageType.ROLL_VIEW_LOG_REQUEST;
    }
}
