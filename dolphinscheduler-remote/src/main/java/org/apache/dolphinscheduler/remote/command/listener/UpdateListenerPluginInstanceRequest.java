package org.apache.dolphinscheduler.remote.command.listener;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.RequestMessageBuilder;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wxn
 * @date 2023/7/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateListenerPluginInstanceRequest implements RequestMessageBuilder {

    private int instanceId;
    private String instanceName;
    private String pluginInstanceParams;
    private List<Integer> eventTypes;

    @Override
    public MessageType getCommandType() {
        return MessageType.UPDATE_LISTENER_PLUGIN_INSTANCE;
    }
}
