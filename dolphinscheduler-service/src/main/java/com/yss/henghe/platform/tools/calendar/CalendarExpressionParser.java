// Generated from X:/HengHe-2.0.0/henghe-platform-tools/CalendarExpressionParser/src/main/java/com/yss/henghe/platform/tools/calendar\CalendarExpression.g4 by ANTLR 4.8
package com.yss.henghe.platform.tools.calendar;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CalendarExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, AND=3, NOT=4, OR=5, NAME=6, SPACES=7, UNEXPECTED_CHAR=8;
	public static final int
		RULE_expression = 0, RULE_booleanExpression = 1, RULE_error = 2;
	private static String[] makeRuleNames() {
		return new String[] {
			"expression", "booleanExpression", "error"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "AND", "NOT", "OR", "NAME", "SPACES", "UNEXPECTED_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CalendarExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CalendarExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExpressionContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CalendarExpressionParser.EOF, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public ErrorContext error() {
			return getRuleContext(ErrorContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(8);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case NOT:
			case NAME:
				{
				setState(6);
				booleanExpression(0);
				}
				break;
			case UNEXPECTED_CHAR:
				{
				setState(7);
				error();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(10);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanExpressionContext extends ParserRuleContext {
		public BooleanExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanExpression; }
	 
		public BooleanExpressionContext() { }
		public void copyFrom(BooleanExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class LogicalNotContext extends BooleanExpressionContext {
		public TerminalNode NOT() { return getToken(CalendarExpressionParser.NOT, 0); }
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public LogicalNotContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterLogicalNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitLogicalNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitLogicalNot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParenthesizedExpressionContext extends BooleanExpressionContext {
		public BooleanExpressionContext booleanExpression() {
			return getRuleContext(BooleanExpressionContext.class,0);
		}
		public ParenthesizedExpressionContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterParenthesizedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitParenthesizedExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitParenthesizedExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LogicalAndContext extends BooleanExpressionContext {
		public BooleanExpressionContext left;
		public BooleanExpressionContext right;
		public TerminalNode AND() { return getToken(CalendarExpressionParser.AND, 0); }
		public List<BooleanExpressionContext> booleanExpression() {
			return getRuleContexts(BooleanExpressionContext.class);
		}
		public BooleanExpressionContext booleanExpression(int i) {
			return getRuleContext(BooleanExpressionContext.class,i);
		}
		public LogicalAndContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterLogicalAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitLogicalAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitLogicalAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AtomCalendarContext extends BooleanExpressionContext {
		public TerminalNode NAME() { return getToken(CalendarExpressionParser.NAME, 0); }
		public AtomCalendarContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterAtomCalendar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitAtomCalendar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitAtomCalendar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LogicalOrContext extends BooleanExpressionContext {
		public BooleanExpressionContext left;
		public BooleanExpressionContext right;
		public TerminalNode OR() { return getToken(CalendarExpressionParser.OR, 0); }
		public List<BooleanExpressionContext> booleanExpression() {
			return getRuleContexts(BooleanExpressionContext.class);
		}
		public BooleanExpressionContext booleanExpression(int i) {
			return getRuleContext(BooleanExpressionContext.class,i);
		}
		public LogicalOrContext(BooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterLogicalOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitLogicalOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitLogicalOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanExpressionContext booleanExpression() throws RecognitionException {
		return booleanExpression(0);
	}

	private BooleanExpressionContext booleanExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanExpressionContext _localctx = new BooleanExpressionContext(_ctx, _parentState);
		BooleanExpressionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_booleanExpression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(20);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				_localctx = new LogicalNotContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(13);
				match(NOT);
				setState(14);
				booleanExpression(5);
				}
				break;
			case NAME:
				{
				_localctx = new AtomCalendarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(15);
				match(NAME);
				}
				break;
			case T__0:
				{
				_localctx = new ParenthesizedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(16);
				match(T__0);
				setState(17);
				booleanExpression(0);
				setState(18);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(30);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(28);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalAndContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalAndContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(22);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(23);
						match(AND);
						setState(24);
						((LogicalAndContext)_localctx).right = booleanExpression(5);
						}
						break;
					case 2:
						{
						_localctx = new LogicalOrContext(new BooleanExpressionContext(_parentctx, _parentState));
						((LogicalOrContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_booleanExpression);
						setState(25);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(26);
						match(OR);
						setState(27);
						((LogicalOrContext)_localctx).right = booleanExpression(4);
						}
						break;
					}
					} 
				}
				setState(32);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ErrorContext extends ParserRuleContext {
		public Token UNEXPECTED_CHAR;
		public TerminalNode UNEXPECTED_CHAR() { return getToken(CalendarExpressionParser.UNEXPECTED_CHAR, 0); }
		public ErrorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_error; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).enterError(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CalendarExpressionListener ) ((CalendarExpressionListener)listener).exitError(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CalendarExpressionVisitor ) return ((CalendarExpressionVisitor<? extends T>)visitor).visitError(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorContext error() throws RecognitionException {
		ErrorContext _localctx = new ErrorContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_error);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			((ErrorContext)_localctx).UNEXPECTED_CHAR = match(UNEXPECTED_CHAR);

			          throw new RuntimeException("UNEXPECTED_CHAR=" + (((ErrorContext)_localctx).UNEXPECTED_CHAR!=null?((ErrorContext)_localctx).UNEXPECTED_CHAR.getText():null));
			      
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return booleanExpression_sempred((BooleanExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean booleanExpression_sempred(BooleanExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\n\'\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\3\2\3\2\5\2\13\n\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\5\3\27\n\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3\37\n\3\f\3\16\3\"\13\3\3\4\3\4"+
		"\3\4\3\4\2\3\4\5\2\4\6\2\2\2(\2\n\3\2\2\2\4\26\3\2\2\2\6#\3\2\2\2\b\13"+
		"\5\4\3\2\t\13\5\6\4\2\n\b\3\2\2\2\n\t\3\2\2\2\13\f\3\2\2\2\f\r\7\2\2\3"+
		"\r\3\3\2\2\2\16\17\b\3\1\2\17\20\7\6\2\2\20\27\5\4\3\7\21\27\7\b\2\2\22"+
		"\23\7\3\2\2\23\24\5\4\3\2\24\25\7\4\2\2\25\27\3\2\2\2\26\16\3\2\2\2\26"+
		"\21\3\2\2\2\26\22\3\2\2\2\27 \3\2\2\2\30\31\f\6\2\2\31\32\7\5\2\2\32\37"+
		"\5\4\3\7\33\34\f\5\2\2\34\35\7\7\2\2\35\37\5\4\3\6\36\30\3\2\2\2\36\33"+
		"\3\2\2\2\37\"\3\2\2\2 \36\3\2\2\2 !\3\2\2\2!\5\3\2\2\2\" \3\2\2\2#$\7"+
		"\n\2\2$%\b\4\1\2%\7\3\2\2\2\6\n\26\36 ";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}