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
package org.apache.dolphinscheduler.common.model;

import org.apache.dolphinscheduler.common.enums.DependentRelation;

import java.util.List;

public class DependentTaskModel {


    private List<DependentItem> dependItemList;
    private DependentRelation relation;

    public List<DependentItem> getDependItemList() {
        return dependItemList;
    }

    public void setDependItemList(List<DependentItem> dependItemList) {
        this.dependItemList = dependItemList;
    }

    public DependentRelation getRelation() {
        return relation;
    }

    public void setRelation(DependentRelation relation) {
        this.relation = relation;
    }
}
