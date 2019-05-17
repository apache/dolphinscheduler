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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;

import java.util.List;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiImplicitParamsPlugin implements OperationBuilderPlugin {

    @Autowired
    private DescriptionResolver descriptions;
    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(OperationContext context) {
        context.operationBuilder().parameters(readParameters(context));
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return pluginDoesApply(delimiter);
    }

    private List<Parameter> readParameters(OperationContext context) {
        Optional<ApiImplicitParams> annotation = context.findAnnotation(ApiImplicitParams.class);

        List<Parameter> parameters = Lists.newArrayList();
        if (annotation.isPresent()) {
            for (ApiImplicitParam param : annotation.get().value()) {
                parameters.add(SwaggerApiImplicitParamPlugin.implicitParameter(messageSource, descriptions, param));
            }
        }

        return parameters;
    }


}