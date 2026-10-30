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

package org.apache.dolphinscheduler.extract.base.authentication;

import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.base.config.NettyClientConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RpcAuthenticationConfiguration {

    @Bean
    public static RpcAuthenticationConfig rpcAuthenticationConfig(Environment environment) {
        if (!environment.getProperty("rpc.authentication.enabled", Boolean.class, false)) {
            return RpcAuthenticationConfig.disabled();
        }
        return RpcAuthenticationConfig.fromSecret(
                environment.getProperty("rpc.authentication.secret"),
                5000);
    }

    @Bean
    public static RpcClientInitializer rpcClientInitializer(RpcAuthenticationConfig authenticationConfig) {
        return new RpcClientInitializer(authenticationConfig);
    }

    /** Initialize the static client before any application bean can make RPC calls. */
    static final class RpcClientInitializer implements BeanFactoryPostProcessor, DisposableBean {

        private final RpcAuthenticationConfig authenticationConfig;
        private boolean initialized;

        RpcClientInitializer(RpcAuthenticationConfig authenticationConfig) {
            this.authenticationConfig = authenticationConfig;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            Clients.initialize(NettyClientConfig.builder().authentication(authenticationConfig).build());
            initialized = true;
        }

        @Override
        public void destroy() {
            if (initialized) {
                Clients.close();
            }
        }
    }
}
