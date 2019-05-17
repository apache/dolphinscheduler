/*
 *
 *  Copyright 2015-2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package cn.escheduler.api.configuration;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.Collections;
import springfox.documentation.schema.Enums;
import springfox.documentation.schema.Example;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.swagger.schema.ApiModelProperties;

import java.util.Locale;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.pluginDoesApply;
import static springfox.documentation.swagger.readers.parameter.Examples.examples;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiParamPlugin implements ParameterBuilderPlugin {
    @Autowired
    private DescriptionResolver descriptions;
    @Autowired
    private EnumTypeDeterminer enumTypeDeterminer;
    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(ParameterContext context) {
        Optional<ApiParam> apiParam = context.resolvedMethodParameter().findAnnotation(ApiParam.class);
        context.parameterBuilder()
                .allowableValues(allowableValues(
                        context.alternateFor(context.resolvedMethodParameter().getParameterType()),
                        apiParam.transform(toAllowableValue()).or("")));
        if (apiParam.isPresent()) {
            Locale locale = LocaleContextHolder.getLocale();

            ApiParam annotation = apiParam.get();
            context.parameterBuilder().name(emptyToNull(annotation.name()))
                    .description(emptyToNull(descriptions.resolve(messageSource.getMessage(annotation.value(), null, "",locale))))
                    .parameterAccess(emptyToNull(annotation.access()))
                    .defaultValue(emptyToNull(annotation.defaultValue()))
                    .allowMultiple(annotation.allowMultiple())
                    .allowEmptyValue(annotation.allowEmptyValue())
                    .required(annotation.required())
                    .scalarExample(new Example(annotation.example()))
                    .complexExamples(examples(annotation.examples()))
                    .hidden(annotation.hidden())
                    .collectionFormat(annotation.collectionFormat())
                    .order(SWAGGER_PLUGIN_ORDER);
        }
    }

    private Function<ApiParam, String> toAllowableValue() {
        return new Function<ApiParam, String>() {
            @Override
            public String apply(ApiParam input) {
                return input.allowableValues();
            }
        };
    }

    private AllowableValues allowableValues(ResolvedType parameterType, String allowableValueString) {
        AllowableValues allowableValues = null;
        if (!isNullOrEmpty(allowableValueString)) {
            allowableValues = ApiModelProperties.allowableValueFromString(allowableValueString);
        } else {
            if (enumTypeDeterminer.isEnum(parameterType.getErasedType())) {
                allowableValues = Enums.allowableValues(parameterType.getErasedType());
            }
            if (Collections.isContainerType(parameterType)) {
                allowableValues = Enums.allowableValues(Collections.collectionElementType(parameterType).getErasedType());
            }
        }
        return allowableValues;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return pluginDoesApply(delimiter);
    }
}

