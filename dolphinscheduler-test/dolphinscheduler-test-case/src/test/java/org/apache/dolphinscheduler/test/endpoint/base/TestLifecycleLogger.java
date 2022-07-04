package org.apache.dolphinscheduler.test.endpoint.base;

import org.junit.jupiter.api.*;

import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public
interface TestLifecycleLogger {

    static Logger logger = Logger.getLogger(TestLifecycleLogger.class.getName());


    @BeforeAll
    default void beforeAllTests() {
        logger.info("init user session");
    }

    @AfterAll
    default void afterAllTests() {
        logger.info("release user session");
    }

    @BeforeEach
    default void beforeEachTest(TestInfo testInfo) {
        logger.info(() -> String.format("About to execute [%s]",
                testInfo.getDisplayName()));
    }

    @AfterEach
    default void afterEachTest(TestInfo testInfo) {
        logger.info(() -> String.format("Finished executing [%s]",
                testInfo.getDisplayName()));
    }

}
