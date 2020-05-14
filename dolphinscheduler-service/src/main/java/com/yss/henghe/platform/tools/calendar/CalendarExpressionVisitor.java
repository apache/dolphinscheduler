// Generated from X:/HengHe-2.0.0/henghe-platform-tools/CalendarExpressionParser/src/main/java/com/yss/henghe/platform/tools/calendar\CalendarExpression.g4 by ANTLR 4.8
package com.yss.henghe.platform.tools.calendar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CalendarExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CalendarExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CalendarExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CalendarExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalNot}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalNot(CalendarExpressionParser.LogicalNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesizedExpression}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedExpression(CalendarExpressionParser.ParenthesizedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalAnd}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAnd(CalendarExpressionParser.LogicalAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomCalendar}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomCalendar(CalendarExpressionParser.AtomCalendarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalOr}
	 * labeled alternative in {@link CalendarExpressionParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOr(CalendarExpressionParser.LogicalOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link CalendarExpressionParser#error}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitError(CalendarExpressionParser.ErrorContext ctx);
}