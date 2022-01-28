package org.apache.dolphinscheduler.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("python-gateway")
public class PythonGatewayConfig {
    private String gatewayServerAddress;
    private int gatewayServerPort;
    private String pythonAddress;
    private int pythonPort;
    private int connectTimeout;
    private int readTimeout;

    public String getGatewayServerAddress() {
        return gatewayServerAddress;
    }

    public void setGatewayServerAddress(String gatewayServerAddress) {
        this.gatewayServerAddress = gatewayServerAddress;
    }

    public int getGatewayServerPort() {
        return gatewayServerPort;
    }

    public void setGatewayServerPort(int gatewayServerPort) {
        this.gatewayServerPort = gatewayServerPort;
    }

    public String getPythonAddress() {
        return pythonAddress;
    }

    public void setPythonAddress(String pythonAddress) {
        this.pythonAddress = pythonAddress;
    }

    public int getPythonPort() {
        return pythonPort;
    }

    public void setPythonPort(int pythonPort) {
        this.pythonPort = pythonPort;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
