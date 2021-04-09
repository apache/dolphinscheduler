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

package org.apache.dolphinscheduler.service.zk;

import static org.apache.dolphinscheduler.common.Constants.ADD_ZK_OP;
import static org.apache.dolphinscheduler.common.Constants.DELETE_ZK_OP;
import static org.apache.dolphinscheduler.common.Constants.MASTER_TYPE;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.WORKER_TYPE;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * register operator
 */
@Component
public class RegisterOperator extends ZookeeperCachedOperator {

    private final Logger logger = LoggerFactory.getLogger(RegisterOperator.class);

    /**
     * @return get dead server node parent path
     */
    protected String getDeadZNodeParentPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS;
    }

    /**
     * remove dead server by host
     *
     * @param host       host
     * @param serverType serverType
     * @throws Exception
     */
    public void removeDeadServerByHost(String host, String serverType) throws Exception {
        List<String> deadServers = super.getChildrenKeys(getDeadZNodeParentPath());
        for (String serverPath : deadServers) {
            if (serverPath.startsWith(serverType + UNDERLINE + host)) {
                String server = getDeadZNodeParentPath() + SINGLE_SLASH + serverPath;
                super.remove(server);
                logger.info("{} server {} deleted from zk dead server path success", serverType, host);
            }
        }
    }

    /**
     * get host ip:port, string format: parentPath/ip:port
     *
     * @param path path
     * @return host ip:port, string format: parentPath/ip:port
     */
    protected String getHostByEventDataPath(String path) {
        if (StringUtils.isEmpty(path)) {
            logger.error("empty path!");
            return "";
        }
        String[] pathArray = path.split(SINGLE_SLASH);
        if (pathArray.length < 1) {
            logger.error("parse ip error: {}", path);
            return "";
        }
        return pathArray[pathArray.length - 1];

    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param zNode      node path
     * @param zkNodeType master or worker
     * @param opType     delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(String zNode, ZKNodeType zkNodeType, String opType) throws Exception {
        String host = getHostByEventDataPath(zNode);
        String type = (zkNodeType == ZKNodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;

        //check server restart, if restart , dead server path in zk should be delete
        if (opType.equals(DELETE_ZK_OP)) {
            removeDeadServerByHost(host, type);

        } else if (opType.equals(ADD_ZK_OP)) {
            String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
            if (!super.isExisted(deadServerPath)) {
                //add dead server info to zk dead server path : /dead-servers/

                super.persist(deadServerPath, (type + UNDERLINE + host));

                logger.info("{} server dead , and {} added to zk dead server path success",
                        zkNodeType, zNode);
            }
        }

    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param zNodeSet   node path set
     * @param zkNodeType master or worker
     * @param opType     delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(Set<String> zNodeSet, ZKNodeType zkNodeType, String opType) throws Exception {

        String type = (zkNodeType == ZKNodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;
        for (String zNode : zNodeSet) {
            String host = getHostByEventDataPath(zNode);
            //check server restart, if restart , dead server path in zk should be delete
            if (opType.equals(DELETE_ZK_OP)) {
                removeDeadServerByHost(host, type);

            } else if (opType.equals(ADD_ZK_OP)) {
                String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
                if (!super.isExisted(deadServerPath)) {
                    //add dead server info to zk dead server path : /dead-servers/

                    super.persist(deadServerPath, (type + UNDERLINE + host));

                    logger.info("{} server dead , and {} added to zk dead server path success",
                            zkNodeType, zNode);
                }
            }

        }

    }
}
