package org.apache.dolphinscheduler.remote.command.listener;

import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.RequestMessageBuilder;

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
public class RemoveListenerPluginInstanceRequest implements RequestMessageBuilder {

    private int instanceId;

    @Override
    public MessageType getCommandType() {
        return MessageType.REMOVE_LISTENER_PLUGIN_INSTANCE;
    }
}
