package org.apache.dolphinscheduler.poc;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.IOException;
import java.util.Random;

public class YarnClientMoc {

    private Random random = new Random();

    public ApplicationId createAppId() {
        ApplicationId created = ApplicationId.newInstance(System.currentTimeMillis(), random.nextInt());
        System.out.println("created id " + created.getId());
        return created;
    }

    public ApplicationId submitApplication(ApplicationSubmissionContext appContext) throws YarnException, IOException {
        return createAppId();
    }
}
