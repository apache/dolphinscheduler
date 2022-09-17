package org.apache.dolphinscheduler.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollViewLogResponse {

    /**
     * Current log message
     */
    private String log;

    /**
     * Current log line number
     */
    private long currentLogLineNumber;

    /**
     * False means there are no extra log.
     */
    private boolean hasNext;

}
