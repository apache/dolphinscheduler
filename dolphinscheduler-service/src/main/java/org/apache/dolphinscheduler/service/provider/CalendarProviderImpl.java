package org.apache.dolphinscheduler.service.provider;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.SchedulerCalendar;
import org.apache.dolphinscheduler.dao.entity.SchedulerCalendarDetails;
import org.apache.dolphinscheduler.dao.mapper.SchedulerCalendarDetailsMapper;
import org.apache.dolphinscheduler.dao.mapper.SchedulerCalendarMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yss.henghe.platform.tools.calendar.CalendarProvider;
import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

@Component
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class CalendarProviderImpl implements CalendarProvider {

    @Autowired
    private SchedulerCalendarMapper schedulerCalendarMapper;

    @Autowired
    private SchedulerCalendarDetailsMapper schedulerCalendarDetailsMapper;

    final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    Map<String, Set<Date>> calendars = new HashMap<String, Set<Date>>();

    @Override
    public boolean isValidDate(String name, Date date) throws Exception {
        try {
            rwLock.readLock().lock();
            if (!calendars.containsKey(name)) {
                throw new Exception("日历[" + name + "]不存在。");
            }
            return calendars.get(name).contains(date);

        }finally {
            rwLock.readLock().unlock();
        }

    }

    public  void reload(){

        Map<String, Set<Date>> schedulerCalendarMap = new HashMap<String, Set<Date>>();

        List<SchedulerCalendar> schedulerCalendarList = schedulerCalendarMapper.selectList(null);


        try {
            rwLock.writeLock().lock();
            calendars.clear();
            calendars.putAll(schedulerCalendarMap);
        }finally {
            rwLock.writeLock().unlock();
        }


    }

}
