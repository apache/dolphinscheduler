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
package org.apache.dolphinscheduler.remote.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPUtils {

    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);

    private static String IP_REGEX = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

    private static String LOCAL_HOST = "unknown";

    static {
        String host = System.getenv("HOSTNAME");
        if (isNotEmpty(host)) {
            LOCAL_HOST = host;
        } else {

            try {
                String hostName = InetAddress.getLocalHost().getHostName();
                if (isNotEmpty(hostName)) {
                    LOCAL_HOST = hostName;
                }
            } catch (UnknownHostException e) {
                logger.error("get hostName error!", e);
            }
        }
    }

    public static String getLocalHost() {
        return LOCAL_HOST;
    }


    public static String getFirstNoLoopbackIP4Address() {
        Collection<String> allNoLoopbackIP4Addresses = getNoLoopbackIP4Addresses();
        if (allNoLoopbackIP4Addresses.isEmpty()) {
            return null;
        }
        return allNoLoopbackIP4Addresses.iterator().next();
    }

    public static Collection<String> getNoLoopbackIP4Addresses() {
        Collection<String> noLoopbackIP4Addresses = new ArrayList<>();
        Collection<InetAddress> allInetAddresses = getAllHostAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()
                    && !Inet6Address.class.isInstance(address)) {
                noLoopbackIP4Addresses.add(address.getHostAddress());
            }
        }
        if (noLoopbackIP4Addresses.isEmpty()) {
            for (InetAddress address : allInetAddresses) {
                if (!address.isLoopbackAddress() && !Inet6Address.class.isInstance(address)) {
                    noLoopbackIP4Addresses.add(address.getHostAddress());
                }
            }
        }
        return noLoopbackIP4Addresses;
    }

    public static Collection<InetAddress> getAllHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Collection<InetAddress> addresses = new ArrayList<>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String getIpByHostName(String host) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            logger.error("get IP error", e);
        }
        if (address == null) {
            return "";
        }
        return address.getHostAddress();

    }

    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isIp(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        Pattern pat = Pattern.compile(IP_REGEX);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }
}
