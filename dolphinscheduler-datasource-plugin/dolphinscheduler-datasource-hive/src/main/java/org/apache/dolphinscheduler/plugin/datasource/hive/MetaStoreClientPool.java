package org.apache.dolphinscheduler.plugin.datasource.hive;

import com.google.common.base.Preconditions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaHook;
import org.apache.hadoop.hive.metastore.HiveMetaHookLoader;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Manages a pool of RetryingMetaStoreClient connections. If the connection pool is empty
 * a new client is created and added to the pool. The idle pool can expand till a maximum
 * size of MAX_HMS_CONNECTION_POOL_SIZE, beyond which the connections are closed.
 *
 * This default implementation reads the Hive metastore configuration from the HiveConf
 * object passed in the c'tor. If you are looking for a temporary HMS instance created
 * from scratch for unit tests, refer to EmbeddedMetastoreClientPool class. It mocks an
 * actual HMS by creating a temporary Derby backend database on the fly. It should not
 * be used for production Catalog server instances.
 */
public class MetaStoreClientPool {
    private static final int DEFAULT_HIVE_METASTORE_CNXN_DELAY_MS_CONF = 0;
    // Maximum number of idle metastore connections in the connection pool at any point.
    private static final int MAX_HMS_CONNECTION_POOL_SIZE = 32;
    // Number of milliseconds to sleep between creation of HMS connections. Used to debug
    private final int clientCreationDelayMs_;

    private static final Logger LOG = Logger.getLogger(MetaStoreClientPool.class);

    private final ConcurrentLinkedQueue<MetaStoreClient> clientPool_ =
            new ConcurrentLinkedQueue<MetaStoreClient>();
    private Boolean poolClosed_ = false;
    private final Object poolCloseLock_ = new Object();
    private final HiveConf hiveConf_;

    // Required for creating an instance of RetryingMetaStoreClient.
    private static final HiveMetaHookLoader dummyHookLoader = new HiveMetaHookLoader() {
        @Override
        public HiveMetaHook getHook(org.apache.hadoop.hive.metastore.api.Table tbl)
                throws MetaException {
            return null;
        }
    };

    public class MetaStoreClient implements AutoCloseable {
        private final HiveMetaStoreClient hiveMetaStoreClient_;
        private boolean isInUse_;

        /**
         * Creates a new instance of MetaStoreClient.
         * 'cnxnTimeoutSec' specifies the time MetaStoreClient will wait to establish first
         * connection to the HMS before giving up and failing out with an exception.
         */
        private MetaStoreClient(HiveConf hiveConf, int cnxnTimeoutSec) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Creating MetaStoreClient. Pool Size = " + clientPool_.size());
            }

            long retryDelaySeconds = hiveConf.getTimeVar(
                    HiveConf.ConfVars.METASTORE_CLIENT_CONNECT_RETRY_DELAY, TimeUnit.SECONDS);
            long retryDelayMillis = retryDelaySeconds * 1000;
            long endTimeMillis = System.currentTimeMillis() + cnxnTimeoutSec * 1000;
            HiveMetaStoreClient hiveMetaStoreClient = null;
            while (true) {
                try {
                    hiveMetaStoreClient = new HiveMetaStoreClient(hiveConf);
                    break;
                } catch (Exception e) {
                    // If time is up, throw an unchecked exception
                    long delayUntilMillis = System.currentTimeMillis() + retryDelayMillis;
                    if (delayUntilMillis >= endTimeMillis) {
                        throw new IllegalStateException(e);
                    }
                    LOG.warn("Failed to connect to Hive MetaStore. Retrying.", e);
                    while (delayUntilMillis > System.currentTimeMillis()) {
                        try {
                            Thread.sleep(delayUntilMillis - System.currentTimeMillis());
                        } catch (InterruptedException | IllegalArgumentException ignore) {} }
                }
            }
            hiveMetaStoreClient_ = hiveMetaStoreClient;
            isInUse_ = false;
        }

        /**
         * Returns the internal RetryingMetaStoreClient object.
         */
        public HiveMetaStoreClient getHiveClient() {
            return hiveMetaStoreClient_;
        }

        /**
         * Returns this client back to the connection pool. If the connection pool has been
         * closed, just close the Hive client connection.
         */
        @Override
        public void close() {
            Preconditions.checkState(isInUse_);
            isInUse_ = false;
            synchronized (poolCloseLock_) {
                if (poolClosed_ || clientPool_.size() >= MAX_HMS_CONNECTION_POOL_SIZE) {
                    hiveMetaStoreClient_.close();
                } else {
                    clientPool_.offer(this);
                }
            }
        }
        // Marks this client as in use
        private void markInUse() {
            Preconditions.checkState(!isInUse_);
            isInUse_ = true;
        }
    }

    public MetaStoreClientPool(int initialSize, int initialCnxnTimeoutSec, Configuration hadoopConf) {
        this(initialSize, initialCnxnTimeoutSec, new HiveConf(MetaStoreClientPool.class), hadoopConf);
    }

    public MetaStoreClientPool(int initialSize, int initialCnxnTimeoutSec,
                               HiveConf hiveConf, Configuration hadoopConf) {
        hiveConf_ = hiveConf;
        hiveConf_.addResource(hadoopConf);
        clientCreationDelayMs_ = DEFAULT_HIVE_METASTORE_CNXN_DELAY_MS_CONF;
        initClients(initialSize, initialCnxnTimeoutSec);
    }

    /**
     * Initialize client pool with 'numClients' client.
     * 'initialCnxnTimeoutSec' specifies the time (in seconds) the first client will wait to
     * establish an initial connection to the HMS.
     */
    public void initClients(int numClients, int initialCnxnTimeoutSec) {
        Preconditions.checkState(clientPool_.size() == 0);
        if (numClients > 0) {
            clientPool_.add(new MetaStoreClient(hiveConf_, initialCnxnTimeoutSec));
            for (int i = 0; i < numClients - 1; ++i) {
                clientPool_.add(new MetaStoreClient(hiveConf_, 0));
            }
        }
    }
    /**
     * Gets a client from the pool. If the pool is empty a new client is created.
     */
    public MetaStoreClient getClient() {
        if (Thread.currentThread().getContextClassLoader() == null) {
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        }
        MetaStoreClient client = clientPool_.poll();
        if (client == null) {
            synchronized (this) {
                try {
                    Thread.sleep(clientCreationDelayMs_);
                } catch (InterruptedException e) {
                    /* ignore */
                }
                client = new MetaStoreClient(hiveConf_, 0);
            }
        }
        client.markInUse();
        return client;
    }

    /**
     * Removes all items from the connection pool and closes all Hive Meta Store client
     * connections. Can be called multiple times.
     */
    public void close() {
        // Ensure no more items get added to the pool once close is called.
        synchronized (poolCloseLock_) {
            if (poolClosed_) {
                return;
            }
            poolClosed_ = true;
        }
        MetaStoreClient client = null;
        while ((client = clientPool_.poll()) != null) {
            client.getHiveClient().close();
        }
    }
}
