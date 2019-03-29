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
package cn.escheduler.api.configuration;

import cn.escheduler.api.interceptor.LoginHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * application configuration
 */
@Configuration
public class AppConfiguration extends WebMvcConfigurerAdapter {

  public static final String LOGIN_INTERCEPTOR_PATH_PATTERN = "/**/*";
  public static final String LOGIN_PATH_PATTERN = "/login";
  public static final String PATH_PATTERN = "/**";

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor()).addPathPatterns(LOGIN_INTERCEPTOR_PATH_PATTERN).excludePathPatterns(LOGIN_PATH_PATTERN);
  }

  @Bean
  public LoginHandlerInterceptor loginInterceptor() {
    return new LoginHandlerInterceptor();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(PATH_PATTERN).allowedOrigins("*").allowedMethods("*");
  }


  /**
   * Turn off suffix-based content negotiation
   *
   * @param configurer
   */
  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }
}
