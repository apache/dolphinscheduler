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
    private int sourceWorkFlowId;
    private int targetWorkFlowId;

    public int getSourceWorkFlowId() {
        return sourceWorkFlowId;
    }

    public void setSourceWorkFlowId(int sourceWorkFlowId) {
        this.sourceWorkFlowId = sourceWorkFlowId;
    }

    public int getTargetWorkFlowId() {
        return targetWorkFlowId;
    }

    public void setTargetWorkFlowId(int targetWorkFlowId) {
        this.targetWorkFlowId = targetWorkFlowId;
    }

    public WorkFlowRelation() {
    }

    public WorkFlowRelation(int sourceWorkFlowId, int targetWorkFlowId) {
        this.sourceWorkFlowId = sourceWorkFlowId;
        this.targetWorkFlowId = targetWorkFlowId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkFlowRelation
                && this.sourceWorkFlowId == ((WorkFlowRelation) obj).getSourceWorkFlowId()
                && this.targetWorkFlowId == ((WorkFlowRelation) obj).getTargetWorkFlowId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceWorkFlowId, targetWorkFlowId);
    }
}
