package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.base.Rpc;
import org.apache.dolphinscheduler.remote.rpc.base.RpcService;

/**
 * @author jiangli
 * @date 2021-01-11 21:05
 */
@RpcService("IUserService")
public class UserService  implements IUserService{

    @Rpc(async = true, serviceCallback = UserCallback.class, retries = 9999)
    @Override
    public Boolean say(String s) {
        return true;
    }

    @Override
    public String hi(int num) {
        return "this world has " + num + "sun";
    }
}
