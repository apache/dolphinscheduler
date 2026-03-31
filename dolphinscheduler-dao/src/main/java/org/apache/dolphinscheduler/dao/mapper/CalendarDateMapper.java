package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.CalendarDate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;

public interface CalendarDateMapper extends BaseMapper<CalendarDate> {
    
    /**
     * Find the Nth valid business date relative to a base date.
     * @param calendarId     The custom calendar ID
     * @param baseDate       The starting date evaluated
     * @param offsetDays     T+N or T-N 
     * @param checkTrading   Whether to skip non-trading days
     * @return The calculated business date
     */
    Date calculateBusinessDate(@Param("calendarId") Long calendarId,
                               @Param("baseDate") Date baseDate,
                               @Param("offsetDays") int offsetDays,
                               @Param("checkTrading") boolean checkTrading);
}
