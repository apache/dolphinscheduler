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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.commons.lang3.StringUtils;

public class BlockingParameters extends AbstractParameters {

    // condition of blocking: BlockingOnFailed or BlockingOnSuccess
    private String blockingOpportunity;

    // if true, alert when blocking, otherwise do nothing

    private boolean isAlertWhenBlocking;

    @Override
    public boolean checkParameters() {
        return !StringUtils.isEmpty(blockingOpportunity);
    }

    public String getBlockingOpportunity() {
        return blockingOpportunity;
    }

    public void setBlockingCondition(String blockingOpportunity) {
        this.blockingOpportunity = blockingOpportunity;
    }

    public boolean isAlertWhenBlocking() {
        return isAlertWhenBlocking;
    }

    public void setAlertWhenBlocking(boolean alertWhenBlocking) {
        isAlertWhenBlocking = alertWhenBlocking;
    }
}
