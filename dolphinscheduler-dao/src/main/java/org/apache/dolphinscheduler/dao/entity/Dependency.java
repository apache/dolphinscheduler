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

import org.apache.dolphinscheduler.common.enums.SelfDependStrategy;

/**
 * dependency
 */
public class Dependency {

    /**
     * self depend strategy
     */
    private SelfDependStrategy self;

    /**
     * outer dependency string
     */
    private String outer;


    public Dependency(){}

    public Dependency(String outer, SelfDependStrategy self){

        this.outer = outer;
        this.self = self;

    }


    public SelfDependStrategy getSelf() {
        return self;
    }

    public void setSelf(SelfDependStrategy self) {
        this.self = self;
    }

    public String getOuter() {
        return outer;
    }

    public void setOuter(String outer) {
        this.outer = outer;
    }
}
