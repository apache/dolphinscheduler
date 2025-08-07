package org.apache.dolphinscheduler.plugin.task.grpc;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcCleanupExtension implements AfterEachCallback {

    private final List<CleanupTarget> cleanupTargets = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(GrpcCleanupExtension.class);
    private static final long TERMINATION_TIMEOUT_MS = 250L;
    private static final int MAX_NUM_TERMINATIONS = 100;

    public ManagedChannel addService(BindableService service) throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        Server server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .intercept(new ServerInterceptor() {
                    @Override
                    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
                        return null;
                    }
                })
                .addService(service)
                .build()
                .start();

        cleanupTargets.add(new ServerCleanupTarget(server));

        ManagedChannel channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        cleanupTargets.add(new ManagedChannelCleanupTarget(channel));

        return channel;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (CleanupTarget target : cleanupTargets) {
            try {
                int count = 0;
                target.shutdown();
                do {
                    target.awaitTermination(TERMINATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    count++;
                    if (count > MAX_NUM_TERMINATIONS) {
                        logger.error("Hit max count {} trying to shut down cleanupTarget {}", count, target);
                        break;
                    }
                } while (!target.isTerminated());
            } catch (Exception e) {
                logger.error("Problem shutting down cleanupTarget " + target, e);
            }
        }

        if (isAllTerminated()) {
            cleanupTargets.clear();
        } else {
            logger.error("Not all cleanupTargets are terminated");
        }
    }

    private boolean isAllTerminated() {
        for (CleanupTarget target : cleanupTargets) {
            if (!target.isTerminated()) {
                return false;
            }
        }
        return true;
    }

    interface CleanupTarget {
        void shutdown();
        boolean awaitTermination(long timeout, TimeUnit unit);
        boolean isTerminated();
    }

    static class ServerCleanupTarget implements CleanupTarget {
        private final Server server;

        ServerCleanupTarget(Server server) {
            this.server = server;
        }

        @Override
        public void shutdown() {
            server.shutdown();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            try {
                return server.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public boolean isTerminated() {
            return server.isTerminated();
        }
    }

    static class ManagedChannelCleanupTarget implements CleanupTarget {
        private final ManagedChannel channel;

        ManagedChannelCleanupTarget(ManagedChannel channel) {
            this.channel = channel;
        }

        @Override
        public void shutdown() {
            channel.shutdown();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            try {
                return channel.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public boolean isTerminated() {
            return channel.isTerminated();
        }
    }
}
