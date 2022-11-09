package org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TimeCalculateParser}.
 */
public interface TimeCalculateParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#timeCalc}.
	 * @param ctx the parse tree
	 */
	void enterTimeCalc(TimeCalculateParser.TimeCalcContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#timeCalc}.
	 * @param ctx the parse tree
	 */
	void exitTimeCalc(TimeCalculateParser.TimeCalcContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void enterDateTime(TimeCalculateParser.DateTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void exitDateTime(TimeCalculateParser.DateTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#abbrDate}.
	 * @param ctx the parse tree
	 */
	void enterAbbrDate(TimeCalculateParser.AbbrDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#abbrDate}.
	 * @param ctx the parse tree
	 */
	void exitAbbrDate(TimeCalculateParser.AbbrDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#fullDate}.
	 * @param ctx the parse tree
	 */
	void enterFullDate(TimeCalculateParser.FullDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#fullDate}.
	 * @param ctx the parse tree
	 */
	void exitFullDate(TimeCalculateParser.FullDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#timeInit}.
	 * @param ctx the parse tree
	 */
	void enterTimeInit(TimeCalculateParser.TimeInitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#timeInit}.
	 * @param ctx the parse tree
	 */
	void exitTimeInit(TimeCalculateParser.TimeInitContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#time}.
	 * @param ctx the parse tree
	 */
	void enterTime(TimeCalculateParser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#time}.
	 * @param ctx the parse tree
	 */
	void exitTime(TimeCalculateParser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#yearValue}.
	 * @param ctx the parse tree
	 */
	void enterYearValue(TimeCalculateParser.YearValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#yearValue}.
	 * @param ctx the parse tree
	 */
	void exitYearValue(TimeCalculateParser.YearValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#fourDigit}.
	 * @param ctx the parse tree
	 */
	void enterFourDigit(TimeCalculateParser.FourDigitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#fourDigit}.
	 * @param ctx the parse tree
	 */
	void exitFourDigit(TimeCalculateParser.FourDigitContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#monthValue}.
	 * @param ctx the parse tree
	 */
	void enterMonthValue(TimeCalculateParser.MonthValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#monthValue}.
	 * @param ctx the parse tree
	 */
	void exitMonthValue(TimeCalculateParser.MonthValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#dayValue}.
	 * @param ctx the parse tree
	 */
	void enterDayValue(TimeCalculateParser.DayValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#dayValue}.
	 * @param ctx the parse tree
	 */
	void exitDayValue(TimeCalculateParser.DayValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#hourValue}.
	 * @param ctx the parse tree
	 */
	void enterHourValue(TimeCalculateParser.HourValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#hourValue}.
	 * @param ctx the parse tree
	 */
	void exitHourValue(TimeCalculateParser.HourValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#minuteValue}.
	 * @param ctx the parse tree
	 */
	void enterMinuteValue(TimeCalculateParser.MinuteValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#minuteValue}.
	 * @param ctx the parse tree
	 */
	void exitMinuteValue(TimeCalculateParser.MinuteValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#secondValue}.
	 * @param ctx the parse tree
	 */
	void enterSecondValue(TimeCalculateParser.SecondValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#secondValue}.
	 * @param ctx the parse tree
	 */
	void exitSecondValue(TimeCalculateParser.SecondValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#twoDigit}.
	 * @param ctx the parse tree
	 */
	void enterTwoDigit(TimeCalculateParser.TwoDigitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#twoDigit}.
	 * @param ctx the parse tree
	 */
	void exitTwoDigit(TimeCalculateParser.TwoDigitContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#interval}.
	 * @param ctx the parse tree
	 */
	void enterInterval(TimeCalculateParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#interval}.
	 * @param ctx the parse tree
	 */
	void exitInterval(TimeCalculateParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#firstIntervalField}.
	 * @param ctx the parse tree
	 */
	void enterFirstIntervalField(TimeCalculateParser.FirstIntervalFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#firstIntervalField}.
	 * @param ctx the parse tree
	 */
	void exitFirstIntervalField(TimeCalculateParser.FirstIntervalFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#intervalField}.
	 * @param ctx the parse tree
	 */
	void enterIntervalField(TimeCalculateParser.IntervalFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#intervalField}.
	 * @param ctx the parse tree
	 */
	void exitIntervalField(TimeCalculateParser.IntervalFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#intervalSign}.
	 * @param ctx the parse tree
	 */
	void enterIntervalSign(TimeCalculateParser.IntervalSignContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#intervalSign}.
	 * @param ctx the parse tree
	 */
	void exitIntervalSign(TimeCalculateParser.IntervalSignContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void enterIntervalValue(TimeCalculateParser.IntervalValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#intervalValue}.
	 * @param ctx the parse tree
	 */
	void exitIntervalValue(TimeCalculateParser.IntervalValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#refVar}.
	 * @param ctx the parse tree
	 */
	void enterRefVar(TimeCalculateParser.RefVarContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#refVar}.
	 * @param ctx the parse tree
	 */
	void exitRefVar(TimeCalculateParser.RefVarContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#integerValue}.
	 * @param ctx the parse tree
	 */
	void enterIntegerValue(TimeCalculateParser.IntegerValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#integerValue}.
	 * @param ctx the parse tree
	 */
	void exitIntegerValue(TimeCalculateParser.IntegerValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDivMod}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDivMod(TimeCalculateParser.MulDivModContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDivMod}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDivMod(TimeCalculateParser.MulDivModContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Number}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNumber(TimeCalculateParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Number}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNumber(TimeCalculateParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Comparison}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterComparison(TimeCalculateParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Comparison}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitComparison(TimeCalculateParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Exponal}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExponal(TimeCalculateParser.ExponalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Exponal}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExponal(TimeCalculateParser.ExponalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PlusOrMinus}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusOrMinus(TimeCalculateParser.PlusOrMinusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PlusOrMinus}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusOrMinus(TimeCalculateParser.PlusOrMinusContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RefDTVar}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRefDTVar(TimeCalculateParser.RefDTVarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RefDTVar}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRefDTVar(TimeCalculateParser.RefDTVarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parentheses}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParentheses(TimeCalculateParser.ParenthesesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parentheses}
	 * labeled alternative in {@link TimeCalculateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParentheses(TimeCalculateParser.ParenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(TimeCalculateParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(TimeCalculateParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link TimeCalculateParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit(TimeCalculateParser.IntervalUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TimeCalculateParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit(TimeCalculateParser.IntervalUnitContext ctx);
}