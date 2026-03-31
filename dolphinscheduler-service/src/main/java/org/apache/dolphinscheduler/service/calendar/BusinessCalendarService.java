package org.apache.dolphinscheduler.service.calendar;

import java.util.Date;

/**
 * Service to handle business date resolution using custom calendars, cutover times, and T+/-N offsets.
 */
public interface BusinessCalendarService {

    /**
     * Resolves the actual business date by applying the calendar logic.
     * @param calendarId The Custom Calendar linked to the Schedule (null if standard cron)
     * @param physicalDate The moment in time the master generated the scheduling action
     * @param offsetDays T+N / T-N offset from the calculated execution day
     * @param cutoverTime e.g., "15:00" - if physicalTime is after this, the logical start shifts to the next day
     * @return Resolved logical business date, or original physicalDate if no calendar configured.
     */
    Date resolveBusinessDate(Long calendarId, Date physicalDate, Integer offsetDays, String cutoverTime);
}
