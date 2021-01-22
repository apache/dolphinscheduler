package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.base.Rpc;

/**
 * @author jiangli
 * @date 2021-01-11 21:05
 */
public class UserService  {

    @Rpc(async = true, serviceCallback = UserCallback.class, retries = 9999)
    public Boolean say(String s) {
        return true;
    }

    public String hi(int num) {
        return "this world has " + num + "sun";
    }
}
