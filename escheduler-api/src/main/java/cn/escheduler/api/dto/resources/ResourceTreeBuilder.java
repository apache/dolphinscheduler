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
package cn.escheduler.api.dto.resources;

import cn.escheduler.api.service.ResourcesService;
import cn.escheduler.dao.model.Resource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource Tree Builder
 */
public class ResourceTreeBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesService.class);

    List<ResourceViewDto> nodes = new ArrayList<ResourceViewDto>();

    public ResourceTreeBuilder() {
    }

    public ResourceTreeBuilder(List<?> resNodes,Class<?> clazz) {
        if(clazz == ResourceViewDto.class){
            this.nodes = (List<ResourceViewDto>) resNodes;
        }else if(clazz == Resource.class){
            List<ResourceViewDto> resourceViewDtoList = new ArrayList<>();

            for(Object resource:resNodes){
                resourceViewDtoList.add(new ResourceViewDto((Resource) resource));
            }
            this.nodes = resourceViewDtoList;
        }
    }


    public ResourceTreeBuilder(List<ResourceViewDto> nodes) {
        this.nodes = nodes;
    }

    /**
     *
     * @param all
     * @param authorized
     */
    public ResourceTreeBuilder(List<Resource> all,List<Resource> authorized) {
        List<ResourceViewDto> resourceViewDtoList = all.stream().map(t -> {
            ResourceViewDto resourceViewDto = null;
            if (authorized.contains(t)) {
                resourceViewDto = new ResourceViewDto(t,1);
            } else {
                resourceViewDto = new ResourceViewDto(t, 0);
            }
            return resourceViewDto;
        }).collect(Collectors.toList());
        this.nodes = resourceViewDtoList;
        //all.stream().filter(item -> authorized.contains(item)).collect(Collectors.toList());
    }

    /**
     * resource tree builder
     * @param resources
     * @param permission 0 no permission 1 has permission
     */
    public ResourceTreeBuilder(List<Resource> resources,int permission) {
        List<ResourceViewDto> resourceViewDtoList = new ArrayList<>();

        for(Resource resource:resources){
            resourceViewDtoList.add(new ResourceViewDto(resource,permission));
        }
        this.nodes = resourceViewDtoList;

    }

    /**
     * Construct a JSON tree structure
     *
     * @return
     */

    public String buildJSONTree() {
        Collections.sort(this.nodes);
        List<ResourceViewDto> nodeTree = buildTree();
        logger.info(nodeTree.toString());

        return JSON.toJSONString(nodeTree, SerializerFeature.SortField);

    }


    /**
     * Build a tree structure
     *
     * @return
     */

    public List<ResourceViewDto> buildTree() {

        List<ResourceViewDto> treeNodes = new ArrayList<ResourceViewDto>();
        List<ResourceViewDto> rootNodes = getRootNodes();

        for (ResourceViewDto rootNode : rootNodes) {
            buildChildNodes(rootNode);
            treeNodes.add(rootNode);
        }

        return treeNodes;

    }


    /**
     * Recursively subnodess
     * 递归子节点
     *
     * @param node
     */

    public void buildChildNodes(ResourceViewDto node) {

        List<ResourceViewDto> children = getChildNodes(node);

        if (!children.isEmpty()) {
            for (ResourceViewDto child : children) {
                buildChildNodes(child);
            }
            node.setChildren(children);
        }

    }


    /**
     * Gets all children under the parent node
     * 获取父节点下所有的子节点
     *
     * @param pnode
     * @return
     */

    public List<ResourceViewDto> getChildNodes(ResourceViewDto pnode) {

        List<ResourceViewDto> childNodes = new ArrayList<ResourceViewDto>();

        for (ResourceViewDto n : nodes) {
            if (pnode.getId() == n.getPid()) {
                childNodes.add(n);
            }
        }

        return childNodes;

    }


    /**
     * Determine whether it is the root node
     * 判断是否为根节点
     *
     * @param node
     * @return
     */

    public boolean rootNode(ResourceViewDto node) {

        boolean isRootNode = true;

        for (ResourceViewDto n : nodes) {
            if (node.getPid() == n.getId()) {
                isRootNode = false;
                break;
            }
        }

        return isRootNode;

    }


    /**
     * Gets all the root nodes in the collection
     * 获取集合中所有的根节点
     *
     * @return
     */

    public List<ResourceViewDto> getRootNodes() {

        List<ResourceViewDto> rootNodes = new ArrayList<ResourceViewDto>();

        for (ResourceViewDto n : nodes) {
            if (rootNode(n)) {
                rootNodes.add(n);
            }
        }

        return rootNodes;

    }
}
