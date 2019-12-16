package org.apache.dolphinscheduler.common.zk.operation;

import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * zk register node operation
 */
public class ZKNodeOperationImpl extends AbstractZKClient implements NodeOperation {

    private static final Logger logger = LoggerFactory.getLogger(ZKNodeOperationImpl.class);

    /**
     * judge whether node exists
     * @param node register node
     * @return whether node exists
     */
    @Override
    public Boolean exists(String node) {
        try {
            if (zkClient.checkExists().forPath(node) == null){
                return false;
            }
            return true;
        }catch (Exception e){
            logger.error("error",e);
            return false;
        }finally {
            zkClient.close();
        }
    }

    /**
     * remove node cascade
     * @param rootNode register node
     * @return remove node status
     */
    @Override
    public Boolean removeNode(String rootNode) {
        try {

            zkClient.delete().guaranteed().deletingChildrenIfNeeded().forPath(rootNode);

            logger.info("delete node : {}", rootNode);

            return true;
        }catch (Exception e){
            logger.error("error",e);
            return false;
        }finally {
            zkClient.close();
        }
    }

    /**
     * list nodes by path
     * @param path path
     * @return list nodes
     */
    @Override
    public List<String> listNodesByPath(String path) {
        List<String> nodes = null;
        try {
            nodes = zkClient.getChildren().forPath(path);
            return nodes;
        }catch (Exception e){
            logger.error("error",e);
            return null;
        }finally {
            zkClient.close();
        }
    }


    public static void main(String[] args) {
        NodeOperation nodeOperation = new ZKNodeOperationImpl();
        System.out.println(nodeOperation.listNodesByPath("/qiaozhanwei"));
    }
}
