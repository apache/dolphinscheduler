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

package org.apache.dolphinscheduler.registry.api.ha;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractServerStatusChangeListener implements ServerStatusChangeListener {

    @Override
    public void change(HAServer.ServerStatus originStatus, HAServer.ServerStatus currentStatus) {
        log.info("The status change from {} to {}.", originStatus, currentStatus);
        if (originStatus == HAServer.ServerStatus.ACTIVE) {
            if (currentStatus == HAServer.ServerStatus.STAND_BY) {
                changeToStandBy();
            }
        } else if (originStatus == HAServer.ServerStatus.STAND_BY) {
            if (currentStatus == HAServer.ServerStatus.ACTIVE) {
                changeToActive();
            }
        }
    }

    public abstract void changeToActive();

    public abstract void changeToStandBy();
}
