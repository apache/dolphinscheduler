package org.apache.dolphinscheduler.listener.event;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wxn
 * @date 2023/7/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DsListenerEvent {

    protected Map<String, String> listenerInstanceParams;
}
