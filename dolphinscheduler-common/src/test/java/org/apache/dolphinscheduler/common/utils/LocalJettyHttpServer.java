package org.apache.dolphinscheduler.common.utils;

import java.io.IOException;
import java.io.OutputStream;

import junit.extensions.TestSetup;
import junit.framework.Test;


import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LocalJettyHttpServer extends TestSetup {
    protected static Server server;
    private static Logger logger = LoggerFactory.getLogger(LocalJettyHttpServer.class);

    public LocalJettyHttpServer(Test suite) {
        super(suite);
    }

    protected void setUp() throws Exception {
        logger.info("server si starting...");
        server = new Server(8888);
        ContextHandler context = new ContextHandler("/test.json");
        context.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, HttpServletRequest request, HttpServletResponse response, int i) throws IOException {
                ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
                writer.write("{\"name\":\"Github\"}");
                writer.flush();
                response.setContentLength(writer.size());
                OutputStream out = response.getOutputStream();
                writer.writeTo(out);
                out.flush();
                Request baseRequest = request instanceof Request ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
                baseRequest.setHandled(true);
            }
        });
        server.setHandler(context);
        logger.info("server for " + context.getBaseResource());
        server.start();

    }
    protected void tearDown() throws Exception {
        logger.info("server stopping...");
        server.stop();
    }

}
