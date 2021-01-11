package org.apache.dolphinscheduler.remote.rpc.filter;

import org.apache.dolphinscheduler.remote.rpc.Invoker;
import org.apache.dolphinscheduler.remote.rpc.common.RpcRequest;
import org.apache.dolphinscheduler.remote.rpc.common.RpcResponse;

/**
 * @author jiangli
 * @date 2021-01-11 11:48
 */
public class FilterWrapper implements Invoker {


    private Filter next;

    private Invoker invoker;


    public FilterWrapper(Filter next, Invoker invoker) {
        this.next = next;
        this.invoker = invoker;
    }

    @Override
    public RpcResponse invoke(RpcRequest args) throws Throwable {
        if (next != null) {
            return next.filter(invoker, args);
        } else {
            return invoker.invoke(args);
        }
    }
}
