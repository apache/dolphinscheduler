package org.apache.dolphinscheduler.api.dto.schedule;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleParam {
    private String startTime;
    private String endTime;
    private String crontab;
    private String timezoneId;

    public ScheduleParam (String startTime, String endTime, String crontab, String timezoneId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.crontab = crontab;
        this.timezoneId = timezoneId;
    }
}
