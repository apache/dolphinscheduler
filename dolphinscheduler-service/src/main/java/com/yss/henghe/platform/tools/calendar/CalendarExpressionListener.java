// Generated from X:/HengHe-2.0.0/henghe-platform-tools/CalendarExpressionParser/src/main/java/com/yss/henghe/platform/tools/calendar\CalendarExpression.g4 by ANTLR 4.8
package com.yss.henghe.platform.tools.calendar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CalendarExpressionParser}.
 */
public interface CalendarExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CalendarExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(CalendarExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CalendarExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(CalendarExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalNot(CalendarExpressionParser.LogicalNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalNot(CalendarExpressionParser.LogicalNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedExpression(CalendarExpressionParser.ParenthesizedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedExpression(CalendarExpressionParser.ParenthesizedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAnd}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAnd(CalendarExpressionParser.LogicalAndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAnd}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAnd(CalendarExpressionParser.LogicalAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomCalendar}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterAtomCalendar(CalendarExpressionParser.AtomCalendarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomCalendar}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitAtomCalendar(CalendarExpressionParser.AtomCalendarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOr}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOr(CalendarExpressionParser.LogicalOrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOr}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOr(CalendarExpressionParser.LogicalOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link CalendarExpressionParser#error}.
	 * @param ctx the parse tree
	 */
	void enterError(CalendarExpressionParser.ErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CalendarExpressionParser#error}.
	 * @param ctx the parse tree
	 */
	void exitError(CalendarExpressionParser.ErrorContext ctx);
}