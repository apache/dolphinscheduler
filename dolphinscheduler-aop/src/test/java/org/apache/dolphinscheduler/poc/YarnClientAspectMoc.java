package org.apache.dolphinscheduler.poc;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class YarnClientAspectMoc {

    private ApplicationId privateId = null;

    @AfterReturning(pointcut = "execution(ApplicationId org.apache.dolphinscheduler.poc.YarnClientMoc.submitApplication(ApplicationSubmissionContext)) && args(appContext)", returning = "submittedAppId", argNames = "appContext,submittedAppId")
    public void submitApplication(ApplicationSubmissionContext appContext, ApplicationId submittedAppId) {
        System.out.println("YarnClientAspectMoc[submitApplication]: app context " + appContext + ", submittedAppId "
                + submittedAppId + " privateId " + privateId);
    }

    @AfterReturning(pointcut = "cflow(execution(ApplicationId org.apache.dolphinscheduler.poc.YarnClientMoc.submitApplication(ApplicationSubmissionContext))) "
            +
            "&& !within(CfowAspect) && execution(ApplicationId org.apache.dolphinscheduler.poc.YarnClientMoc.createAppId())", returning = "submittedAppId")
    public void createAppId(ApplicationId submittedAppId) {
        privateId = submittedAppId;
        System.out.println("YarnClientAspectMoc[createAppId]: created submittedAppId " + submittedAppId);
    }
}
