package org.apache.dolphinscheduler.api.audit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.dolphinscheduler.api.service.AuditService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.Audit.AuditOperationType;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperatorLogAspect {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ProjectMapper projectMapper;

    @Pointcut("@annotation(OperatorLog)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        OperatorLog sysLog = method.getAnnotation(OperatorLog.class);
        if (sysLog == null) {
            return null;
        }

        Object[] args = point.getArgs();
        String[] strings = signature.getParameterNames();

        User user = null;

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < strings.length; i++) {
            map.put(strings[i], args[i]);

            if (args[i] instanceof User) {
                user = (User)args[i];
            }
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(user.getId());
        auditLog.setObjectType(sysLog.objectType().getCode());
        auditLog.setOperationType(sysLog.operationType().getCode());
        auditLog.setTime(new Date());

        if(sysLog.operationType().equals(AuditOperationType.DELETE)){
            // need to get the name before real deleted
            switch (sysLog.objectType()){
                case PROJECT:
                    Project project = projectMapper.queryByCode((long)map.get("code"));
                    auditLog.setDetail(project.getName());
                    auditLog.setObjectId(project.getId());
                default:
                    break;
            }
        }

        long beginTime = System.currentTimeMillis();
        Object result = point.proceed();
        long time = System.currentTimeMillis() - beginTime;

        saveLog(result, sysLog, auditLog);

        return result;
    }

    private void saveLog(Object object, OperatorLog sysLog, AuditLog auditLog) {
        Result result = (Result)object;
        if(result.isFailed()) {
            return;
        }

        auditService.addAudit(auditLog);
    }

    public static int getId(Object obj) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (int) idField.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            e.printStackTrace();
        }
        return -1;
    }
}