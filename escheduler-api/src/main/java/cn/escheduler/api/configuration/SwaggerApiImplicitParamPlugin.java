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

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.List;
import java.util.Locale;

import static com.google.common.base.Strings.emptyToNull;
import static springfox.documentation.schema.Types.isBaseType;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.readers.parameter.Examples.examples;
import static springfox.documentation.swagger.schema.ApiModelProperties.allowableValueFromString;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiImplicitParamPlugin implements OperationBuilderPlugin {

    @Autowired
    private DescriptionResolver descriptions;

    @Autowired
    private MessageSource messageSource;

    static Parameter implicitParameter(MessageSource messageSource, DescriptionResolver descriptions, ApiImplicitParam param) {
        Locale locale = LocaleContextHolder.getLocale();

        ModelRef modelRef = maybeGetModelRef(param);
        return new ParameterBuilder()
                .name(param.name())
                .description(descriptions.resolve(messageSource.getMessage(param.value(), null, locale)))
                .defaultValue(param.defaultValue())
                .required(param.required())
                .allowMultiple(param.allowMultiple())
                .modelRef(modelRef)
                .allowableValues(allowableValueFromString(param.allowableValues()))
                .parameterType(emptyToNull(param.paramType()))
                .parameterAccess(param.access())
                .order(SWAGGER_PLUGIN_ORDER)
                .scalarExample(param.example())
                .complexExamples(examples(param.examples()))
                .build();
    }

    private static ModelRef maybeGetModelRef(ApiImplicitParam param) {
        String dataType = MoreObjects.firstNonNull(emptyToNull(param.dataType()), "string");
        AllowableValues allowableValues = null;
        if (isBaseType(dataType)) {
            allowableValues = allowableValueFromString(param.allowableValues());
        }
        if (param.allowMultiple()) {
            return new ModelRef("", new ModelRef(dataType, allowableValues));
        }
        return new ModelRef(dataType, allowableValues);
    }

    @Override
    public void apply(OperationContext context) {
        context.operationBuilder().parameters(readParameters(context));
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
    }

    private List<Parameter> readParameters(OperationContext context) {
        Optional<ApiImplicitParam> annotation = context.findAnnotation(ApiImplicitParam.class);
        List<Parameter> parameters = Lists.newArrayList();
        if (annotation.isPresent()) {
            parameters.add(implicitParameter(messageSource, descriptions, annotation.get()));
        }
        return parameters;
    }

}