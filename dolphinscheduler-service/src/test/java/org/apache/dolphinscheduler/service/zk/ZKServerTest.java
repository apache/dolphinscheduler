package org.apache.dolphinscheduler.service.zk;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@Ignore
public class ZKServerTest {

    @Test
    public void start() {
        //ZKServer is a process, can't unit test
    }

    @Test
    public void isStarted() {

    }

    @Test
    public void stop() {
        ZKServer.stop();
    }
}