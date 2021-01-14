package org.apache.dolphinscheduler.remote.rpc;

/**
 * @author jiangli
 * @date 2021-01-11 21:05
 */
public class UserService implements IUserService{
    @Override
    public String say(String s) {
        return "krris"+s;
    }
}
