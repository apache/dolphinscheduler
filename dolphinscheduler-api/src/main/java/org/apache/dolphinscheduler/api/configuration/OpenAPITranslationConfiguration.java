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


public class OpenAPITranslationConfiguration {

    /*@Component
    @RequiredArgsConstructor
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static class TranslationOperationBuilderPlugin implements OperationBuilderPlugin {

        private final MessageSource messageSource;

        @Override
        public boolean supports(DocumentationType delimiter) {
            return true;
        }

        @Override
        public void apply(OperationContext context) {
            Locale locale = LocaleContextHolder.getLocale();
            Operation operation = context.operationBuilder().build();
            String notes = operation.getNotes();
            notes = messageSource.getMessage(notes, null, description, locale);

            Set<String> tags = operation.getTags().stream()
                    .map(tag -> messageSource.getMessage(tag, null, tag, locale))
                    .collect(toSet());

            Collection<RequestParameter> parameters = operation.getRequestParameters().stream()
            .map(it -> new RequestParameter(
                it.getName(),
                it.getIn(),
                messageSource.getMessage(it.getDescription(), null, it.getDescription(), locale),
                it.getRequired(),
                it.getDeprecated(),
                it.getHidden(),
                it.getParameterSpecification(),
                it.getScalarExample(),
                it.getExamples(),
                it.getPrecedence(),
                it.getExtensions(),
                it.getParameterIndex()))
                .collect(toList());

            context.operationBuilder()
                    .notes(notes)
                    .requestParameters(parameters)
                    .tags(tags);
        }
    }*/
}
