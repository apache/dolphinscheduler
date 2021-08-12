package org.apache.dolphinscheduler.common.cmd;

public interface OsSystemNativeCommand {
    public static String REPLACE_HOLDER = "REPLACE_HOLDER";
    String deleteCmd();
    String stopProcess();
}
