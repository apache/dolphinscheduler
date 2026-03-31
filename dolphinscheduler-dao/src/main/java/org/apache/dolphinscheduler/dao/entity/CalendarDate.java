package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_ds_calendar_date")
public class CalendarDate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long calendarId;
    
    private Date dateValue;
    
    // 0 = no, 1 = yes
    private Integer isWorkingDay;
    
    // 0 = no, 1 = yes
    private Integer isTradingDay;
    
    // Extensible day types
    private Integer dayType;
}
