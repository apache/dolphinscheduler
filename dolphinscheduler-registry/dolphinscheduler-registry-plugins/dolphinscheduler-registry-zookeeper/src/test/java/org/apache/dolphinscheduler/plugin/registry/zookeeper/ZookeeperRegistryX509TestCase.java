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

package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import org.apache.dolphinscheduler.plugin.registry.RegistryTestCase;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.client.ZKClientConfig;
import org.apache.zookeeper.common.ClientX509Util;
import org.apache.zookeeper.common.QuorumX509Util;
import org.apache.zookeeper.common.Time;
import org.apache.zookeeper.common.X509Exception;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.DumbWatcher;
import org.apache.zookeeper.server.NettyServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.admin.Commands;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.apache.zookeeper.server.auth.ProviderRegistry;
import org.apache.zookeeper.server.auth.X509AuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("x509")
@SpringBootTest(classes = ZookeeperRegistryProperties.class)
@SpringBootApplication(scanBasePackageClasses = ZookeeperRegistryProperties.class)
class ZookeeperRegistryX509TestCase extends RegistryTestCase<ZookeeperRegistry> {

    @Autowired
    private ZookeeperRegistryProperties zookeeperRegistryProperties;

    private static GenericContainer<?> zookeeperContainer;

    private static final Network NETWORK = Network.newNetwork();

    private static ZooKeeper zk;

    private static final String X509_SCHEMA = "x509";

    private static final String X509_SUBJECT_PRINCIPAL = "CN=localhost,OU=ZooKeeper,O=Apache,L=Unknown,ST=Unknown,C=Unknown";

    private static final ClientX509Util clientX509Util = new ClientX509Util();

    private static final QuorumX509Util quorumX509Util = new QuorumX509Util();

    public static class CountdownWatcher implements Watcher {

        // TODO this doesn't need to be volatile! (Should probably be final)
        volatile CountDownLatch clientConnected;
        // Set to true when connected to a read-only server, or a read-write (quorum) server.
        volatile boolean connected;
        // Set to true when connected to a quorum server.
        volatile boolean syncConnected;
        // Set to true when connected to a quorum server in read-only mode
        volatile boolean readOnlyConnected;

        public CountdownWatcher() {
            reset();
        }
        public synchronized void reset() {
            clientConnected = new CountDownLatch(1);
            connected = false;
            syncConnected = false;
            readOnlyConnected = false;
        }
        public synchronized void process(WatchedEvent event) {
            Event.KeeperState state = event.getState();
            if (state == Event.KeeperState.SyncConnected) {
                connected = true;
                syncConnected = true;
                readOnlyConnected = false;
            } else if (state == Event.KeeperState.ConnectedReadOnly) {
                connected = true;
                syncConnected = false;
                readOnlyConnected = true;
            } else {
                connected = false;
                syncConnected = false;
                readOnlyConnected = false;
            }

            notifyAll();
            if (connected) {
                clientConnected.countDown();
            }
        }
        public synchronized boolean isConnected() {
            return connected;
        }

        protected synchronized String connectionDescription() {
            return String.format("connected(%s), syncConnected(%s), readOnlyConnected(%s)",
                    connected, syncConnected, readOnlyConnected);
        }

        public synchronized void waitForConnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = Time.currentElapsedTime() + timeout;
            long left = timeout;
            while (!connected && left > 0) {
                wait(left);
                left = expire - Time.currentElapsedTime();
            }
            if (!connected) {
                throw new TimeoutException("Failed to connect to ZooKeeper server: " + connectionDescription());
            }
        }
        public synchronized void waitForSyncConnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = Time.currentElapsedTime() + timeout;
            long left = timeout;
            while (!syncConnected && left > 0) {
                wait(left);
                left = expire - Time.currentElapsedTime();
            }
            if (!syncConnected) {
                throw new TimeoutException(
                        "Failed to connect to read-write ZooKeeper server: "
                                + connectionDescription());
            }
        }
        public synchronized void waitForReadOnlyConnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = Time.currentElapsedTime() + timeout;
            long left = timeout;
            while (!readOnlyConnected && left > 0) {
                wait(left);
                left = expire - Time.currentElapsedTime();
            }
            if (!readOnlyConnected) {
                throw new TimeoutException(
                        "Failed to connect in read-only mode to ZooKeeper server: "
                                + connectionDescription());
            }
        }
        public synchronized void waitForDisconnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = Time.currentElapsedTime() + timeout;
            long left = timeout;
            while (connected && left > 0) {
                wait(left);
                left = expire - Time.currentElapsedTime();
            }
            if (connected) {
                throw new TimeoutException("Did not disconnect: " + connectionDescription());
            }
        }

    }

    private static void setupTLS() throws Exception {
        System.setProperty("zookeeper.authProvider.x509", "org.apache.zookeeper.server.auth.X509AuthenticationProvider");
        String testDataPath = System.getProperty("test.data.dir", "src/test/resources");

        System.setProperty(clientX509Util.getSslKeystoreLocationProperty(), testDataPath + "/ssl/testKeyStore.jks");
        System.setProperty(clientX509Util.getSslKeystorePasswdProperty(), "testpass");
        System.setProperty(clientX509Util.getSslTruststoreLocationProperty(), testDataPath + "/ssl/testTrustStore.jks");
        System.setProperty(clientX509Util.getSslTruststorePasswdProperty(), "testpass");

        // client
        System.setProperty(ZKClientConfig.ZOOKEEPER_CLIENT_CNXN_SOCKET, "org.apache.zookeeper.ClientCnxnSocketNetty");
        System.setProperty(ZKClientConfig.SECURE_CLIENT, "true");

        // server
        System.setProperty(ServerCnxnFactory.ZOOKEEPER_SERVER_CNXN_FACTORY, "org.apache.zookeeper.server.NettyServerCnxnFactory");
        System.setProperty(NettyServerCnxnFactory.PORT_UNIFICATION_KEY, Boolean.TRUE.toString());

        // admin server
        System.setProperty(quorumX509Util.getSslKeystoreLocationProperty(), testDataPath + "/ssl/testKeyStore.jks");
        System.setProperty(quorumX509Util.getSslKeystorePasswdProperty(), "testpass");
        System.setProperty(quorumX509Util.getSslTruststoreLocationProperty(), testDataPath + "/ssl/testTrustStore.jks");
        System.setProperty(quorumX509Util.getSslTruststorePasswdProperty(), "testpass");
        System.setProperty("zookeeper.admin.forceHttps", "true");
        System.setProperty("zookeeper.admin.needClientAuth", "true");

        // create SSLContext
        final SSLContext sslContext = SSLContext.getInstance(ClientX509Util.DEFAULT_PROTOCOL);
        final X509AuthenticationProvider authProvider = (X509AuthenticationProvider) ProviderRegistry.getProvider("x509");
        if (authProvider == null) {
            throw new X509Exception.SSLContextException("Could not create SSLContext with x509 auth provider");
        }
        sslContext.init(new X509KeyManager[]{authProvider.getKeyManager()}, new X509TrustManager[]{authProvider.getTrustManager()}, null);

        // set SSLSocketFactory
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    private static void clearTLS() {
        System.clearProperty("zookeeper.authProvider.x509");

        System.clearProperty(clientX509Util.getSslKeystoreLocationProperty());
        System.clearProperty(clientX509Util.getSslKeystorePasswdProperty());
        System.clearProperty(clientX509Util.getSslTruststoreLocationProperty());
        System.clearProperty(clientX509Util.getSslTruststorePasswdProperty());

        // client side
        System.clearProperty(ZKClientConfig.ZOOKEEPER_CLIENT_CNXN_SOCKET);
        System.clearProperty(ZKClientConfig.SECURE_CLIENT);

        // server side
        System.clearProperty(ServerCnxnFactory.ZOOKEEPER_SERVER_CNXN_FACTORY);
        System.clearProperty(NettyServerCnxnFactory.PORT_UNIFICATION_KEY);

        // admin server
        System.clearProperty(quorumX509Util.getSslKeystoreLocationProperty());
        System.clearProperty(quorumX509Util.getSslKeystorePasswdProperty());
        System.clearProperty(quorumX509Util.getSslTruststoreLocationProperty());
        System.clearProperty(quorumX509Util.getSslTruststorePasswdProperty());
        System.clearProperty("zookeeper.admin.forceHttps");
        System.clearProperty("zookeeper.admin.needClientAuth");
    }

    public static void addAuthInfoForX509(final ZooKeeper zk) {
        zk.addAuthInfo(X509_SCHEMA, X509_SUBJECT_PRINCIPAL.getBytes(StandardCharsets.UTF_8));
    }

    private static void setupRootACLForX509(final ZooKeeper zk) throws Exception  {
        final ACL acl = new ACL(ZooDefs.Perms.ALL, new Id(X509_SCHEMA, X509_SUBJECT_PRINCIPAL));
        zk.setACL("/", Collections.singletonList(acl), -1);
    }

    @SneakyThrows
    @BeforeAll
    public static void setUpTestingServer() {


        String testDataPath = System.getProperty("test.data.dir", "src/test/resources");
        zookeeperContainer = new GenericContainer<>(DockerImageName.parse("zookeeper:3.8"))
                .withNetwork(NETWORK)
                .withExposedPorts(2181)
                ;

        setupTLS();

        Startables.deepStart(Stream.of(zookeeperContainer)).join();
        System.clearProperty("registry.zookeeper.connect-string");
        System.setProperty("registry.zookeeper.connect-string", "localhost:" + zookeeperContainer.getMappedPort(2181));
        zk = new ZooKeeper("localhost:" + zookeeperContainer.getMappedPort(2181),
                30000, new CountdownWatcher(), new ZKClientConfig());
        addAuthInfoForX509(zk);
        setupRootACLForX509(zk);

    }

    @SneakyThrows
    @Override
    public ZookeeperRegistry createRegistry() {
        return new ZookeeperRegistry(zookeeperRegistryProperties);
    }

    @SneakyThrows
    @AfterAll
    public static void tearDownTestingServer() {
        clearTLS();
        zk.close();
        zookeeperContainer.close();
    }
}