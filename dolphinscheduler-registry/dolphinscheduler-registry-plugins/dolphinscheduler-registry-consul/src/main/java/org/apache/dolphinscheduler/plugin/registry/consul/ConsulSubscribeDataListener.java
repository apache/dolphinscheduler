package org.apache.dolphinscheduler.plugin.registry.consul;

import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.model.kv.Value;

public class ConsulSubscribeDataListener implements ConsulCache.Listener<String, Value> {

    private final SubscribeListener listener;

    public ConsulSubscribeDataListener(SubscribeListener listener) {
        this.listener = listener;
    }

    Map<String, Value> lastValues = null;

    @Override
    public void notify(Map<String, Value> newValues) {
        if (lastValues == null) {
            lastValues = newValues;
        } else {
            List<Value> addedData = new ArrayList<>();
            List<Value> deletedData = new ArrayList<>();
            List<Value> updatedData = new ArrayList<>();
            for (Map.Entry<String, Value> entry : newValues.entrySet()) {
                Value newData = entry.getValue();
                Value oldData = lastValues.get(entry.getKey());
                if (oldData == null) {
                    addedData.add(newData);
                } else {
                    if (entry.getValue().getModifyIndex() != oldData.getModifyIndex()) {
                        updatedData.add(newData);
                    }
                }
            }
            for (Map.Entry<String, Value> entry : lastValues.entrySet()) {
                if (!newValues.containsKey(entry.getKey())) {
                    deletedData.add(entry.getValue());
                }
            }
            lastValues = newValues;
            // trigger listener
            triggerListener(addedData, listener, Event.Type.ADD);
            triggerListener(deletedData, listener, Event.Type.REMOVE);
            triggerListener(updatedData, listener, Event.Type.UPDATE);
        }
    }

    private void triggerListener(List<Value> list, SubscribeListener listener, Event.Type type) {
        for (Value val : list) {
            listener.notify(new Event(val.getKey(), val.getKey(), val.getValueAsString().orElse(""), type));
        }
    }

}
