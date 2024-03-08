package org.apache.dolphinscheduler.api.audit.operator;

import org.apache.dolphinscheduler.api.audit.enums.AuditType;

import org.aspectj.lang.ProceedingJoinPoint;

public interface Operator {

    Object recordAudit(ProceedingJoinPoint point, String describe, AuditType auditType) throws Throwable;
}
