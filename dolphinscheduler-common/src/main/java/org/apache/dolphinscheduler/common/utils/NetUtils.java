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

import static org.apache.dolphinscheduler.common.Constants.DOLPHIN_SCHEDULER_PREFERRED_NETWORK_INTERFACE;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetUtils
 */
public class NetUtils {

    private NetUtils() {
        throw new UnsupportedOperationException("Construct NetUtils");
    }

    private static Logger logger = LoggerFactory.getLogger(NetUtils.class);

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static String ANY_HOST_VALUE = "0.0.0.0";

    private static String LOCAL_HOST_VALUE = "127.0.0.1";

    private static InetAddress LOCAL_ADDRESS = null;

    private static volatile String HOST_ADDRESS;

    public static String getHost() {
        if (HOST_ADDRESS != null) {
            return HOST_ADDRESS;
        }

        InetAddress address = getLocalAddress();
        if (address != null) {
            HOST_ADDRESS = address.getHostAddress();
            return HOST_ADDRESS;
        }
        return LOCAL_HOST_VALUE;
    }

    private static InetAddress getLocalAddress() {
        if (null != LOCAL_ADDRESS) {
            return LOCAL_ADDRESS;
        }
        return getLocalAddress0();
    }

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    private static synchronized InetAddress getLocalAddress0() {
        if (null != LOCAL_ADDRESS) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = null;
        NetworkInterface networkInterface = findNetworkInterface();
        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
            Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
            if (addressOp.isPresent()) {
                try {
                    if (addressOp.get().isReachable(100)) {
                        LOCAL_ADDRESS = addressOp.get();
                        return LOCAL_ADDRESS;
                    }
                } catch (IOException e) {
                    logger.warn("test address id reachable io exception", e);
                }
            }
        }

        try {
            localAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.warn("InetAddress get LocalHost exception", e);
        }
        Optional<InetAddress> addressOp = toValidAddress(localAddress);
        if (addressOp.isPresent()) {
            LOCAL_ADDRESS = addressOp.get();
        }
        return LOCAL_ADDRESS;
    }

    private static Optional<InetAddress> toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address v6Address = (Inet6Address) address;
            if (isPreferIPV6Address()) {
                return Optional.ofNullable(normalizeV6Address(v6Address));
            }
        }
        if (isValidV4Address(address)) {
            return Optional.of(address);
        }
        return Optional.empty();
    }

    private static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                logger.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    public static boolean isValidV4Address(InetAddress address) {

        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANY_HOST_VALUE.equals(name)
                && !LOCAL_HOST_VALUE.equals(name));
    }

    /**
     * Check if an ipv6 address
     *
     * @return true if it is reachable
     */
    private static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    /**
     * Get the suitable {@link NetworkInterface}
     *
     * @return If no {@link NetworkInterface} is available , return <code>null</code>
     */
    private static NetworkInterface findNetworkInterface() {

        List<NetworkInterface> validNetworkInterfaces = emptyList();

        try {
            validNetworkInterfaces = getValidNetworkInterfaces();
        } catch (SocketException e) {
            logger.warn("ValidNetworkInterfaces exception", e);
        }

        NetworkInterface result = null;
        // Try to specify config NetWork Interface
        for (NetworkInterface networkInterface : validNetworkInterfaces) {
            if (isSpecifyNetworkInterface(networkInterface)) {
                result = networkInterface;
                break;
            }
        }

        if (null != result) {
            return result;
        }
        return validNetworkInterfaces.get(0);
    }

    /**
     * Get the valid {@link NetworkInterface network interfaces}
     *
     * @throws SocketException SocketException if an I/O error occurs.
     */
    private static List<NetworkInterface> getValidNetworkInterfaces() throws SocketException {
        List<NetworkInterface> validNetworkInterfaces = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (ignoreNetworkInterface(networkInterface)) { // ignore
                continue;
            }
            validNetworkInterfaces.add(networkInterface);
        }
        return validNetworkInterfaces;
    }

    /**
     * @param networkInterface {@link NetworkInterface}
     * @return if the specified {@link NetworkInterface} should be ignored, return <code>true</code>
     * @throws SocketException SocketException if an I/O error occurs.
     */
    public static boolean ignoreNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        return networkInterface == null
                || networkInterface.isLoopback()
                || networkInterface.isVirtual()
                || !networkInterface.isUp();
    }

    private static boolean isSpecifyNetworkInterface(NetworkInterface networkInterface) {
        String preferredNetworkInterface = System.getProperty(DOLPHIN_SCHEDULER_PREFERRED_NETWORK_INTERFACE);
        return Objects.equals(networkInterface.getDisplayName(), preferredNetworkInterface);
    }
}
