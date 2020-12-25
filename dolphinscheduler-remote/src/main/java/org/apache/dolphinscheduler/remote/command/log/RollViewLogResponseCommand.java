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

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;

import java.io.Serializable;

/**
 *  roll view log response command
 */
public class RollViewLogResponseCommand implements Serializable {

    /**
     *  response data
     */
    private String msg;

    public RollViewLogResponseCommand() {
    }

    public RollViewLogResponseCommand(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * package response command
     *
     * @param opaque request unique identification
     * @return command
     */
    public Command convert2Command(long opaque){
        Command command = new Command(opaque);
        command.setType(CommandType.ROLL_VIEW_LOG_RESPONSE);
        byte[] body = FastJsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
