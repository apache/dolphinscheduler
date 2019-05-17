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

import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiPlugin implements OperationBuilderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerApiPlugin.class);

    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(OperationContext context) {
        Locale locale = LocaleContextHolder.getLocale();

        List<Api> list = context.findAllAnnotations(Api.class);
        if (list.size() > 0) {
            Api api = list.get(0);

            Set<String> tagsSet = new HashSet<>(1);

            if(api.tags() != null && api.tags().length > 0){
                tagsSet.add(StringUtils.isNotBlank(api.tags()[0]) ? messageSource.getMessage(api.tags()[0], null, locale) : " ");
            }

            context.operationBuilder().hidden(api.hidden())
                    .tags(tagsSet).build();

        }

    }


    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

}
