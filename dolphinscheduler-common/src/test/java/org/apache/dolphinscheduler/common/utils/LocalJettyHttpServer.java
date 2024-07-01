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

package org.apache.dolphinscheduler.common.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJettyHttpServer extends TestSetup {

    protected static Server server;
    private static Logger logger = LoggerFactory.getLogger(LocalJettyHttpServer.class);
    private Integer serverPort = 0;

    public Integer getServerPort() {
        return serverPort;
    }

    public LocalJettyHttpServer(Test suite) {
        super(suite);
    }

    protected void setUp() throws Exception {
        server = new Server(serverPort);
        ContextHandler context = new ContextHandler("/test.json");
        context.setHandler(new AbstractHandler() {

            @Override
            public void handle(String s, HttpServletRequest request, HttpServletResponse response,
                               int i) throws IOException {
                ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
                writer.write("{\"name\":\"Github\"}");
                writer.flush();
                response.setContentLength(writer.size());
                OutputStream out = response.getOutputStream();
                writer.writeTo(out);
                out.flush();
                Request baseRequest = request instanceof Request ? (Request) request
                        : HttpConnection.getCurrentConnection().getRequest();
                baseRequest.setHandled(true);
            }
        });
        server.setHandler(context);
        logger.info("server for " + context.getBaseResource());
        server.start();
        serverPort = server.getConnectors()[0].getLocalPort();
        logger.info("server is starting in port: " + serverPort);
    }

    protected void tearDown() throws Exception {
        logger.info("server stopping...");
        server.stop();
    }

}
