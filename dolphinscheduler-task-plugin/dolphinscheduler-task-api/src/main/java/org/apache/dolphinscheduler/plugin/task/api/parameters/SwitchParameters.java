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

import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;

import java.util.ArrayList;
import java.util.List;

public class SwitchParameters extends AbstractParameters {

    private DependentRelation dependRelation;
    private String relation;
    private List<Long> nextNode;

    @Override
    public boolean checkParameters() {
        return true;
    }

    private int resultConditionLocation;
    private List<SwitchResultVo> dependTaskList;

    public DependentRelation getDependRelation() {
        return dependRelation;
    }

    public void setDependRelation(DependentRelation dependRelation) {
        this.dependRelation = dependRelation;
    }

    public int getResultConditionLocation() {
        return resultConditionLocation;
    }

    public void setResultConditionLocation(int resultConditionLocation) {
        this.resultConditionLocation = resultConditionLocation;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public List<SwitchResultVo> getDependTaskList() {
        return dependTaskList;
    }

    public void setDependTaskList(List<SwitchResultVo> dependTaskList) {
        this.dependTaskList = dependTaskList;
    }

    public List<Long> getNextNode() {
        return nextNode;
    }

    public void setNextNode(Object nextNode) {
        if (nextNode instanceof Long) {
            List<Long> nextNodeList = new ArrayList<>();
            nextNodeList.add((Long) nextNode);
            this.nextNode = nextNodeList;
        } else {
            this.nextNode = (ArrayList) nextNode;
        }
    }
}
