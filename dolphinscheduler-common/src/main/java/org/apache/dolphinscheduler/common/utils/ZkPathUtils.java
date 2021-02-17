package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.utils.StringUtils;

import static org.apache.dolphinscheduler.common.Constants.COLON;


public class ZkPathUtils {

    public static String generateWorkerZkNodeName(String address, String weight, long workerStartTime) {
        StringBuilder workerZkNodeNameBuilder = new StringBuilder(address);
        workerZkNodeNameBuilder.append(COLON);
        workerZkNodeNameBuilder.append(weight);
        workerZkNodeNameBuilder.append(COLON);
        workerZkNodeNameBuilder.append(workerStartTime);
        return workerZkNodeNameBuilder.toString();
    }

    public static WorkerZkNode getWorkerZkNodeName(String node) {
        if (StringUtils.isBlank(node)) {
            return null;
        }
        String[] split = node.split(COLON);

        return null;
    }

    public static String getWorkerAddress(WorkerZkNode workerZkNode) {
        if (StringUtils.isBlank(node)) {
            return null;
        }
        String[] split = node.split(COLON);
        StringBuilder workerZkNodeNameBuilder = new StringBuilder(address);
        workerZkNodeNameBuilder.append(COLON);
        workerZkNodeNameBuilder.append(weight);

        return workerZkNode == ;
    }


    public static class WorkerZkNode {
        private String addressHost;
        private String addressPort;
        private String weight;
        private String startTime;



    }

}
