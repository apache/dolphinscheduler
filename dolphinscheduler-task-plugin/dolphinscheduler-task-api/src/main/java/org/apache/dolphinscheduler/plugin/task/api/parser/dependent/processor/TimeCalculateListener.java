package org.apache.dolphinscheduler.plugin.task.api.parser.dependent.processor;

import org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate.TimeCalculateParser;
import org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate.TimeCalculateParserBaseListener;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

import org.joda.time.DateTime;

public class TimeCalculateListener extends TimeCalculateParserBaseListener {

    private final DateTime businessTime;
    private final HashMap<String, Integer> refVar;
    private final Stack<Double> stack = new Stack<>();
    private DateTime reference;

    private String sign;

    public TimeCalculateListener(DateTime businessTime) {
        this.businessTime = businessTime;
        this.refVar = setRefVar(this.businessTime);
        this.reference = businessTime;

    }

    public TimeCalculateListener(DateTime businessTime, String timeInit) {
        this.businessTime = businessTime;
        this.refVar = setRefVar(this.businessTime);
        switch (timeInit.toUpperCase()) {
            case "S":
                this.reference = businessTime.withTimeAtStartOfDay();
                break;
            case "E":
                this.reference = businessTime.withTimeAtStartOfDay().plusDays(1).minusMillis(1);
                break;
            default:
                this.reference = businessTime;
        }
    }

    private HashMap<String, Integer> setRefVar(DateTime dateTime) {
        return new HashMap<String, Integer>() {{
            put("Y", dateTime.getYear());
            put("M", dateTime.getMonthOfYear());
            put("D", dateTime.getDayOfMonth());
            put("H", dateTime.getHourOfDay());
            put("m", dateTime.getMinuteOfHour());
            put("S", dateTime.getSecondOfMinute());
            put("WOY", dateTime.getWeekOfWeekyear());
            put("DOW", dateTime.getDayOfWeek());
            put("DOY", dateTime.getDayOfYear());

        }};

    }

    public DateTime getResult() {
        return reference;
    }

    public static int boolToInt(boolean foo) {
        return Boolean.compare(foo, false);
    }

    @Override
    public void exitFourDigit(TimeCalculateParser.FourDigitContext ctx) {
        Optional.ofNullable(ctx.getText()).ifPresent(
                x -> this.reference = this.reference.withYear(Integer.parseInt(x))
        );
    }

    @Override
    public void exitMonthValue(TimeCalculateParser.MonthValueContext ctx) {
        Optional.ofNullable(ctx.twoDigit()).ifPresent(
                x -> this.reference = this.reference.withMonthOfYear(Integer.parseInt(x.getText()))
        );
    }

    @Override
    public void exitDayValue(TimeCalculateParser.DayValueContext ctx) {
        Optional.ofNullable(ctx.twoDigit()).ifPresent(
                x -> this.reference = this.reference.withDayOfMonth(Integer.parseInt(x.getText()))
        );
    }

    @Override
    public void exitTimeInit(TimeCalculateParser.TimeInitContext ctx) {
        Optional.ofNullable(ctx.getText()).ifPresent(x ->
                {
                    switch (x.toUpperCase()) {
                        case "S":
                            this.reference = this.reference.withTimeAtStartOfDay();
                            break;
                        case "E":
                            this.reference = this.reference.withTimeAtStartOfDay().plusDays(1).minusMillis(1);
                            break;
                        case "C":
                            this.reference = this.reference.withTime(businessTime.toLocalTime());
                            break;
                    }
                }
        );
    }

    @Override
    public void exitHourValue(TimeCalculateParser.HourValueContext ctx) {
        Optional.ofNullable(ctx.twoDigit()).ifPresent(
                x -> this.reference = this.reference.withHourOfDay(Integer.parseInt(x.getText()))
        );
    }

    @Override
    public void exitMinuteValue(TimeCalculateParser.MinuteValueContext ctx) {
        Optional.ofNullable(ctx.twoDigit()).ifPresent(
                x -> this.reference = this.reference.withMinuteOfHour(Integer.parseInt(x.getText()))
        );
    }

    @Override
    public void exitSecondValue(TimeCalculateParser.SecondValueContext ctx) {
        Optional.ofNullable(ctx.twoDigit()).ifPresent(
                x -> this.reference = this.reference.withSecondOfMinute(Integer.parseInt(x.getText()))
        );
    }

    @Override
    public void exitIntegerValue(TimeCalculateParser.IntegerValueContext ctx) {
        int number = Integer.parseInt(ctx.getText());
        this.stack.push((double) number);
    }

    private String upperCaseExceptMonthMinute(String s) {
        return s.equalsIgnoreCase("M") ? s : s.toUpperCase();
    }

    @Override
    public void exitRefVar(TimeCalculateParser.RefVarContext ctx) {
        this.stack.push(Double.valueOf(this.refVar.get(upperCaseExceptMonthMinute(ctx.getText()))));
    }

    @Override
    public void exitExponal(TimeCalculateParser.ExponalContext ctx) {
        Double right = this.stack.pop();
        Double left = this.stack.pop();
        this.stack.push(Math.pow(left, right));
    }

    @Override
    public void exitMulDivMod(TimeCalculateParser.MulDivModContext ctx) {
        Double right = this.stack.pop();
        Double left = this.stack.pop();
        switch (ctx.op.getText()) {
            case "*":
                this.stack.push(left * right);
                break;
            case "/":
                this.stack.push(left / right);
                break;
            //
            case "//":
                this.stack.push((double) Math.floorDiv((int) Math.round(left), (int) Math.round(right)));
                break;
            case "%":
                this.stack.push(left % right);
                break;
        }
    }

    @Override
    public void exitPlusOrMinus(TimeCalculateParser.PlusOrMinusContext ctx) {
        Double right = this.stack.pop();
        Double left = this.stack.pop();
        switch (ctx.op.getText()) {
            case "+":
                this.stack.push(left + right);
                break;
            case "-":
                this.stack.push(left - right);
                break;
        }
    }

    @Override
    public void exitComparison(TimeCalculateParser.ComparisonContext ctx) {
        Double right = this.stack.pop();
        Double left = this.stack.pop();
        switch (ctx.op.getText()) {
            case "==":
            case "=":
                this.stack.push((double) boolToInt(Objects.equals(left, right)));
                break;
            case "<>":
            case "!=":
                this.stack.push((double) boolToInt(!Objects.equals(left, right)));
                break;
            case "<":
                this.stack.push((double) boolToInt(left < right));
                break;
            case "<=":
            case "!>":
                this.stack.push((double) boolToInt(left <= right));
                break;
            case ">=":
            case "!<":
                this.stack.push((double) boolToInt(left >= right));
                break;
            case ">":
                this.stack.push((double) boolToInt(left > right));
                break;
        }
    }


    private void setRefFromField(int value, String unit) {
        if (unit == null) {
            this.reference = this.reference.plusDays(value);
        } else {
            switch (upperCaseExceptMonthMinute(unit)) {
                case "Y":
                    this.reference = this.reference.plusYears(value);
                    break;
                case "M":
                    this.reference = this.reference.plusMonths(value);
                    break;
                case "W":
                    this.reference = this.reference.plusWeeks(value);
                    break;
                case "D":
                    this.reference = this.reference.plusDays(value);
                    break;
                case "H":
                    this.reference = this.reference.plusHours(value);
                    break;
                case "m":
                    this.reference = this.reference.plusMinutes(value);
                    break;
                case "S":
                    this.reference = this.reference.plusSeconds(value);
                    break;
            }
        }
    }

    @Override
    public void exitIntervalSign(TimeCalculateParser.IntervalSignContext ctx) {
        Optional.ofNullable(ctx.getText()).ifPresent(x -> sign = x);
    }

    @Override
    public void exitFirstIntervalField(TimeCalculateParser.FirstIntervalFieldContext ctx) {
        int value = (int) Math.round(this.stack.pop());
        if (Objects.equals(sign, "-")) {
            setRefFromField(-value, ctx.unit.getText());
        } else {
            setRefFromField(value, ctx.unit.getText());
        }
    }

    @Override
    public void exitIntervalField(TimeCalculateParser.IntervalFieldContext ctx) {
        int value = (int) Math.round(this.stack.pop());
        if (Objects.equals(sign, "-")) {
            setRefFromField(-value, ctx.unit.getText());
        } else {
            setRefFromField(value, ctx.unit.getText());
        }
    }

    @Override
    public void exitInterval(TimeCalculateParser.IntervalContext ctx) {
        Optional.ofNullable(ctx.integerValue()).ifPresent(x -> {
            int value = this.stack.pop().intValue();
            if (Objects.equals(sign, "-")) {
                setRefFromField(-value, null);
            } else {
                setRefFromField(value, null);
            }
        });
    }
}
