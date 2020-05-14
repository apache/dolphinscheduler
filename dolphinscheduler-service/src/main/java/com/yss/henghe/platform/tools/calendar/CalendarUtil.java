package com.yss.henghe.platform.tools.calendar;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class CalendarUtil {
  private CalendarUtil() {
  }

  public static final class AST {
    private final ParseTree tree;

    private AST(final ParseTree tree) {
      this.tree = tree;
    }
  }

  private static class CalendarErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
      throw new RuntimeException("line " + line + ":" + charPositionInLine + " " + msg);
    }
  }

  private static class CalendarExpressionExecutor extends CalendarExpressionBaseVisitor<Boolean> {
    private final CalendarProvider provider;
    private final Date date;

    CalendarExpressionExecutor(CalendarProvider provider, Date date) {
      this.provider = provider;
      this.date = date;
    }

    @Override
    public Boolean visitExpression(CalendarExpressionParser.ExpressionContext ctx) {
      return ctx.booleanExpression().accept(this);
    }

    @Override
    public Boolean visitLogicalNot(CalendarExpressionParser.LogicalNotContext ctx) {
      return !ctx.booleanExpression().accept(this);
    }

    @Override
    public Boolean visitLogicalAnd(CalendarExpressionParser.LogicalAndContext ctx) {
      //防止短路求值
      boolean left=ctx.left.accept(this);
      boolean right=ctx.right.accept(this);
      return left && right;
    }

    @Override
    public Boolean visitLogicalOr(CalendarExpressionParser.LogicalOrContext ctx) {
      //防止短路求值
      boolean left=ctx.left.accept(this);
      boolean right=ctx.right.accept(this);
      return left || right;
    }

    @Override
    public Boolean visitAtomCalendar(CalendarExpressionParser.AtomCalendarContext ctx) {
      final String name = ctx.NAME().getText();
      try {
        return this.provider.isValidDate(name, this.date);
      } catch (Exception e) {
        throw new RuntimeException("CalendarProvider can't handle the calendar [" + name + "] and date [" + (new SimpleDateFormat("yyyy-MM-dd").format(this.date)) + "].", e);
      }
    }

    @Override
    public Boolean visitParenthesizedExpression(CalendarExpressionParser.ParenthesizedExpressionContext ctx) {
      return ctx.booleanExpression().accept(this);
    }
  }

  public static AST parseCalendarExpression(final String expression) throws Exception {
    try {
      CharStream input = CharStreams.fromString(expression);
      CalendarExpressionLexer lexer = new CalendarExpressionLexer(input);
      lexer.removeErrorListeners();
      lexer.addErrorListener(new CalendarErrorListener());
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      CalendarExpressionParser parser = new CalendarExpressionParser(tokens);
      parser.removeErrorListeners();
      parser.addErrorListener(new CalendarErrorListener());
      return new AST(parser.expression());
    } catch (Throwable e) {
      throw new Exception("The calendar expression [" + expression + "] is invalid.", e);
    }
  }

  public static boolean executeCalendarExpression(final AST ast, final CalendarProvider provider, final Date date) throws Exception {
    try {
      return new CalendarExpressionExecutor(provider, date).visit(ast.tree);
    } catch (Throwable e) {
      throw new Exception("The calendar expression can not be executed.", e);
    }
  }

  public static boolean executeCalendarExpression(final String expression, final CalendarProvider provider, final Date date) throws Exception {
    AST ast = parseCalendarExpression(expression);
    return executeCalendarExpression(ast, provider, date);
  }
}
