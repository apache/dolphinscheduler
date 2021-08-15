package org.apache.dolphinscheduler.service.queue;

import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MasterPriorityQueue implements TaskPriorityQueue<Server>{

    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 20;

    /**
     * queue
     */
    private PriorityBlockingQueue<Server> queue = new PriorityBlockingQueue<>(QUEUE_MAX_SIZE, new ServerComparator());

    private HashMap<String, Integer> hostIndexMap = new HashMap<>();

    @Override
    public void put(Server serverInfo) throws TaskPriorityQueueException {
        this.queue.put(serverInfo);
        refreshMasterList();
    }

    @Override
    public Server take() throws TaskPriorityQueueException, InterruptedException {
        return queue.take();
    }

    @Override
    public Server poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException, InterruptedException {
        return queue.poll();
    }

    @Override
    public int size() throws TaskPriorityQueueException {
        return queue.size();
    }

    public void putList(List<Server> serverList){
        for(Server server : serverList){
            this.queue.put(server);
        }
        refreshMasterList();
    }

    public void remove(Server server){
        this.queue.remove(server);
    }

    public void clear(){
        queue.clear();
        refreshMasterList();
    }

    private void refreshMasterList(){
        hostIndexMap.clear();
        Iterator<Server> iterator = queue.iterator();
        int index = 0;
        while(iterator.hasNext()){
            Server server = iterator.next();
            hostIndexMap.put(server.getHost(), index);
            index += 1;
        }

    }

    public int getIndex(String host){
        if(!hostIndexMap.containsKey(host)){
            return -1;
        }
        return hostIndexMap.get(host);
    }

    /**
     * server comparator
     */
    private class ServerComparator implements Comparator<Server> {
        @Override
        public int compare(Server o1, Server o2) {
            return o1.getCreateTime().before(o2.getCreateTime()) ? 0 : 1;
        }
    }


}
