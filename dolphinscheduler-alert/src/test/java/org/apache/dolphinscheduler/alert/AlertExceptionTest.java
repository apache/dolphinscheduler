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
package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.dao.entity.Alert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import static org.junit.Assert.*;

public class AlertExceptionTest {
    private static final Logger logger = LoggerFactory.getLogger(AlertExceptionTest.class);

    @Test
    public void testGetAlert() {
        Alert alert = new Alert(1, "");
        try {
            throw new AlertException("test", alert);
        } catch (AlertException e) {
            assertEquals(Optional.of(alert), e.getAlert());
        }
    }

    @Test
    public void testToString() {
        try {
            throw new AlertException("test", new Alert(1, "test"));
        } catch (AlertException e) {
            logger.info(e.toString());
        }
    }
}