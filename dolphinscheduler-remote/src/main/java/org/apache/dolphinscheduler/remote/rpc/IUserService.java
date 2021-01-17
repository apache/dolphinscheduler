package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.base.Rpc;

/**
 * @author jiangli
 * @date 2021-01-11 21:05
 */
public interface IUserService {

    @Rpc(async = true,callback = UserCallback.class)
    Boolean say(String sb);

    String hi(int num);
}
