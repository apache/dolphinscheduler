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

package org.apache.dolphinscheduler.plugin.task.linkis;

import org.apache.commons.lang3.StringUtils;

public enum LinkisJobStatus {

    UNSUBMITTED("Unsubmitted", 0),
    SUBMITTING("Submitting", 1),
    INITED("Inited", 2),
    WAIT_FOR_RETRY("WaitForRetry", 3),
    SCHEDULED("Scheduled", 4),
    RUNNING("Running", 5),
    SUCCEED("Succeed", 6),
    FAILED("Failed", 7),
    CANCELLED("Cancelled", 8),
    TIMEOUT("Timeout", 9),
    UNKNOWN("Unknown", 10),
    SHUTTINGDOWN("Shuttingdown", 11);

    private String name;
    private int id;

    LinkisJobStatus(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static LinkisJobStatus convertFromJobStatusString(String status) {
        if (StringUtils.isNotBlank(status)) {
            if (LinkisJobStatus.INITED.name().equalsIgnoreCase(status))
                return LinkisJobStatus.INITED;
            else if (LinkisJobStatus.WAIT_FOR_RETRY.name().equalsIgnoreCase(status))
                return LinkisJobStatus.WAIT_FOR_RETRY;
            else if (LinkisJobStatus.SCHEDULED.name().equalsIgnoreCase(status))
                return LinkisJobStatus.SCHEDULED;
            else if (LinkisJobStatus.RUNNING.name().equalsIgnoreCase(status))
                return LinkisJobStatus.RUNNING;
            else if (LinkisJobStatus.SUCCEED.name().equalsIgnoreCase(status))
                return LinkisJobStatus.SUCCEED;
            else if (LinkisJobStatus.FAILED.name().equalsIgnoreCase(status))
                return LinkisJobStatus.FAILED;
            else if (LinkisJobStatus.CANCELLED.name().equalsIgnoreCase(status))
                return LinkisJobStatus.CANCELLED;
            else if (LinkisJobStatus.TIMEOUT.name().equalsIgnoreCase(status))
                return LinkisJobStatus.TIMEOUT;
            else
                return LinkisJobStatus.UNKNOWN;
        } else {
            return LinkisJobStatus.UNKNOWN;
        }
    }
}
