package org.apache.dolphinscheduler.service.calendar;

import org.apache.dolphinscheduler.dao.mapper.CalendarDateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class BusinessCalendarServiceImpl implements BusinessCalendarService {

    @Autowired
    private CalendarDateMapper calendarDateMapper;

    @Override
    public Date resolveBusinessDate(Long calendarId, Date physicalDate, Integer offsetDays, String cutoverTime) {
        if (calendarId == null) {
            // Unchanged behavior for standard DS schedules
            return calculateSimpleOffset(physicalDate, offsetDays);
        }

        // 1. Shift base date if physical time breaches cutover
        ZonedDateTime baseTime = physicalDate.toInstant().atZone(ZoneId.systemDefault());
        if (cutoverTime != null && !cutoverTime.trim().isEmpty()) {
            LocalTime cutover = LocalTime.parse(cutoverTime);
            if (!baseTime.toLocalTime().isBefore(cutover)) {
                baseTime = baseTime.plusDays(1); // logical execution is "next cycle"
            }
        }
        
        // 2. Lookup DB to skip non-trading days and apply offset
        Date computedDate = calendarDateMapper.calculateBusinessDate(
                calendarId, 
                Date.from(baseTime.toInstant()), 
                offsetDays == null ? 0 : offsetDays, 
                true // strictly check trading day per user requirements
        );
        
        // Guard against missing calendar dates returning null
        return computedDate != null ? computedDate : physicalDate;
    }

    private Date calculateSimpleOffset(Date physicalDate, Integer offsetDays) {
        if (offsetDays == null || offsetDays == 0) return physicalDate;
        ZonedDateTime baseTime = physicalDate.toInstant().atZone(ZoneId.systemDefault());
        return Date.from(baseTime.plusDays(offsetDays).toInstant());
    }
}
