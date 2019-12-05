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
package org.apache.dolphinscheduler.api.configuration;

import org.apache.dolphinscheduler.api.interceptor.LoginHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;


/**
 * application configuration
 */
@Configuration
public class AppConfiguration implements WebMvcConfigurer {

  public static final String LOGIN_INTERCEPTOR_PATH_PATTERN = "/**/*";
  public static final String LOGIN_PATH_PATTERN = "/login";
  public static final String PATH_PATTERN = "/**";
  public static final String LOCALE_LANGUAGE_COOKIE = "language";
  public static final int COOKIE_MAX_AGE = 3600;


  @Bean
  public LoginHandlerInterceptor loginInterceptor() {
    return new LoginHandlerInterceptor();
  }


  /**
   * Cookie
   * @return local resolver
   */
  @Bean(name = "localeResolver")
  public LocaleResolver localeResolver() {
    CookieLocaleResolver localeResolver = new CookieLocaleResolver();
    localeResolver.setCookieName(LOCALE_LANGUAGE_COOKIE);
    /** set default locale **/
    localeResolver.setDefaultLocale(Locale.US);
    /** set cookie max age **/
    localeResolver.setCookieMaxAge(COOKIE_MAX_AGE);
    return localeResolver;
  }

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    /**  **/
    lci.setParamName("language");

    return lci;
  }


  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    //i18n
    registry.addInterceptor(localeChangeInterceptor());

    registry.addInterceptor(loginInterceptor()).addPathPatterns(LOGIN_INTERCEPTOR_PATH_PATTERN).excludePathPatterns(LOGIN_PATH_PATTERN,"/swagger-resources/**", "/webjars/**", "/v2/**", "/doc.html", "*.html", "/ui/**");
  }


  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    registry.addResourceHandler("/ui/**").addResourceLocations("file:ui/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/ui/").setViewName("forward:/ui/index.html");
    registry.addViewController("/").setViewName("forward:/ui/index.html");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(PATH_PATTERN).allowedOrigins("*").allowedMethods("*");
  }


  /**
   * Turn off suffix-based content negotiation
   *
   * @param configurer configurer
   */
  @Override
  public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }




}
