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

package org.apache.dolphinscheduler.dao.entity;

import java.util.Objects;

public class WorkFlowRelation {

    private long sourceWorkFlowCode;
    private long targetWorkFlowCode;

    public long getSourceWorkFlowCode() {
        return sourceWorkFlowCode;
    }

    public void setSourceWorkFlowCode(long sourceWorkFlowCode) {
        this.sourceWorkFlowCode = sourceWorkFlowCode;
    }

    public long getTargetWorkFlowCode() {
        return targetWorkFlowCode;
    }

    public void setTargetWorkFlowCode(long targetWorkFlowCode) {
        this.targetWorkFlowCode = targetWorkFlowCode;
    }

    public WorkFlowRelation() {
    }

    public WorkFlowRelation(long sourceWorkFlowCode, long targetWorkFlowCode) {
        this.sourceWorkFlowCode = sourceWorkFlowCode;
        this.targetWorkFlowCode = targetWorkFlowCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkFlowRelation that = (WorkFlowRelation) o;
        return sourceWorkFlowCode == that.sourceWorkFlowCode
                && targetWorkFlowCode == that.targetWorkFlowCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceWorkFlowCode, targetWorkFlowCode);
    }
}
