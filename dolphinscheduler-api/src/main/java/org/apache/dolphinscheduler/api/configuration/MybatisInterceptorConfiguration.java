package org.apache.dolphinscheduler.api.configuration;

import org.apache.dolphinscheduler.api.interceptor.CacheUpdateInterceptor;
import org.apache.dolphinscheduler.dao.interceptor.MybatisInterceptorRegistry;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisInterceptorConfiguration {

    @Autowired
    private MybatisInterceptorRegistry mybatisInterceptorRegistry;

    @Bean
    public CacheUpdateInterceptor cacheUpdateInterceptor() {
        return new CacheUpdateInterceptor();
    }

    @PostConstruct
    private void init() {
        mybatisInterceptorRegistry.addInterceptor(cacheUpdateInterceptor());
    }
}
