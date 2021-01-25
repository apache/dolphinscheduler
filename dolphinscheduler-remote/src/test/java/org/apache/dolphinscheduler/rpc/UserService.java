package org.apache.dolphinscheduler.rpc;

import org.apache.dolphinscheduler.rpc.base.RpcService;

/**
 * UserService
 */
@RpcService("IUserService")
public class UserService  implements IUserService{

    @Override
    public Boolean say(String s) {
        return true;
    }

    @Override
    public Integer hi(int num) {
        return ++num;
    }
}
