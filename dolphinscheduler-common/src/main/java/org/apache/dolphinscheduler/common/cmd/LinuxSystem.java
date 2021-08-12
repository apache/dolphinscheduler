package org.apache.dolphinscheduler.common.cmd;

public class LinuxSystem implements OsSystemNativeCommand{

    @Override
    public String deleteCmd() {
        return "rm -rf "+REPLACE_HOLDER;
    }

    @Override
    public String stopProcess() {
        return "kill -9 "+REPLACE_HOLDER;
    }

}
