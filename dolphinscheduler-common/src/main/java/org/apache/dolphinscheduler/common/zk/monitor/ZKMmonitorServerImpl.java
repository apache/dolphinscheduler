package org.apache.dolphinscheduler.common.zk.monitor;

import org.apache.dolphinscheduler.common.zk.operation.NodeOperation;
import org.apache.dolphinscheduler.common.zk.operation.ZKNodeOperationImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * zk monitor server impl
 */
public class ZKMmonitorServerImpl extends AbstractMonitorServer {

    private NodeOperation nodeOperation;

    public ZKMmonitorServerImpl(NodeOperation nodeOperation){
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
            return null;
        }

        for (String child : childrenList){
            maps.put(child.split("_")[0],child);
        }

        return maps;
    }

    public static void main(String[] args) throws Exception{
        String masterPath = args[0];
        String workerPath = args[1];
        Integer port = Integer.parseInt(args[2]);
        String installPath = args[3];

        MonitorServer monitorServer = new ZKMmonitorServerImpl(new ZKNodeOperationImpl());
        while (true){
            monitorServer.monitor(masterPath,workerPath,port,installPath);

            // per five minutes to monitor
            Thread.sleep(1000 * 60 * 5);
        }
    }
}
