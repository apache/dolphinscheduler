package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.base.Rpc;

/**
 * @author jiangli
 * @date 2021-01-11 21:05
 */
public class UserService implements IUserService {
    @Override
    @Rpc(async = true, callback = UserCallback.class, retries = 9999, isOneway = false)
    public Boolean say(String s) {
        return true;
    }

    @Override
    public String hi(int num) {
        return "this world has " + num + "sun";
    }
}
