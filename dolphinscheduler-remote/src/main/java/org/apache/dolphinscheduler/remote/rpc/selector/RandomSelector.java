package org.apache.dolphinscheduler.remote.rpc.selector;

import org.apache.dolphinscheduler.remote.utils.Host;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RandomSelector
 */
public class RandomSelector extends AbstractSelector<Host> {

    @Override
    public Host doSelect(final Collection<Host> source) {

        List<Host> hosts = new ArrayList<>(source);
        int size = hosts.size();
        int[] weights = new int[size];
        int totalWeight = 0;
        int index = 0;

        for (Host host : hosts) {
            totalWeight += host.getWeight();
            weights[index] = host.getWeight();
            index++;
        }

        if (totalWeight > 0) {
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);

            for (int i = 0; i < size; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return hosts.get(i);
                }
            }
        }
        return hosts.get(ThreadLocalRandom.current().nextInt(size));
    }

}

