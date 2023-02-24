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

package org.apache.dolphinscheduler.service.expand;

import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TimePlaceholderResolverExpandServiceTest {

    @Mock
    private TimePlaceholderResolverExpandService timePlaceholderResolverExpandService;

    @InjectMocks
    private TimePlaceholderResolverExpandServiceImpl timePlaceholderResolverExpandServiceImpl;

    private static final String placeHolderName = "$[yyyy-MM-dd-1]";

    @Test
    public void testTimePlaceholderResolverExpandService() {
        boolean checkResult = timePlaceholderResolverExpandService.timeFunctionNeedExpand(placeHolderName);
        Assertions.assertFalse(checkResult);
        String resultString = timePlaceholderResolverExpandService.timeFunctionExtension(1, "", placeHolderName);
        Assertions.assertTrue(StringUtils.isEmpty(resultString));

        boolean implCheckResult = timePlaceholderResolverExpandServiceImpl.timeFunctionNeedExpand(placeHolderName);
        Assertions.assertFalse(implCheckResult);
        String implResultString =
                timePlaceholderResolverExpandServiceImpl.timeFunctionExtension(1, "", placeHolderName);
        Assertions.assertTrue(StringUtils.isEmpty(implResultString));
    }
}
