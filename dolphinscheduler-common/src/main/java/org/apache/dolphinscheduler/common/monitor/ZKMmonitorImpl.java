package org.apache.dolphinscheduler.common.monitor;

import org.apache.dolphinscheduler.common.zk.operation.NodeOperation;
import org.apache.dolphinscheduler.common.zk.operation.ZKNodeOperationImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk monitor server impl
 */
public class ZKMmonitorImpl extends AbstractMonitor {

    private NodeOperation nodeOperation;

    public ZKMmonitorImpl(NodeOperation nodeOperation){
        this.nodeOperation = nodeOperation;
    }

    /**
     * get active nodes map by path
     * @param path path
     * @return active nodes map
     */
    @Override
    protected Map<String,String> getActiveNodesByPath(String path) {

        Map<String,String> maps = new HashMap<>();

        List<String> childrenList = nodeOperation.listNodesByPath(path);

        if (childrenList == null){
            return maps;
        }

        for (String child : childrenList){
            maps.put(child.split("_")[0],child);
        }

        return maps;
    }
}
