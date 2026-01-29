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

package org.apache.dolphinscheduler.dao.repository;

import org.apache.dolphinscheduler.dao.entity.Command;

import java.util.List;

public interface CommandDao extends IDao<Command> {

    /**
     * Query command by command id and server slot, return the command which match (commandId / step) %s totalSlot = currentSlotIndex
     *
     * @param currentSlotIndex current slot index
     * @param totalSlot        total slot number
     * @param idStep           id step in db
     * @param fetchNum         fetch number
     * @return command list
     */
    List<Command> queryCommandByIdSlot(int currentSlotIndex,
                                       int totalSlot,
                                       int idStep,
                                       int fetchNum);
}
