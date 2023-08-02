package org.apache.dolphinscheduler.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author wxn
 * @date 2023/7/10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class DsListenerMasterDownEvent extends DsListenerEvent {

    /**
     * server type :master or worker
     */
    @JsonProperty("type")
    String type;
    @JsonProperty("host")
    String host;
    @JsonProperty("event")
    String event;
    @JsonProperty("warningLevel")
    String warningLevel;
}
