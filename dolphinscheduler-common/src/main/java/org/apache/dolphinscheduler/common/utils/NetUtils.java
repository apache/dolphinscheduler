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

import static java.util.Collections.emptyList;

import org.apache.dolphinscheduler.common.constants.Constants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;

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

    private static final String NETWORK_PRIORITY_DEFAULT = "default";
    private static final String NETWORK_PRIORITY_INNER = "inner";
    private static final String NETWORK_PRIORITY_OUTER = "outer";

    private static final int IPV4_SEMICOLON_PART = 2;

    private static final String IPV6_STARTER = "[";

    private static final String IPV6_END = "]";
    private static final String COLON = ":";

    private static final String PORT_PATTERN = "^([1-9]|[1-9]\\d{1,4}|[1-6][0-5][0-5][0-3][0-5])$";
    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);
    private static InetAddress LOCAL_ADDRESS = null;
    private static volatile String HOST_ADDRESS;

    private NetUtils() {
        throw new UnsupportedOperationException("Construct NetUtils");
    }

    /**
     * get addr like host:port
     * @return addr
     */
    public static String getAddr(String host, int port) {
        return String.format("%s:%d", host, port);
    }

    /**
     * get addr like host:port
     * @return addr
     */
    public static String getAddr(int port) {
        return getAddr(getHost(), port);
    }

    /**
     * get host
     * @return host
     */
    public static String getHost(InetAddress inetAddress) {
        if (inetAddress != null) {
            if (KubernetesUtils.isKubernetesMode()) {
                String canonicalHost = inetAddress.getCanonicalHostName();
                String[] items = canonicalHost.split("\\.");
                if (items.length == 6 && "svc".equals(items[3])) {
                    return String.format("%s.%s", items[0], items[1]);
                }
                return canonicalHost;
            }
            return inetAddress.getHostAddress();
        }
        return null;
    }

    public static String getHost() {
        if (HOST_ADDRESS != null) {
            return HOST_ADDRESS;
        }

        InetAddress address = getLocalAddress();
        if (address != null) {
            HOST_ADDRESS = getHost(address);
            return HOST_ADDRESS;
        }
        return KubernetesUtils.isKubernetesMode() ? "localhost" : "127.0.0.1";
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
        try {
            NetworkInterface networkInterface = findNetworkInterface();
            if (networkInterface != null) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                    if (addressOp.isPresent()) {
                        try {
                            if (addressOp.get().isReachable(200)) {
                                LOCAL_ADDRESS = addressOp.get();
                                return LOCAL_ADDRESS;
                            }
                        } catch (IOException e) {
                            logger.warn("test address id reachable io exception", e);
                        }
                    }
                }
            }

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
                && InetAddressUtils.isIPv4Address(name)
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress());
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
        return findAddress(validNetworkInterfaces);
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
            // ignore
            if (ignoreNetworkInterface(networkInterface)) {
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
        String preferredNetworkInterface =
                PropertyUtils.getString(Constants.DOLPHIN_SCHEDULER_NETWORK_INTERFACE_PREFERRED,
                        System.getProperty(Constants.DOLPHIN_SCHEDULER_NETWORK_INTERFACE_PREFERRED));
        return Objects.equals(networkInterface.getDisplayName(), preferredNetworkInterface);
    }

    private static NetworkInterface findAddress(List<NetworkInterface> validNetworkInterfaces) {
        if (CollectionUtils.isEmpty(validNetworkInterfaces)) {
            return null;
        }
        String networkPriority = PropertyUtils.getString(Constants.DOLPHIN_SCHEDULER_NETWORK_PRIORITY_STRATEGY,
                NETWORK_PRIORITY_DEFAULT);
        if (NETWORK_PRIORITY_DEFAULT.equalsIgnoreCase(networkPriority)) {
            return findAddressByDefaultPolicy(validNetworkInterfaces);
        } else if (NETWORK_PRIORITY_INNER.equalsIgnoreCase(networkPriority)) {
            return findInnerAddress(validNetworkInterfaces);
        } else if (NETWORK_PRIORITY_OUTER.equalsIgnoreCase(networkPriority)) {
            return findOuterAddress(validNetworkInterfaces);
        } else {
            logger.error("There is no matching network card acquisition policy!");
            return null;
        }
    }

    private static NetworkInterface findAddressByDefaultPolicy(List<NetworkInterface> validNetworkInterfaces) {
        NetworkInterface networkInterface;
        networkInterface = findInnerAddress(validNetworkInterfaces);
        if (networkInterface == null) {
            networkInterface = findOuterAddress(validNetworkInterfaces);
            if (networkInterface == null) {
                networkInterface = validNetworkInterfaces.get(0);
            }
        }
        return networkInterface;
    }

    /**
     * Get the Intranet IP
     *
     * @return If no {@link NetworkInterface} is available , return <code>null</code>
     */
    private static NetworkInterface findInnerAddress(List<NetworkInterface> validNetworkInterfaces) {

        NetworkInterface networkInterface = null;
        for (NetworkInterface ni : validNetworkInterfaces) {
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress ip = address.nextElement();
                if (ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()) {
                    networkInterface = ni;
                }
            }
        }
        return networkInterface;
    }

    private static NetworkInterface findOuterAddress(List<NetworkInterface> validNetworkInterfaces) {
        NetworkInterface networkInterface = null;
        for (NetworkInterface ni : validNetworkInterfaces) {
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress ip = address.nextElement();
                if (!ip.isSiteLocalAddress()
                        && !ip.isLoopbackAddress()) {
                    networkInterface = ni;
                }
            }
        }
        return networkInterface;
    }

    /**
     * check if address is legal address, a legal address should be:
     * ipv4: ip:port
     * ipv6: [ip]:port
     * @param ipAndPortAddress address to check
     * @return true if address is legal
     */
    public static boolean isLegalAddress(String ipAndPortAddress) {
        if (StringUtils.isEmpty(ipAndPortAddress)) {
            return false;
        }

        String[] ipAndPort = ipAndPortAddress.split(COLON);
        if (ipAndPort.length == IPV4_SEMICOLON_PART) {
            return isValidIPv4Address(ipAndPort[0]) && isValidPort(ipAndPort[1]);
        }

        if (ipAndPortAddress.startsWith(IPV6_STARTER)) {
            String[] ipv6Formats = ipAndPortAddress.split(IPV6_END);
            return isValidIPv6Address(ipv6Formats[0].replace(IPV6_STARTER, ""))
                    && isValidPort(ipv6Formats[1].replace(COLON, ""));
        }

        return false;
    }

    public static boolean isValidIPv4Address(String ipAddress) {
        if (StringUtils.isEmpty(ipAddress)) {
            return false;
        }
        return InetAddressUtils.isIPv4Address(ipAddress);
    }

    public static boolean isValidIPv6Address(String ipAddress) {
        if (StringUtils.isEmpty(ipAddress)) {
            return false;
        }
        return InetAddressUtils.isIPv6Address(ipAddress);
    }

    public static boolean isValidPort(String port) {
        if (StringUtils.isEmpty(port)) {
            return false;
        }
        return Pattern.matches(PORT_PATTERN, port);
    }

}
