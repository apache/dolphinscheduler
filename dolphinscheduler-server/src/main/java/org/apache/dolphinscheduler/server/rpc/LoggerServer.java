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
package org.apache.dolphinscheduler.server.rpc;

import io.grpc.stub.StreamObserver;
import org.apache.dolphinscheduler.common.Constants;
import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.dolphinscheduler.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * logger server
 */
public class LoggerServer {

    private static  final Logger logger = LoggerFactory.getLogger(LoggerServer.class);

    /**
     * server
     */
    private Server server;

    /**
     * server start
     * @throws IOException io exception
     */
    public void start() throws IOException {
	    /* The port on which the server should run */
        int port = Constants.RPC_PORT;
        server = ServerBuilder.forPort(port)
                .addService(new LogViewServiceGrpcImpl())
                .build()
                .start();
        logger.info("server started, listening on port : {}" , port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.info("shutting down gRPC server since JVM is shutting down");
                LoggerServer.this.stop();
                logger.info("server shut down");
            }
        });
    }

    /**
     * stop
     */
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * main launches the server from the command line.
     */

    /**
     * main launches the server from the command line.
     * @param args arguments
     * @throws IOException          io exception
     * @throws InterruptedException interrupted exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final LoggerServer server = new LoggerServer();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * Log View Service Grpc Implementation
     */
    static class LogViewServiceGrpcImpl extends LogViewServiceGrpc.LogViewServiceImplBase {
        @Override
        public void rollViewLog(LogParameter request, StreamObserver<RetStrInfo> responseObserver) {

            logger.info("log parameter path : {} ,skip line : {}, limit : {}",
                    request.getPath(),
                    request.getSkipLineNum(),
                    request.getLimit());
            List<String> list = readFile(request.getPath(), request.getSkipLineNum(), request.getLimit());
            StringBuilder sb = new StringBuilder();
            boolean errorLineFlag = false;
            for (String line : list){
                sb.append(line + "\r\n");
            }
            RetStrInfo retInfoBuild = RetStrInfo.newBuilder().setMsg(sb.toString()).build();
            responseObserver.onNext(retInfoBuild);
            responseObserver.onCompleted();
        }

        @Override
        public void viewLog(PathParameter request, StreamObserver<RetStrInfo> responseObserver) {
            logger.info("task path is : {} " , request.getPath());
            RetStrInfo retInfoBuild = RetStrInfo.newBuilder().setMsg(readFile(request.getPath())).build();
            responseObserver.onNext(retInfoBuild);
            responseObserver.onCompleted();
        }

        @Override
        public void getLogBytes(PathParameter request, StreamObserver<RetByteInfo> responseObserver) {
            try {
                ByteString bytes = ByteString.copyFrom(getFileBytes(request.getPath()));
                RetByteInfo.Builder builder = RetByteInfo.newBuilder();
                builder.setData(bytes);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }catch (Exception e){
                logger.error("get log bytes failed",e);
            }
        }
    }

    /**
     * get files bytes
     *
     * @param path path
     * @return byte array of file
     * @throws Exception exception
     */
    private static byte[] getFileBytes(String path){
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * read file content
     *
     * @param path
     * @param skipLine
     * @param limit
     * @return
     */
    private static List<String> readFile(String path,int skipLine,int limit){
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("read file failed",e);
        }
        return null;
    }

    /**
     * read  file content
     *
     * @param path path
     * @return string of file content
     * @throws Exception exception
     */
    private static String readFile(String path){
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            boolean errorLineFlag = false;
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
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
        return null;
    }

}