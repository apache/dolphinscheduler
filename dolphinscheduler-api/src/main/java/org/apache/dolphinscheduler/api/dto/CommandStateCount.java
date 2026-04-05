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

package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.common.enums.CommandType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * command state count
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandStateCount {

    private int errorCount;
    private int normalCount;
    private CommandType commandState;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommandStateCount that = (CommandStateCount) o;

        if (errorCount != that.errorCount) {
            return false;
        }
        if (normalCount != that.normalCount) {
            return false;
        }
        return commandState == that.commandState;
    }

    @Override
    public int hashCode() {
        int result = errorCount;
        result = 31 * result + normalCount;
        result = 31 * result + (commandState != null ? commandState.hashCode() : 0);
        return result;
    }
}
