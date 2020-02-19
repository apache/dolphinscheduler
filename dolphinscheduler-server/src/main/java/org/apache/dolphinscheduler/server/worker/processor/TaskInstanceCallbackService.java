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

package org.apache.dolphinscheduler.server.worker.processor;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskAckCommand;
import org.apache.dolphinscheduler.remote.command.ExecuteTaskResponseCommand;

import java.util.concurrent.ConcurrentHashMap;

public class TaskInstanceCallbackService {

    private static final ConcurrentHashMap<Integer, CallbackChannel> CALL_BACK_CHANNELS = new ConcurrentHashMap<>();

    public void addCallbackChannel(int taskInstanceId, CallbackChannel channel){
        CALL_BACK_CHANNELS.put(taskInstanceId, channel);
    }

    public CallbackChannel getCallbackChannel(int taskInstanceId){
        CallbackChannel callbackChannel = CALL_BACK_CHANNELS.get(taskInstanceId);
        if(callbackChannel.getChannel().isActive()){
            return callbackChannel;
        }
        Channel newChannel = createChannel();
        callbackChannel.setChannel(newChannel);
        CALL_BACK_CHANNELS.put(taskInstanceId, callbackChannel);
        return callbackChannel;
    }

    public void remove(int taskInstanceId){
        CALL_BACK_CHANNELS.remove(taskInstanceId);
    }

    public void sendAck(int taskInstanceId, ExecuteTaskAckCommand ackCommand){
        CallbackChannel callbackChannel = getCallbackChannel(taskInstanceId);
        callbackChannel.getChannel().writeAndFlush(ackCommand.convert2Command(callbackChannel.getOpaque()));
    }

    public void sendResult(int taskInstanceId, ExecuteTaskResponseCommand responseCommand){
        CallbackChannel callbackChannel = getCallbackChannel(taskInstanceId);
        callbackChannel.getChannel().writeAndFlush(responseCommand.convert2Command()).addListener(new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    remove(taskInstanceId);
                    return;
                }
            }
        });
    }

    //TODO
    private Channel createChannel(){
        return null;
    }

}
