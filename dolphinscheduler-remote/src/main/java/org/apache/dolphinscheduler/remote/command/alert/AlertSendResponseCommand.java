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

package org.apache.dolphinscheduler.remote.command.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSendResponseCommand implements Serializable {

    /**
     * true:All alert are successful,
     * false:As long as one alert fails
     */
    private boolean success;

    private List<AlertSendResponseResult> resResults;

    /**
     * package response command
     *
     * @param opaque request unique identification
     * @return command
     */
    public Command convert2Command(long opaque) {
        Command command = new Command(opaque);
        command.setType(CommandType.ALERT_SEND_RESPONSE);
        byte[] body = JsonSerializer.serialize(this);
        command.setBody(body);
        return command;
    }
}
