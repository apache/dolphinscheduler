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

package org.apache.dolphinscheduler.api.aspect;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.SensitivePropertyUtils;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * HTTP outbound mask for sensitive {@code Property} values.
 * <p>
 * Service / persistence stay plaintext. Only {@link Result} bodies whose {@code data}
 * contains workflow/task {@code Property} payloads are copied then masked; other
 * {@link Result} responses are returned unchanged.
 */
@RestControllerAdvice(basePackages = "org.apache.dolphinscheduler.api.controller")
@SuppressWarnings("unchecked")
public class SensitivePropertyResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType() != null
                && Result.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(body instanceof Result)) {
            return body;
        }
        Result<Object> result = (Result<Object>) body;
        Object data = result.getData();
        if (!SensitivePropertyUtils.containsSensitivePropertyPayload(data)) {
            return result;
        }
        result.setData(SensitivePropertyUtils.maskApiResponseData(data));
        return result;
    }
}
