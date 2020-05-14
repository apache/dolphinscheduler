package com.yss.henghe.platform.tools.calendar;

import java.util.Date;

/**
 * 业务日历提供者，由调用方实现。
 */
public interface CalendarProvider {

  /**
   * 返回日期在日历中的状态，如在"工作日"日历中"2020-01-01"的状态为false，在"节假日"日历中"2020-01-01"的状态为true
   * @param name 日历名称（key）
   * @param date 具体日期
   * @return 日期在日历中的状态，true为有效，false为无效
   * @throws Exception 日历不存在、日历不可用或过期、日期超出日历范围等情况，应抛出异常
   */
  boolean isValidDate(final String name, final Date date) throws Exception;
}
