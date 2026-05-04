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

package org.apache.dolphinscheduler.server.master.integration;

import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class MasterIntegrationTestContextCustomizerFactory implements ContextCustomizerFactory {

    private static final AtomicInteger NEXT_MASTER_LISTEN_PORT =
            new AtomicInteger(ThreadLocalRandom.current().nextInt(20_000, 50_000));

    @Override
    public ContextCustomizer createContextCustomizer(final Class<?> testClass,
                                                     final List<ContextConfigurationAttributes> configAttributes) {
        if (!AbstractMasterIntegrationTestCase.class.isAssignableFrom(testClass)) {
            return null;
        }
        return new MasterIntegrationTestContextCustomizer(testClass.getName());
    }

    private static final class MasterIntegrationTestContextCustomizer implements ContextCustomizer {

        private final String testClassName;

        private MasterIntegrationTestContextCustomizer(final String testClassName) {
            this.testClassName = testClassName;
        }

        @Override
        public void customizeContext(final ConfigurableApplicationContext context,
                                     final MergedContextConfiguration mergedConfig) {
            final String databaseName = "dolphinscheduler_" + UUID.randomUUID().toString().replace("-", "");
            final int masterListenPort = NEXT_MASTER_LISTEN_PORT.getAndIncrement();

            TestPropertyValues.of(
                    "spring.datasource.url=jdbc:h2:mem:" + databaseName
                            + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;",
                    "master.listen-port=" + masterListenPort,
                    "server.port=0")
                    .applyTo(context);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MasterIntegrationTestContextCustomizer)) {
                return false;
            }
            MasterIntegrationTestContextCustomizer that = (MasterIntegrationTestContextCustomizer) o;
            return Objects.equals(testClassName, that.testClassName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(testClassName);
        }
    }
}
