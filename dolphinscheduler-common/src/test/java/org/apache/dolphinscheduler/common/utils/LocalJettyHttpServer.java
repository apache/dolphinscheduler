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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
                if ("/test.json".equals(target)) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding(StandardCharsets.ISO_8859_1.name());
                    response.setStatus(HttpServletResponse.SC_OK);
                    try (PrintWriter writer = response.getWriter()) {
                        writer.write("{\"name\":\"Github\"}");
                        writer.flush();
                    }
                    baseRequest.setHandled(true);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    baseRequest.setHandled(true);
                }
            }
        });
        server.start();
        serverPort = server.getURI().getPort();
        logger.info("server is starting in port: " + serverPort);
    }

    protected void tearDown() throws Exception {
        logger.info("server stopping...");
        server.stop();
    }

}