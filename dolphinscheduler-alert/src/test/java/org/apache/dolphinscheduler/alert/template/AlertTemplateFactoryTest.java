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

import org.apache.dolphinscheduler.alert.template.impl.DefaultHTMLTemplate;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * test class for AlertTemplateFactory
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertyUtils.class)
public class AlertTemplateFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertTemplateFactoryTest.class);

    /**
     * GetMessageTemplate method test
     */
    @Test
    public void testGetMessageTemplate(){

        PowerMockito.mockStatic(PropertyUtils.class);

        AlertTemplate defaultTemplate = AlertTemplateFactory.getMessageTemplate();

        assertTrue(defaultTemplate instanceof DefaultHTMLTemplate);
    }

    /**
     * GetMessageTemplate method throw Exception test
     */
    @Test
    public void testGetMessageTemplateException(){

        AlertTemplate defaultTemplate = AlertTemplateFactory.getMessageTemplate();
        assertTrue(defaultTemplate instanceof DefaultHTMLTemplate);
    }
}
