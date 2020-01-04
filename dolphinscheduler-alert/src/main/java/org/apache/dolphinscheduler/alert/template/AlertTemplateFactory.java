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
package org.apache.dolphinscheduler.alert.template;

import org.apache.dolphinscheduler.alert.exception.NotSupportOperatorException;
import org.apache.dolphinscheduler.alert.template.impl.DefaultHTMLTemplate;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the alert template factory
 */
public class AlertTemplateFactory {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplateFactory.class);

    private static final String alertTemplate = PropertyUtils.getString(Constants.ALERT_TEMPLATE);

    private AlertTemplateFactory(){}

    /**
     * get a template from alert.properties conf file
     * @return a template, default is DefaultHTMLTemplate
     * @throws NotSupportOperatorException
     */
    public static AlertTemplate getMessageTemplate() throws NotSupportOperatorException {

        if(StringUtils.isEmpty(alertTemplate)){
            return new DefaultHTMLTemplate();
        }

        switch (alertTemplate){
            case "html":
                return new DefaultHTMLTemplate();
            default:
                logger.error("not support alert template: {}",alertTemplate);
                throw new NotSupportOperatorException(String.format("not support alert template: %s",alertTemplate));
        }
    }
}
