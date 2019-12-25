package org.apache.dolphinscheduler.server.utils.operation;

import java.util.List;

/**
 * register node operation
 */
public interface NodeOperation {

    /**
     * judge whether node exists
     * @param node register node
     * @return whether node exists
     */
    Boolean exists(String node);


    /**
     * remove node
     * @param node register node
     * @return remove node status
     */
    Boolean removeNode(String node);


    /**
     * list nodes by path
     * @param path path
     * @return list nodes
     */
    List<String> listNodesByPath(String path);
}
