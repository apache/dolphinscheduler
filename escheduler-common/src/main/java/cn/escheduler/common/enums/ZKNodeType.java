package cn.escheduler.common.enums;

/**
 * zk node type
 */
public enum ZKNodeType {

    /**
     * 0 do not send warning;
     * 1 send if process success;
     * 2 send if process failed;
     * 3 send if process ending;
     */
    MASTER, WORKER, DEAD_SERVER, TASK_QUEUE;
}
