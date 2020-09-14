package org.apache.dolphinscheduler.server.worker.cache;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.remote.command.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResponceCache {

    private static volatile ResponceCache instance = null;

    public static ResponceCache get(){
        if (instance == null){
            synchronized (ResponceCache.class){
                if (instance == null){
                    instance = new ResponceCache();
                }
            }
        }
        return instance;
    }

    private Map<Integer,Command> ackCache = new ConcurrentHashMap<>();
    private Map<Integer,Command> responseCache = new ConcurrentHashMap<>();


    public void cache(Integer taskInstanceId, Command command, Event event){
        switch (event){
            case ACK:
                ackCache.put(taskInstanceId,command);
                break;
            case RESULT:
                responseCache.put(taskInstanceId,command);
                break;
            default:
                throw new IllegalArgumentException("invalid event type : " + event);
        }
    }


    public void removeAckCache(Integer taskInstanceId){
        ackCache.remove(taskInstanceId);
    }
    public void removeResponseCache(Integer taskInstanceId){
        responseCache.remove(taskInstanceId);
    }

    public Map<Integer,Command> getAckCache(){
        return ackCache;
    }

    public Map<Integer,Command> getResponseCache(){
        return responseCache;
    }
}
