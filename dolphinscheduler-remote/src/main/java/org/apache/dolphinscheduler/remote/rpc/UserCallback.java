package org.apache.dolphinscheduler.remote.rpc;

import org.apache.dolphinscheduler.remote.rpc.common.AbstractRpcCallBack;

/**
 * @author jiangli
 * @date 2021-01-15 07:32
 */
public class UserCallback extends AbstractRpcCallBack {
    @Override
    public void run(Object object) {
        String msg= (String) object;
        System.out.println("我是异步回调"+msg);
    }
}
