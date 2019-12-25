package org.apache.dolphinscheduler.server.monitor;

import org.apache.dolphinscheduler.server.utils.operation.NodeOperation;
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
     * node operation
     */
    @Autowired
    private NodeOperation nodeOperation;


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
