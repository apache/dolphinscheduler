package org.apache.dolphinscheduler.listener.service;

import org.apache.dolphinscheduler.listener.enums.ListenerEventPostServiceStatus;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostStatus;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.dolphinscheduler.listener.processor.ListenerEventProcessorManager;

/**
 * @author wxn
 * @date 2023/7/9
 */
@Slf4j
public class ListenerInstancePostService extends Thread {

    private volatile ListenerEventPostServiceStatus status;
    @Getter
    private ListenerPluginInstance listenerPluginInstance;
    private final ListenerEventMapper listenerEventMapper;
    private ListenerPlugin listenerPlugin;

    public ListenerInstancePostService(ListenerPlugin listenerPlugin, ListenerPluginInstance listenerPluginInstance,
                                       ListenerEventMapper listenerEventMapper) {
        log.info("listener plugin instance {} service start!", listenerPluginInstance.getInstanceName());
        this.status = ListenerEventPostServiceStatus.RUN;
        this.listenerPlugin = listenerPlugin;
        this.listenerPluginInstance = listenerPluginInstance;
        this.listenerEventMapper = listenerEventMapper;
    }

    @Override
    public synchronized void start() {
        super.setName("ListenerInstancePostService-" + listenerPluginInstance.getInstanceName());
        super.start();
    }

    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped() && status != ListenerEventPostServiceStatus.STOP) {
            try {
                if (status == ListenerEventPostServiceStatus.RUN) {
                    LambdaQueryWrapper<ListenerEvent> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ListenerEvent::getPluginInstanceId, listenerPluginInstance.getId())
                            .last("limit 100");
                    List<ListenerEvent> eventList = listenerEventMapper.selectList(wrapper);
                    if (CollectionUtils.isEmpty(eventList)) {
                        log.info("There is not waiting listener events");
                        continue;
                    }
                    this.post(eventList);
                }
            } catch (Exception e) {
                log.error("Alert sender thread meet an exception", e);
            } finally {
                try {
                    Thread.sleep(10000);
                } catch (final InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Current thread sleep error", interruptedException);
                }
            }
        }
    }

    public void setServiceStatus(ListenerEventPostServiceStatus status) {
        log.info("set status {}", status.getDescp());
        this.status = status;
    }

    private void post(List<ListenerEvent> eventList) {
        for (ListenerEvent event : eventList) {
            try {
                event.setParams(JSONUtils.toMap(listenerPluginInstance.getPluginInstanceParams()));
                ListenerEventProcessorManager.getInstance().getListenerEventProcessor(event.getEventType())
                        .process(listenerPlugin, event);

            } catch (Exception e) {
                log.error("post listener event failed, event id:{}", event.getId(), e);
                event.setPostStatus(ListenerEventPostStatus.EXECUTION_FAILURE);
                event.setLog(e.toString());
                event.setUpdateTime(new Date());
                listenerEventMapper.updateById(event);
                continue;
            }
            log.info("listener event {} post successfully, delete", event.getId());
            listenerEventMapper.deleteById(event);
        }
    }

    public void updateListenerPluginInstance(ListenerPluginInstance listenerPluginInstance) {
        this.listenerPluginInstance = listenerPluginInstance;
    }

    public void updateListenerPlugin(ListenerPlugin listenerPlugin) {
        this.listenerPlugin = listenerPlugin;
    }

}
