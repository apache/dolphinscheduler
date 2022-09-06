package org.apache.dolphinscheduler.plugin.task.api;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@Slf4j
public class ShellCommandExecutorTest {

    private static final String APP_ID_FILE = ShellCommandExecutorTest.class.getResource("/appId.txt")
            .getFile();

    @Test
    public void getAppIds() {
        ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor(strings -> {
        }, new TaskExecutionContext(), log);
        List<String> appIds = shellCommandExecutor.getAppIds(APP_ID_FILE);
        Assert.assertEquals(Lists.newArrayList("application_1548381669007_1234"), appIds);
    }

}
