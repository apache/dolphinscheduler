package org.apache.dolphinscheduler.server.master;

import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ResourceLock("MASTER_LOCK")
public @interface MasterIntegrationTestResourceLock {
}
