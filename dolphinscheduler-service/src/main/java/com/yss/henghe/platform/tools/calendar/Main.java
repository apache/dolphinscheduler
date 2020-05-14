package com.yss.henghe.platform.tools.calendar;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
  private static Date dateOf(int y, int m, int d) {
    DateTime dt = new DateTime(y, m, d, 0, 0, 0);
    return dt.toDate();
  }

  /**
   * 业务日历提供者示例，CalendarProvider接口由调用方实现，具体实现中可以通过查库、调用服务等获取日历信息
   */
  private static final CalendarProvider PROVIDER = new CalendarProvider() {
//    private final Date startDate = dateOf(2020, 1, 1);
//    private final Date endDate = dateOf(2020, 1, 10);
    private final Map<String, Set<Date>> CALENDARS = new HashMap<String, Set<Date>>() {{
      put("中国节假日", new HashSet<Date>() {{
        add(dateOf(2020, 1, 1));
        //add(dateOf(2020, 1, 2));
        //add(dateOf(2020, 1, 3));
        add(dateOf(2020, 1, 4));
        add(dateOf(2020, 1, 5));
        //add(dateOf(2020, 1, 6));
        //add(dateOf(2020, 1, 7));
        //add(dateOf(2020, 1, 8));
        //add(dateOf(2020, 1, 9));
        //add(dateOf(2020, 1, 10));
      }});
      put("美股交易日", new HashSet<Date>() {{
        //add(dateOf(2020, 1, 1));
        add(dateOf(2020, 1, 2));
        add(dateOf(2020, 1, 3));
        //add(dateOf(2020, 1, 4));
        //add(dateOf(2020, 1, 5));
        add(dateOf(2020, 1, 6));
        add(dateOf(2020, 1, 7));
        add(dateOf(2020, 1, 8));
        add(dateOf(2020, 1, 9));
        add(dateOf(2020, 1, 10));
      }});
      put("银行间交易日", new HashSet<Date>() {{
        //add(dateOf(2020, 1, 1));
        add(dateOf(2020, 1, 2));
        add(dateOf(2020, 1, 3));
        add(dateOf(2020, 1, 4));
        add(dateOf(2020, 1, 5));
        add(dateOf(2020, 1, 6));
        add(dateOf(2020, 1, 7));
        add(dateOf(2020, 1, 8));
        add(dateOf(2020, 1, 9));
        add(dateOf(2020, 1, 10));
      }});
    }};

    @Override
    public boolean isValidDate(String name, Date date) throws Exception {
      if (!CALENDARS.containsKey(name)) {
        throw new Exception("日历[" + name + "]不存在。");
      }
//      if (date.before(startDate) || date.after(endDate)) {
//        throw new Exception("日期[" + (new SimpleDateFormat("yyyy-MM-dd").format(date)) + "]不在日历[" + name + "]的有效范围内。");
//      }
      return CALENDARS.get(name).contains(date);
    }
  };

  public static void main(String[] args) throws Exception {
    final String expression = "(美股交易日 or 银行间交易日) and (not 中国节假日)";
    final CalendarUtil.AST ast = CalendarUtil.parseCalendarExpression(expression);
    System.out.println(expression);
    for (int d = 1; d <= 20; d++) {
      Date date = dateOf(2020, 1, d);
      boolean result = CalendarUtil.executeCalendarExpression(ast, PROVIDER, date);
      System.out.println((new SimpleDateFormat("yyyy-MM-dd").format(date)) + " is " + result);
    }
    try {
      CalendarUtil.executeCalendarExpression(expression, PROVIDER, new Date());
    } catch (Throwable e) {
      String msg=e.getMessage();
      while(e!=null){
        msg=e.getMessage();
        e=e.getCause();
      }
      System.err.println(msg);
    }
  }
}
