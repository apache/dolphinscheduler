package org.apache.dolphinscheduler;

import org.apache.dolphinscheduler.poc.YarnClientMoc;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.exceptions.YarnException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YarnClientAspectMocTest {

    private final PrintStream standardOut = System.out;
    ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
    @BeforeEach
    public void beforeEveryTest() {
        System.setOut(new PrintStream(stdoutStream));
    }
    @AfterEach
    public void afterEveryTest() throws IOException {
        System.setOut(standardOut);
        stdoutStream.close();
    }
    @Test
    public void testMoc() {
        YarnClientMoc moc = new YarnClientMoc();
        try {
            ApplicationSubmissionContext appContext = ApplicationSubmissionContext.newInstance(
                    ApplicationId.newInstance(System.currentTimeMillis(), 1236), "appName",
                    "queue", Priority.UNDEFINED,
                    null, false,
                    false, 10, null,
                    "type");
            moc.createAppId();
            ApplicationId applicationId = moc.submitApplication(appContext);
            String stdoutContent = stdoutStream.toString();
            Assertions.assertTrue(stdoutContent.contains("YarnClientAspectMoc[submitApplication]"),
                    "trigger YarnClientAspectMoc.submitApplication failed");
            Assertions.assertTrue(stdoutContent.contains("YarnClientAspectMoc[createAppId]:"),
                    "trigger YarnClientAspectMoc.createAppId failed");
        } catch (YarnException | IOException e) {
            Assertions.fail("test YarnClientAspectMoc failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
