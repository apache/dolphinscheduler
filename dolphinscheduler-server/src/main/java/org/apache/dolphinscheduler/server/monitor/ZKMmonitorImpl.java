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
package org.apache.dolphinscheduler.server.monitor;

import org.apache.dolphinscheduler.common.zk.ZookeeperOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk monitor server impl
 */
@Component
public class ZKMmonitorImpl extends AbstractMonitor {

    /**
     * zookeeper operator
     */
    @Autowired
    private ZookeeperOperator zookeeperOperator;


    /**
     * get active nodes map by path
     * @param path path
     * @return active nodes map
     */
    @Override
    protected Map<String,String> getActiveNodesByPath(String path) {

        Map<String,String> maps = new HashMap<>();

        List<String> childrenList = zookeeperOperator.getChildrenKeys(path);

        if (childrenList == null){
            return maps;
        }

        for (String child : childrenList){
            maps.put(child.split("_")[0],child);
        }

        return maps;
    }
}
