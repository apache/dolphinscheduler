package org.apache.dolphinscheduler.api.dto.resources.visitor;


import org.apache.dolphinscheduler.api.dto.resources.Directory;
import org.apache.dolphinscheduler.api.dto.resources.FileLeaf;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.ArrayList;
import java.util.List;

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
public class ResourceTreeVisitor implements Visitor{
    private List<Resource> resourceList;

    public ResourceTreeVisitor() {
    }

    public ResourceTreeVisitor(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    public ResourceComponent visit() {
        ResourceComponent rootDirectory = new Directory();
        for (Resource resource : resourceList) {

            ResourceComponent tempResourceComponent;
            if(resource.isDirectory()){
                tempResourceComponent = new Directory();
            }else{
                tempResourceComponent = new FileLeaf();
            }
            //表明是一级父类
            if (rootNode(resource)){
                tempResourceComponent.setName(resource.getAlias());
                tempResourceComponent.setFullName(resource.getFullName());
                tempResourceComponent.setId(resource.getId());
                tempResourceComponent.setPid(resource.getPid());
                tempResourceComponent.setIdValue(resource.getId(),resource.isDirectory());
                tempResourceComponent.setDescription(resource.getDescription());
                tempResourceComponent.setType(resource.getType());
                rootDirectory.add(tempResourceComponent);
            }

            //resource.setList(setChild(resource.getId(),resourceList));
            tempResourceComponent.setChildren(setChildren(tempResourceComponent.getId(),resourceList));
        }
        return rootDirectory;
    }

    public static List<ResourceComponent> setChildren(int id, List<Resource> list ){
        List<ResourceComponent> childList = new ArrayList<>();
        for (Resource resource : list) {
            ResourceComponent tempResourceComponent;
            if(resource.isDirectory()){
                tempResourceComponent = new Directory();
            }else{
                tempResourceComponent = new FileLeaf();
            }
            if (id == resource.getPid()){
                tempResourceComponent.setName(resource.getAlias());
                tempResourceComponent.setFullName(resource.getFullName());
                tempResourceComponent.setId(resource.getId());
                tempResourceComponent.setPid(resource.getPid());
                tempResourceComponent.setIdValue(resource.getId(),resource.isDirectory());
                tempResourceComponent.setDescription(resource.getDescription());
                tempResourceComponent.setType(resource.getType());
                childList.add(tempResourceComponent);
            }
        }
        for (ResourceComponent resourceComponent : childList) {
            resourceComponent.setChildren(setChildren(resourceComponent.getId(),list));
        }
        if (childList.size()==0){
            return new ArrayList<>();
        }
        return childList;
    }

    /**
     * Determine whether it is the root node
     * @param resource resource
     * @return true if it is the root node
     */

    public boolean rootNode(Resource resource) {

        boolean isRootNode = true;
        if(resource.getPid() != -1 ){
            for (Resource parent : resourceList) {
                if (resource.getPid() == parent.getId()) {
                    isRootNode = false;
                    break;
                }
            }
        }
        return isRootNode;
    }

}
