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
package org.apache.dolphinscheduler.server.log;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.*;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class LoggerRequestProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(LoggerRequestProcessor.class);

    private final ThreadPoolExecutor executor;

    public LoggerRequestProcessor(){
        this.executor = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }

    @Override
    public void process(Channel channel, Command command) {
        logger.info("received command : {}", command);
        final CommandType commandType = command.getType();
        switch (commandType){
            case GET_LOG_REQ:
                GetLogRequestCommand getLogRequest = FastJsonSerializer.deserialize(command.getBody(), GetLogRequestCommand.class);
                byte[] bytes = getFileBytes(getLogRequest.getPath());
                GetLogResponseCommand getLogResponse = new GetLogResponseCommand(bytes);
                channel.writeAndFlush(getLogResponse.convert2Command(command.getOpaque()));
                break;
            case VIEW_LOG_REQ:
                ViewLogRequestCommand viewLogRequest = FastJsonSerializer.deserialize(command.getBody(), ViewLogRequestCommand.class);
                String msg = readFile(viewLogRequest.getPath());
                ViewLogResponseCommand viewLogResponse = new ViewLogResponseCommand(msg);
                channel.writeAndFlush(viewLogResponse.convert2Command(command.getOpaque()));
                break;
            case ROLL_VIEW_LOG_REQ:
                RollViewLogRequestCommand rollViewLogRequest = FastJsonSerializer.deserialize(command.getBody(), RollViewLogRequestCommand.class);
                List<String> lines = readFile(rollViewLogRequest.getPath(), rollViewLogRequest.getSkipLineNum(), rollViewLogRequest.getLimit());
                StringBuilder builder = new StringBuilder();
                for (String line : lines){
                    builder.append(line + "\r\n");
                }
                RollViewLogResponseCommand rollViewLogRequestResponse = new RollViewLogResponseCommand(builder.toString());
                channel.writeAndFlush(rollViewLogRequestResponse.convert2Command(command.getOpaque()));
                break;
            default:
                throw new IllegalArgumentException(String.format("unknown commandType : %s"));
        }
    }

    public ExecutorService getExecutor(){
        return this.executor;
    }

    /**
     * get files bytes
     *
     * @param path path
     * @return byte array of file
     * @throws Exception exception
     */
    private byte[] getFileBytes(String path){
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = new FileInputStream(path);
            bos  = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        }catch (IOException e){
            logger.error("get file bytes error",e);
        }finally {
            if (bos != null){
                try {
                    bos.close();
                } catch (IOException ignore) {}
            }
            if (in != null){
                try {
                    in.close();
                } catch (IOException ignore) {}
            }
        }
        return new byte[0];
    }

    /**
     * read file content
     *
     * @param path
     * @param skipLine
     * @param limit
     * @return
     */
    private List<String> readFile(String path, int skipLine, int limit){
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("read file failed",e);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * read  file content
     *
     * @param path path
     * @return string of file content
     * @throws Exception exception
     */
    private String readFile(String path){
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            while ((line = br.readLine()) != null){
                sb.append(line + "\r\n");
            }
            return sb.toString();
        }catch (IOException e){
            logger.error("read file failed",e);
        }finally {
            try {
                if (br != null){
                    br.close();
                }
            } catch (IOException ignore) {}
        }
        return "";
    }
}
