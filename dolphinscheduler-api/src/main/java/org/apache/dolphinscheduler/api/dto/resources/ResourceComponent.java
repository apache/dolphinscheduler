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
package org.apache.dolphinscheduler.api.dto.resources;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import org.apache.dolphinscheduler.common.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * resource component
 */
@JSONType(orders={"id","pid","name","fullName","description","isDirctory","children","type"})
public abstract class ResourceComponent {
    public ResourceComponent() {
    }

    public ResourceComponent(int id, int pid, String name, String fullName, String description, boolean isDirctory) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isDirctory = isDirctory;
        int directoryFlag = isDirctory ? 1:0;
        this.idValue = String.format("%s_%s",id,directoryFlag);
    }


    /**
     * id
     */
    @JSONField(ordinal = 1)
    protected int id;
    /**
     * parent id
     */
    @JSONField(ordinal = 2)
    protected int pid;
    /**
     * name
     */
    @JSONField(ordinal = 3)
    protected String name;
    /**
     * current directory
     */
    protected String currentDir;
    /**
     * full name
     */
    @JSONField(ordinal = 4)
    protected String fullName;
    /**
     * description
     */
    @JSONField(ordinal = 5)
    protected String description;
    /**
     * is directory
     */
    @JSONField(ordinal = 6)
    protected boolean isDirctory;
    /**
     * id value
     */
    @JSONField(ordinal = 7)
    protected String idValue;
    /**
     * resoruce type
     */
    @JSONField(ordinal = 8)
    protected ResourceType type;
    /**
     * children
     */
    @JSONField(ordinal = 8)
    protected List<ResourceComponent> children = new ArrayList<>();

    /**
     * add resource component
     * @param resourceComponent resource component
     */
    public void add(ResourceComponent resourceComponent){
        children.add(resourceComponent);
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDirctory() {
        return isDirctory;
    }

    public void setDirctory(boolean dirctory) {
        isDirctory = dirctory;
    }

    public String getIdValue() {
        return idValue;
    }

    public void setIdValue(int id,boolean isDirctory) {
        int directoryFlag = isDirctory ? 1:0;
        this.idValue = String.format("%s_%s",id,directoryFlag);
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public List<ResourceComponent> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceComponent> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ResourceComponent{" +
                "id=" + id +
                ", pid=" + pid +
                ", name='" + name + '\'' +
                ", currentDir='" + currentDir + '\'' +
                ", fullName='" + fullName + '\'' +
                ", description='" + description + '\'' +
                ", isDirctory=" + isDirctory +
                ", idValue='" + idValue + '\'' +
                ", type=" + type +
                ", children=" + children +
                '}';
    }

}
