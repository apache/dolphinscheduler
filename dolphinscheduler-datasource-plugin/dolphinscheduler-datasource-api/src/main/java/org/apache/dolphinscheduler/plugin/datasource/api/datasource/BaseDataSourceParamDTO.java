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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Basic datasource params submitted to api, each datasource plugin should have implementation.
 */
public abstract class BaseDataSourceParamDTO implements Serializable {

    protected Integer id;

    protected String name;

    protected String note;

    protected String host;

    protected Integer port;

    protected String database;

    protected String userName;

    protected String password;

    protected Map<String, String> other;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * extract the host and port from the address,
     * then set it
     * @param address address like 'jdbc:mysql://host:port' or 'jdbc:hive2://zk1:port,zk2:port,zk3:port'
     */
    public void setHostAndPortByAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address is null.");
        }
        address = address.trim();

        int doubleSlashIndex = address.indexOf(Constants.DOUBLE_SLASH);
        // trim address like 'jdbc:mysql://host:port/xxx' ends with '/xxx'
        int slashIndex = address.indexOf(Constants.SLASH, doubleSlashIndex + 2);
        String hostPortString = slashIndex == -1 ? address.substring(doubleSlashIndex + 2)
                : address.substring(doubleSlashIndex + 2, slashIndex);

        ArrayList<String> hosts = new ArrayList<>();
        String portString = null;
        for (String hostPort : hostPortString.split(Constants.COMMA)) {
            String[] parts = hostPort.split(Constants.COLON);
            hosts.add(parts[0]);
            if (portString == null && parts.length > 1)
                portString = parts[1];
        }
        if (hosts.size() == 0 || portString == null) {
            throw new IllegalArgumentException(String.format("host:port '%s' illegal.", hostPortString));
        }

        this.host = String.join(Constants.COMMA, hosts);
        this.port = Integer.parseInt(portString);
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getOther() {
        return other;
    }

    public void setOther(Map<String, String> other) {
        this.other = other;
    }

    /**
     * Get the datasource type
     * see{@link DbType}
     *
     * @return datasource type code
     */
    public abstract DbType getType();
}
