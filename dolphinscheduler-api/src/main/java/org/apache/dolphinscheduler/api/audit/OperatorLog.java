package org.apache.dolphinscheduler.api.audit;

import org.apache.dolphinscheduler.common.enums.Audit.AuditObjectType;
import org.apache.dolphinscheduler.common.enums.Audit.AuditOperationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperatorLog {

    String describe() default "";
    AuditObjectType objectType() default AuditObjectType.PROJECT;
    AuditOperationType operationType() default AuditOperationType.CREATE;

}