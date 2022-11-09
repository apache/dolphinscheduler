package org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TimeCalculateParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DOT=1, COLON=2, LEFT_PAREN=3, RIGHT_PAREN=4, C=5, E=6, Y=7, UPPER_M=8, 
		W=9, D=10, H=11, LOWER_M=12, S=13, WOY=14, DOW=15, DOY=16, EQ=17, NEQ=18, 
		NEQJ=19, LT=20, LTE=21, GT=22, GTE=23, PLUS=24, MINUS=25, ASTERISK=26, 
		SLASH=27, MOD=28, FDIV=29, EXPONAL=30, LPAPED_TWO_DIGIT=31, TWO_INTEGER=32, 
		ZERO=33, ONE_TO_NINE=34, WS=35;
	public static final int
		RULE_timeCalc = 0, RULE_dateTime = 1, RULE_abbrDate = 2, RULE_fullDate = 3, 
		RULE_timeInit = 4, RULE_time = 5, RULE_yearValue = 6, RULE_fourDigit = 7, 
		RULE_monthValue = 8, RULE_dayValue = 9, RULE_hourValue = 10, RULE_minuteValue = 11, 
		RULE_secondValue = 12, RULE_twoDigit = 13, RULE_interval = 14, RULE_firstIntervalField = 15, 
		RULE_intervalField = 16, RULE_intervalSign = 17, RULE_intervalValue = 18, 
		RULE_refVar = 19, RULE_integerValue = 20, RULE_expr = 21, RULE_comparisonOperator = 22, 
		RULE_intervalUnit = 23;
	private static String[] makeRuleNames() {
		return new String[] {
			"timeCalc", "dateTime", "abbrDate", "fullDate", "timeInit", "time", "yearValue", 
			"fourDigit", "monthValue", "dayValue", "hourValue", "minuteValue", "secondValue", 
			"twoDigit", "interval", "firstIntervalField", "intervalField", "intervalSign", 
			"intervalValue", "refVar", "integerValue", "expr", "comparisonOperator", 
			"intervalUnit"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "':'", "'('", "')'", "'C'", "'E'", "'Y'", null, "'W'", "'D'", 
			"'H'", null, "'S'", "'WOY'", "'DOW'", "'DOY'", null, "'<>'", "'!='", 
			"'<'", null, "'>'", null, "'+'", "'-'", "'*'", "'/'", "'%'", "'//'", 
			"'**'", null, null, "'0'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "DOT", "COLON", "LEFT_PAREN", "RIGHT_PAREN", "C", "E", "Y", "UPPER_M", 
			"W", "D", "H", "LOWER_M", "S", "WOY", "DOW", "DOY", "EQ", "NEQ", "NEQJ", 
			"LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", "SLASH", "MOD", 
			"FDIV", "EXPONAL", "LPAPED_TWO_DIGIT", "TWO_INTEGER", "ZERO", "ONE_TO_NINE", 
			"WS"
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
	public String getGrammarFileName() { return "TimeCalculateParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TimeCalculateParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class TimeCalcContext extends ParserRuleContext {
		public DateTimeContext dateTime() {
			return getRuleContext(DateTimeContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TimeCalculateParser.EOF, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public TimeCalcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeCalc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterTimeCalc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitTimeCalc(this);
		}
	}

	public final TimeCalcContext timeCalc() throws RecognitionException {
		TimeCalcContext _localctx = new TimeCalcContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_timeCalc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			dateTime();
			setState(50);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(49);
				interval();
				}
			}

			setState(52);
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

	public static class DateTimeContext extends ParserRuleContext {
		public AbbrDateContext abbrDate() {
			return getRuleContext(AbbrDateContext.class,0);
		}
		public TimeInitContext timeInit() {
			return getRuleContext(TimeInitContext.class,0);
		}
		public TimeContext time() {
			return getRuleContext(TimeContext.class,0);
		}
		public FullDateContext fullDate() {
			return getRuleContext(FullDateContext.class,0);
		}
		public DateTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterDateTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitDateTime(this);
		}
	}

	public final DateTimeContext dateTime() throws RecognitionException {
		DateTimeContext _localctx = new DateTimeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_dateTime);
		int _la;
		try {
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(54);
				abbrDate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				abbrDate();
				setState(56);
				timeInit();
				setState(58);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << COLON) | (1L << H) | (1L << LPAPED_TWO_DIGIT) | (1L << TWO_INTEGER))) != 0)) {
					{
					setState(57);
					time();
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(61);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(60);
					fullDate();
					}
					break;
				}
				setState(64);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DOT) | (1L << C) | (1L << E) | (1L << S))) != 0)) {
					{
					setState(63);
					timeInit();
					}
				}

				setState(67);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << COLON) | (1L << H) | (1L << LPAPED_TWO_DIGIT) | (1L << TWO_INTEGER))) != 0)) {
					{
					setState(66);
					time();
					}
				}

				}
				break;
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

	public static class AbbrDateContext extends ParserRuleContext {
		public YearValueContext yearValue() {
			return getRuleContext(YearValueContext.class,0);
		}
		public MonthValueContext monthValue() {
			return getRuleContext(MonthValueContext.class,0);
		}
		public AbbrDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_abbrDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterAbbrDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitAbbrDate(this);
		}
	}

	public final AbbrDateContext abbrDate() throws RecognitionException {
		AbbrDateContext _localctx = new AbbrDateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_abbrDate);
		try {
			setState(75);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				yearValue();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(72);
				yearValue();
				setState(73);
				monthValue();
				}
				break;
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

	public static class FullDateContext extends ParserRuleContext {
		public YearValueContext yearValue() {
			return getRuleContext(YearValueContext.class,0);
		}
		public MonthValueContext monthValue() {
			return getRuleContext(MonthValueContext.class,0);
		}
		public DayValueContext dayValue() {
			return getRuleContext(DayValueContext.class,0);
		}
		public FullDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterFullDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitFullDate(this);
		}
	}

	public final FullDateContext fullDate() throws RecognitionException {
		FullDateContext _localctx = new FullDateContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_fullDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			yearValue();
			setState(78);
			monthValue();
			setState(79);
			dayValue();
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

	public static class TimeInitContext extends ParserRuleContext {
		public TerminalNode C() { return getToken(TimeCalculateParser.C, 0); }
		public TerminalNode S() { return getToken(TimeCalculateParser.S, 0); }
		public TerminalNode E() { return getToken(TimeCalculateParser.E, 0); }
		public TerminalNode DOT() { return getToken(TimeCalculateParser.DOT, 0); }
		public TimeInitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeInit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterTimeInit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitTimeInit(this);
		}
	}

	public final TimeInitContext timeInit() throws RecognitionException {
		TimeInitContext _localctx = new TimeInitContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_timeInit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DOT) | (1L << C) | (1L << E) | (1L << S))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	public static class TimeContext extends ParserRuleContext {
		public HourValueContext hourValue() {
			return getRuleContext(HourValueContext.class,0);
		}
		public List<TerminalNode> COLON() { return getTokens(TimeCalculateParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(TimeCalculateParser.COLON, i);
		}
		public MinuteValueContext minuteValue() {
			return getRuleContext(MinuteValueContext.class,0);
		}
		public SecondValueContext secondValue() {
			return getRuleContext(SecondValueContext.class,0);
		}
		public TimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitTime(this);
		}
	}

	public final TimeContext time() throws RecognitionException {
		TimeContext _localctx = new TimeContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_time);
		int _la;
		try {
			setState(98);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				hourValue();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << H) | (1L << LPAPED_TWO_DIGIT) | (1L << TWO_INTEGER))) != 0)) {
					{
					setState(84);
					hourValue();
					}
				}

				setState(87);
				match(COLON);
				setState(88);
				minuteValue();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << H) | (1L << LPAPED_TWO_DIGIT) | (1L << TWO_INTEGER))) != 0)) {
					{
					setState(89);
					hourValue();
					}
				}

				setState(92);
				match(COLON);
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LOWER_M) | (1L << LPAPED_TWO_DIGIT) | (1L << TWO_INTEGER))) != 0)) {
					{
					setState(93);
					minuteValue();
					}
				}

				setState(96);
				match(COLON);
				setState(97);
				secondValue();
				}
				break;
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

	public static class YearValueContext extends ParserRuleContext {
		public FourDigitContext fourDigit() {
			return getRuleContext(FourDigitContext.class,0);
		}
		public TerminalNode Y() { return getToken(TimeCalculateParser.Y, 0); }
		public YearValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yearValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterYearValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitYearValue(this);
		}
	}

	public final YearValueContext yearValue() throws RecognitionException {
		YearValueContext _localctx = new YearValueContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_yearValue);
		try {
			setState(102);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 1);
				{
				setState(100);
				fourDigit();
				}
				break;
			case Y:
				enterOuterAlt(_localctx, 2);
				{
				setState(101);
				match(Y);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class FourDigitContext extends ParserRuleContext {
		public TerminalNode TWO_INTEGER() { return getToken(TimeCalculateParser.TWO_INTEGER, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public FourDigitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fourDigit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterFourDigit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitFourDigit(this);
		}
	}

	public final FourDigitContext fourDigit() throws RecognitionException {
		FourDigitContext _localctx = new FourDigitContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fourDigit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			match(TWO_INTEGER);
			setState(105);
			twoDigit();
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

	public static class MonthValueContext extends ParserRuleContext {
		public TerminalNode UPPER_M() { return getToken(TimeCalculateParser.UPPER_M, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public MonthValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_monthValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterMonthValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitMonthValue(this);
		}
	}

	public final MonthValueContext monthValue() throws RecognitionException {
		MonthValueContext _localctx = new MonthValueContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_monthValue);
		try {
			setState(109);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UPPER_M:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				match(UPPER_M);
				}
				break;
			case LPAPED_TWO_DIGIT:
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(108);
				twoDigit();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class DayValueContext extends ParserRuleContext {
		public TerminalNode D() { return getToken(TimeCalculateParser.D, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public DayValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dayValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterDayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitDayValue(this);
		}
	}

	public final DayValueContext dayValue() throws RecognitionException {
		DayValueContext _localctx = new DayValueContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_dayValue);
		try {
			setState(113);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case D:
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				match(D);
				}
				break;
			case LPAPED_TWO_DIGIT:
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(112);
				twoDigit();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class HourValueContext extends ParserRuleContext {
		public TerminalNode H() { return getToken(TimeCalculateParser.H, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public HourValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hourValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterHourValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitHourValue(this);
		}
	}

	public final HourValueContext hourValue() throws RecognitionException {
		HourValueContext _localctx = new HourValueContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_hourValue);
		try {
			setState(117);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case H:
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				match(H);
				}
				break;
			case LPAPED_TWO_DIGIT:
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(116);
				twoDigit();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class MinuteValueContext extends ParserRuleContext {
		public TerminalNode LOWER_M() { return getToken(TimeCalculateParser.LOWER_M, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public MinuteValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minuteValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterMinuteValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitMinuteValue(this);
		}
	}

	public final MinuteValueContext minuteValue() throws RecognitionException {
		MinuteValueContext _localctx = new MinuteValueContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_minuteValue);
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LOWER_M:
				enterOuterAlt(_localctx, 1);
				{
				setState(119);
				match(LOWER_M);
				}
				break;
			case LPAPED_TWO_DIGIT:
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(120);
				twoDigit();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class SecondValueContext extends ParserRuleContext {
		public TerminalNode S() { return getToken(TimeCalculateParser.S, 0); }
		public TwoDigitContext twoDigit() {
			return getRuleContext(TwoDigitContext.class,0);
		}
		public SecondValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_secondValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterSecondValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitSecondValue(this);
		}
	}

	public final SecondValueContext secondValue() throws RecognitionException {
		SecondValueContext _localctx = new SecondValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_secondValue);
		try {
			setState(125);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case S:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				match(S);
				}
				break;
			case LPAPED_TWO_DIGIT:
			case TWO_INTEGER:
				enterOuterAlt(_localctx, 2);
				{
				setState(124);
				twoDigit();
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class TwoDigitContext extends ParserRuleContext {
		public TerminalNode LPAPED_TWO_DIGIT() { return getToken(TimeCalculateParser.LPAPED_TWO_DIGIT, 0); }
		public TerminalNode TWO_INTEGER() { return getToken(TimeCalculateParser.TWO_INTEGER, 0); }
		public TwoDigitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_twoDigit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterTwoDigit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitTwoDigit(this);
		}
	}

	public final TwoDigitContext twoDigit() throws RecognitionException {
		TwoDigitContext _localctx = new TwoDigitContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_twoDigit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			_la = _input.LA(1);
			if ( !(_la==LPAPED_TWO_DIGIT || _la==TWO_INTEGER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	public static class IntervalContext extends ParserRuleContext {
		public IntervalSignContext intervalSign() {
			return getRuleContext(IntervalSignContext.class,0);
		}
		public FirstIntervalFieldContext firstIntervalField() {
			return getRuleContext(FirstIntervalFieldContext.class,0);
		}
		public List<IntervalFieldContext> intervalField() {
			return getRuleContexts(IntervalFieldContext.class);
		}
		public IntervalFieldContext intervalField(int i) {
			return getRuleContext(IntervalFieldContext.class,i);
		}
		public IntegerValueContext integerValue() {
			return getRuleContext(IntegerValueContext.class,0);
		}
		public IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitInterval(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_interval);
		int _la;
		try {
			setState(140);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(129);
				intervalSign();
				setState(130);
				firstIntervalField();
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LEFT_PAREN) | (1L << Y) | (1L << UPPER_M) | (1L << D) | (1L << H) | (1L << LOWER_M) | (1L << S) | (1L << WOY) | (1L << DOW) | (1L << DOY) | (1L << PLUS) | (1L << MINUS) | (1L << TWO_INTEGER) | (1L << ZERO) | (1L << ONE_TO_NINE))) != 0)) {
					{
					{
					setState(131);
					intervalField();
					}
					}
					setState(136);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(137);
				intervalSign();
				setState(138);
				integerValue();
				}
				break;
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

	public static class FirstIntervalFieldContext extends ParserRuleContext {
		public IntervalValueContext value;
		public IntervalUnitContext unit;
		public IntervalValueContext intervalValue() {
			return getRuleContext(IntervalValueContext.class,0);
		}
		public IntervalUnitContext intervalUnit() {
			return getRuleContext(IntervalUnitContext.class,0);
		}
		public FirstIntervalFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_firstIntervalField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterFirstIntervalField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitFirstIntervalField(this);
		}
	}

	public final FirstIntervalFieldContext firstIntervalField() throws RecognitionException {
		FirstIntervalFieldContext _localctx = new FirstIntervalFieldContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_firstIntervalField);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			((FirstIntervalFieldContext)_localctx).value = intervalValue();
			setState(143);
			((FirstIntervalFieldContext)_localctx).unit = intervalUnit();
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

	public static class IntervalFieldContext extends ParserRuleContext {
		public IntervalSignContext sign;
		public IntervalValueContext value;
		public IntervalUnitContext unit;
		public IntervalValueContext intervalValue() {
			return getRuleContext(IntervalValueContext.class,0);
		}
		public IntervalUnitContext intervalUnit() {
			return getRuleContext(IntervalUnitContext.class,0);
		}
		public IntervalSignContext intervalSign() {
			return getRuleContext(IntervalSignContext.class,0);
		}
		public IntervalFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterIntervalField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitIntervalField(this);
		}
	}

	public final IntervalFieldContext intervalField() throws RecognitionException {
		IntervalFieldContext _localctx = new IntervalFieldContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_intervalField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(145);
				((IntervalFieldContext)_localctx).sign = intervalSign();
				}
			}

			setState(148);
			((IntervalFieldContext)_localctx).value = intervalValue();
			setState(149);
			((IntervalFieldContext)_localctx).unit = intervalUnit();
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

	public static class IntervalSignContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(TimeCalculateParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(TimeCalculateParser.MINUS, 0); }
		public IntervalSignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalSign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterIntervalSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitIntervalSign(this);
		}
	}

	public final IntervalSignContext intervalSign() throws RecognitionException {
		IntervalSignContext _localctx = new IntervalSignContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_intervalSign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	public static class IntervalValueContext extends ParserRuleContext {
		public IntegerValueContext integerValue() {
			return getRuleContext(IntegerValueContext.class,0);
		}
		public RefVarContext refVar() {
			return getRuleContext(RefVarContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(TimeCalculateParser.LEFT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(TimeCalculateParser.RIGHT_PAREN, 0); }
		public IntervalValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterIntervalValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitIntervalValue(this);
		}
	}

	public final IntervalValueContext intervalValue() throws RecognitionException {
		IntervalValueContext _localctx = new IntervalValueContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_intervalValue);
		try {
			setState(159);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TWO_INTEGER:
			case ZERO:
			case ONE_TO_NINE:
				enterOuterAlt(_localctx, 1);
				{
				setState(153);
				integerValue();
				}
				break;
			case Y:
			case UPPER_M:
			case D:
			case H:
			case LOWER_M:
			case S:
			case WOY:
			case DOW:
			case DOY:
				enterOuterAlt(_localctx, 2);
				{
				setState(154);
				refVar();
				}
				break;
			case LEFT_PAREN:
				enterOuterAlt(_localctx, 3);
				{
				setState(155);
				match(LEFT_PAREN);
				setState(156);
				expr(0);
				setState(157);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class RefVarContext extends ParserRuleContext {
		public TerminalNode Y() { return getToken(TimeCalculateParser.Y, 0); }
		public TerminalNode UPPER_M() { return getToken(TimeCalculateParser.UPPER_M, 0); }
		public TerminalNode D() { return getToken(TimeCalculateParser.D, 0); }
		public TerminalNode H() { return getToken(TimeCalculateParser.H, 0); }
		public TerminalNode LOWER_M() { return getToken(TimeCalculateParser.LOWER_M, 0); }
		public TerminalNode S() { return getToken(TimeCalculateParser.S, 0); }
		public TerminalNode WOY() { return getToken(TimeCalculateParser.WOY, 0); }
		public TerminalNode DOW() { return getToken(TimeCalculateParser.DOW, 0); }
		public TerminalNode DOY() { return getToken(TimeCalculateParser.DOY, 0); }
		public RefVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refVar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterRefVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitRefVar(this);
		}
	}

	public final RefVarContext refVar() throws RecognitionException {
		RefVarContext _localctx = new RefVarContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_refVar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Y) | (1L << UPPER_M) | (1L << D) | (1L << H) | (1L << LOWER_M) | (1L << S) | (1L << WOY) | (1L << DOW) | (1L << DOY))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	public static class IntegerValueContext extends ParserRuleContext {
		public TerminalNode TWO_INTEGER() { return getToken(TimeCalculateParser.TWO_INTEGER, 0); }
		public List<TerminalNode> ZERO() { return getTokens(TimeCalculateParser.ZERO); }
		public TerminalNode ZERO(int i) {
			return getToken(TimeCalculateParser.ZERO, i);
		}
		public List<TerminalNode> ONE_TO_NINE() { return getTokens(TimeCalculateParser.ONE_TO_NINE); }
		public TerminalNode ONE_TO_NINE(int i) {
			return getToken(TimeCalculateParser.ONE_TO_NINE, i);
		}
		public List<TwoDigitContext> twoDigit() {
			return getRuleContexts(TwoDigitContext.class);
		}
		public TwoDigitContext twoDigit(int i) {
			return getRuleContext(TwoDigitContext.class,i);
		}
		public IntegerValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterIntegerValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitIntegerValue(this);
		}
	}

	public final IntegerValueContext integerValue() throws RecognitionException {
		IntegerValueContext _localctx = new IntegerValueContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_integerValue);
		try {
			int _alt;
			setState(174);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(163);
				match(TWO_INTEGER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(164);
				match(ZERO);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(165);
				match(ONE_TO_NINE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(166);
				match(TWO_INTEGER);
				setState(170); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						setState(170);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case LPAPED_TWO_DIGIT:
						case TWO_INTEGER:
							{
							setState(167);
							twoDigit();
							}
							break;
						case ZERO:
							{
							setState(168);
							match(ZERO);
							}
							break;
						case ONE_TO_NINE:
							{
							setState(169);
							match(ONE_TO_NINE);
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(172); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
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

	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MulDivModContext extends ExprContext {
		public ExprContext left;
		public Token op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode ASTERISK() { return getToken(TimeCalculateParser.ASTERISK, 0); }
		public TerminalNode FDIV() { return getToken(TimeCalculateParser.FDIV, 0); }
		public TerminalNode SLASH() { return getToken(TimeCalculateParser.SLASH, 0); }
		public TerminalNode MOD() { return getToken(TimeCalculateParser.MOD, 0); }
		public MulDivModContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterMulDivMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitMulDivMod(this);
		}
	}
	public static class NumberContext extends ExprContext {
		public IntegerValueContext integerValue() {
			return getRuleContext(IntegerValueContext.class,0);
		}
		public NumberContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitNumber(this);
		}
	}
	public static class ComparisonContext extends ExprContext {
		public ExprContext left;
		public ComparisonOperatorContext op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ComparisonContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitComparison(this);
		}
	}
	public static class ExponalContext extends ExprContext {
		public ExprContext left;
		public Token op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode EXPONAL() { return getToken(TimeCalculateParser.EXPONAL, 0); }
		public ExponalContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterExponal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitExponal(this);
		}
	}
	public static class PlusOrMinusContext extends ExprContext {
		public ExprContext left;
		public Token op;
		public ExprContext right;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(TimeCalculateParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(TimeCalculateParser.MINUS, 0); }
		public PlusOrMinusContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterPlusOrMinus(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitPlusOrMinus(this);
		}
	}
	public static class RefDTVarContext extends ExprContext {
		public RefVarContext refVar() {
			return getRuleContext(RefVarContext.class,0);
		}
		public RefDTVarContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterRefDTVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitRefDTVar(this);
		}
	}
	public static class ParenthesesContext extends ExprContext {
		public ExprContext inner;
		public TerminalNode LEFT_PAREN() { return getToken(TimeCalculateParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(TimeCalculateParser.RIGHT_PAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParenthesesContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterParentheses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitParentheses(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 42;
		enterRecursionRule(_localctx, 42, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TWO_INTEGER:
			case ZERO:
			case ONE_TO_NINE:
				{
				_localctx = new NumberContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(177);
				integerValue();
				}
				break;
			case Y:
			case UPPER_M:
			case D:
			case H:
			case LOWER_M:
			case S:
			case WOY:
			case DOW:
			case DOY:
				{
				_localctx = new RefDTVarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(178);
				refVar();
				}
				break;
			case LEFT_PAREN:
				{
				_localctx = new ParenthesesContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(179);
				match(LEFT_PAREN);
				setState(180);
				((ParenthesesContext)_localctx).inner = expr(0);
				setState(181);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(200);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(198);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
					case 1:
						{
						_localctx = new ExponalContext(new ExprContext(_parentctx, _parentState));
						((ExponalContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(185);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(186);
						((ExponalContext)_localctx).op = match(EXPONAL);
						setState(187);
						((ExponalContext)_localctx).right = expr(7);
						}
						break;
					case 2:
						{
						_localctx = new MulDivModContext(new ExprContext(_parentctx, _parentState));
						((MulDivModContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(188);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(189);
						((MulDivModContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ASTERISK) | (1L << SLASH) | (1L << MOD) | (1L << FDIV))) != 0)) ) {
							((MulDivModContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(190);
						((MulDivModContext)_localctx).right = expr(7);
						}
						break;
					case 3:
						{
						_localctx = new PlusOrMinusContext(new ExprContext(_parentctx, _parentState));
						((PlusOrMinusContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(191);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(192);
						((PlusOrMinusContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
							((PlusOrMinusContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(193);
						((PlusOrMinusContext)_localctx).right = expr(6);
						}
						break;
					case 4:
						{
						_localctx = new ComparisonContext(new ExprContext(_parentctx, _parentState));
						((ComparisonContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(194);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(195);
						((ComparisonContext)_localctx).op = comparisonOperator();
						setState(196);
						((ComparisonContext)_localctx).right = expr(5);
						}
						break;
					}
					} 
				}
				setState(202);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
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

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(TimeCalculateParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(TimeCalculateParser.NEQ, 0); }
		public TerminalNode NEQJ() { return getToken(TimeCalculateParser.NEQJ, 0); }
		public TerminalNode LT() { return getToken(TimeCalculateParser.LT, 0); }
		public TerminalNode LTE() { return getToken(TimeCalculateParser.LTE, 0); }
		public TerminalNode GT() { return getToken(TimeCalculateParser.GT, 0); }
		public TerminalNode GTE() { return getToken(TimeCalculateParser.GTE, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitComparisonOperator(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << NEQ) | (1L << NEQJ) | (1L << LT) | (1L << LTE) | (1L << GT) | (1L << GTE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	public static class IntervalUnitContext extends ParserRuleContext {
		public TerminalNode D() { return getToken(TimeCalculateParser.D, 0); }
		public TerminalNode H() { return getToken(TimeCalculateParser.H, 0); }
		public TerminalNode LOWER_M() { return getToken(TimeCalculateParser.LOWER_M, 0); }
		public TerminalNode UPPER_M() { return getToken(TimeCalculateParser.UPPER_M, 0); }
		public TerminalNode S() { return getToken(TimeCalculateParser.S, 0); }
		public TerminalNode W() { return getToken(TimeCalculateParser.W, 0); }
		public TerminalNode Y() { return getToken(TimeCalculateParser.Y, 0); }
		public IntervalUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).enterIntervalUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TimeCalculateParserListener ) ((TimeCalculateParserListener)listener).exitIntervalUnit(this);
		}
	}

	public final IntervalUnitContext intervalUnit() throws RecognitionException {
		IntervalUnitContext _localctx = new IntervalUnitContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_intervalUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Y) | (1L << UPPER_M) | (1L << W) | (1L << D) | (1L << H) | (1L << LOWER_M) | (1L << S))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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
		case 21:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 7);
		case 1:
			return precpred(_ctx, 6);
		case 2:
			return precpred(_ctx, 5);
		case 3:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001#\u00d0\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0001\u0000"+
		"\u0003\u00003\b\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001;\b\u0001\u0001\u0001\u0003\u0001"+
		">\b\u0001\u0001\u0001\u0003\u0001A\b\u0001\u0001\u0001\u0003\u0001D\b"+
		"\u0001\u0003\u0001F\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0003\u0002L\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0003\u0005V\b"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005[\b\u0005\u0001"+
		"\u0005\u0001\u0005\u0003\u0005_\b\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005c\b\u0005\u0001\u0006\u0001\u0006\u0003\u0006g\b\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0003\bn\b\b\u0001\t\u0001\t"+
		"\u0003\tr\b\t\u0001\n\u0001\n\u0003\nv\b\n\u0001\u000b\u0001\u000b\u0003"+
		"\u000bz\b\u000b\u0001\f\u0001\f\u0003\f~\b\f\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0005\u000e\u0085\b\u000e\n\u000e\f\u000e\u0088"+
		"\t\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u008d\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0003\u0010\u0093\b\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012"+
		"\u00a0\b\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0004\u0014\u00ab\b\u0014"+
		"\u000b\u0014\f\u0014\u00ac\u0003\u0014\u00af\b\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u00b8\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u00c7\b\u0015\n\u0015\f\u0015"+
		"\u00ca\t\u0015\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0000\u0001*\u0018\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0016\u0018\u001a\u001c\u001e \"$&(*,.\u0000\u0007\u0003\u0000\u0001"+
		"\u0001\u0005\u0006\r\r\u0001\u0000\u001f \u0001\u0000\u0018\u0019\u0002"+
		"\u0000\u0007\b\n\u0010\u0001\u0000\u001a\u001d\u0001\u0000\u0011\u0017"+
		"\u0001\u0000\u0007\r\u00db\u00000\u0001\u0000\u0000\u0000\u0002E\u0001"+
		"\u0000\u0000\u0000\u0004K\u0001\u0000\u0000\u0000\u0006M\u0001\u0000\u0000"+
		"\u0000\bQ\u0001\u0000\u0000\u0000\nb\u0001\u0000\u0000\u0000\ff\u0001"+
		"\u0000\u0000\u0000\u000eh\u0001\u0000\u0000\u0000\u0010m\u0001\u0000\u0000"+
		"\u0000\u0012q\u0001\u0000\u0000\u0000\u0014u\u0001\u0000\u0000\u0000\u0016"+
		"y\u0001\u0000\u0000\u0000\u0018}\u0001\u0000\u0000\u0000\u001a\u007f\u0001"+
		"\u0000\u0000\u0000\u001c\u008c\u0001\u0000\u0000\u0000\u001e\u008e\u0001"+
		"\u0000\u0000\u0000 \u0092\u0001\u0000\u0000\u0000\"\u0097\u0001\u0000"+
		"\u0000\u0000$\u009f\u0001\u0000\u0000\u0000&\u00a1\u0001\u0000\u0000\u0000"+
		"(\u00ae\u0001\u0000\u0000\u0000*\u00b7\u0001\u0000\u0000\u0000,\u00cb"+
		"\u0001\u0000\u0000\u0000.\u00cd\u0001\u0000\u0000\u000002\u0003\u0002"+
		"\u0001\u000013\u0003\u001c\u000e\u000021\u0001\u0000\u0000\u000023\u0001"+
		"\u0000\u0000\u000034\u0001\u0000\u0000\u000045\u0005\u0000\u0000\u0001"+
		"5\u0001\u0001\u0000\u0000\u00006F\u0003\u0004\u0002\u000078\u0003\u0004"+
		"\u0002\u00008:\u0003\b\u0004\u00009;\u0003\n\u0005\u0000:9\u0001\u0000"+
		"\u0000\u0000:;\u0001\u0000\u0000\u0000;F\u0001\u0000\u0000\u0000<>\u0003"+
		"\u0006\u0003\u0000=<\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000\u0000"+
		">@\u0001\u0000\u0000\u0000?A\u0003\b\u0004\u0000@?\u0001\u0000\u0000\u0000"+
		"@A\u0001\u0000\u0000\u0000AC\u0001\u0000\u0000\u0000BD\u0003\n\u0005\u0000"+
		"CB\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DF\u0001\u0000\u0000"+
		"\u0000E6\u0001\u0000\u0000\u0000E7\u0001\u0000\u0000\u0000E=\u0001\u0000"+
		"\u0000\u0000F\u0003\u0001\u0000\u0000\u0000GL\u0003\f\u0006\u0000HI\u0003"+
		"\f\u0006\u0000IJ\u0003\u0010\b\u0000JL\u0001\u0000\u0000\u0000KG\u0001"+
		"\u0000\u0000\u0000KH\u0001\u0000\u0000\u0000L\u0005\u0001\u0000\u0000"+
		"\u0000MN\u0003\f\u0006\u0000NO\u0003\u0010\b\u0000OP\u0003\u0012\t\u0000"+
		"P\u0007\u0001\u0000\u0000\u0000QR\u0007\u0000\u0000\u0000R\t\u0001\u0000"+
		"\u0000\u0000Sc\u0003\u0014\n\u0000TV\u0003\u0014\n\u0000UT\u0001\u0000"+
		"\u0000\u0000UV\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000WX\u0005"+
		"\u0002\u0000\u0000Xc\u0003\u0016\u000b\u0000Y[\u0003\u0014\n\u0000ZY\u0001"+
		"\u0000\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000"+
		"\\^\u0005\u0002\u0000\u0000]_\u0003\u0016\u000b\u0000^]\u0001\u0000\u0000"+
		"\u0000^_\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000\u0000`a\u0005\u0002"+
		"\u0000\u0000ac\u0003\u0018\f\u0000bS\u0001\u0000\u0000\u0000bU\u0001\u0000"+
		"\u0000\u0000bZ\u0001\u0000\u0000\u0000c\u000b\u0001\u0000\u0000\u0000"+
		"dg\u0003\u000e\u0007\u0000eg\u0005\u0007\u0000\u0000fd\u0001\u0000\u0000"+
		"\u0000fe\u0001\u0000\u0000\u0000g\r\u0001\u0000\u0000\u0000hi\u0005 \u0000"+
		"\u0000ij\u0003\u001a\r\u0000j\u000f\u0001\u0000\u0000\u0000kn\u0005\b"+
		"\u0000\u0000ln\u0003\u001a\r\u0000mk\u0001\u0000\u0000\u0000ml\u0001\u0000"+
		"\u0000\u0000n\u0011\u0001\u0000\u0000\u0000or\u0005\n\u0000\u0000pr\u0003"+
		"\u001a\r\u0000qo\u0001\u0000\u0000\u0000qp\u0001\u0000\u0000\u0000r\u0013"+
		"\u0001\u0000\u0000\u0000sv\u0005\u000b\u0000\u0000tv\u0003\u001a\r\u0000"+
		"us\u0001\u0000\u0000\u0000ut\u0001\u0000\u0000\u0000v\u0015\u0001\u0000"+
		"\u0000\u0000wz\u0005\f\u0000\u0000xz\u0003\u001a\r\u0000yw\u0001\u0000"+
		"\u0000\u0000yx\u0001\u0000\u0000\u0000z\u0017\u0001\u0000\u0000\u0000"+
		"{~\u0005\r\u0000\u0000|~\u0003\u001a\r\u0000}{\u0001\u0000\u0000\u0000"+
		"}|\u0001\u0000\u0000\u0000~\u0019\u0001\u0000\u0000\u0000\u007f\u0080"+
		"\u0007\u0001\u0000\u0000\u0080\u001b\u0001\u0000\u0000\u0000\u0081\u0082"+
		"\u0003\"\u0011\u0000\u0082\u0086\u0003\u001e\u000f\u0000\u0083\u0085\u0003"+
		" \u0010\u0000\u0084\u0083\u0001\u0000\u0000\u0000\u0085\u0088\u0001\u0000"+
		"\u0000\u0000\u0086\u0084\u0001\u0000\u0000\u0000\u0086\u0087\u0001\u0000"+
		"\u0000\u0000\u0087\u008d\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0089\u008a\u0003\"\u0011\u0000\u008a\u008b\u0003(\u0014"+
		"\u0000\u008b\u008d\u0001\u0000\u0000\u0000\u008c\u0081\u0001\u0000\u0000"+
		"\u0000\u008c\u0089\u0001\u0000\u0000\u0000\u008d\u001d\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0003$\u0012\u0000\u008f\u0090\u0003.\u0017\u0000\u0090"+
		"\u001f\u0001\u0000\u0000\u0000\u0091\u0093\u0003\"\u0011\u0000\u0092\u0091"+
		"\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093\u0094"+
		"\u0001\u0000\u0000\u0000\u0094\u0095\u0003$\u0012\u0000\u0095\u0096\u0003"+
		".\u0017\u0000\u0096!\u0001\u0000\u0000\u0000\u0097\u0098\u0007\u0002\u0000"+
		"\u0000\u0098#\u0001\u0000\u0000\u0000\u0099\u00a0\u0003(\u0014\u0000\u009a"+
		"\u00a0\u0003&\u0013\u0000\u009b\u009c\u0005\u0003\u0000\u0000\u009c\u009d"+
		"\u0003*\u0015\u0000\u009d\u009e\u0005\u0004\u0000\u0000\u009e\u00a0\u0001"+
		"\u0000\u0000\u0000\u009f\u0099\u0001\u0000\u0000\u0000\u009f\u009a\u0001"+
		"\u0000\u0000\u0000\u009f\u009b\u0001\u0000\u0000\u0000\u00a0%\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a2\u0007\u0003\u0000\u0000\u00a2\'\u0001\u0000\u0000"+
		"\u0000\u00a3\u00af\u0005 \u0000\u0000\u00a4\u00af\u0005!\u0000\u0000\u00a5"+
		"\u00af\u0005\"\u0000\u0000\u00a6\u00aa\u0005 \u0000\u0000\u00a7\u00ab"+
		"\u0003\u001a\r\u0000\u00a8\u00ab\u0005!\u0000\u0000\u00a9\u00ab\u0005"+
		"\"\u0000\u0000\u00aa\u00a7\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001\u0000"+
		"\u0000\u0000\u00aa\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ac\u0001\u0000"+
		"\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000"+
		"\u0000\u0000\u00ad\u00af\u0001\u0000\u0000\u0000\u00ae\u00a3\u0001\u0000"+
		"\u0000\u0000\u00ae\u00a4\u0001\u0000\u0000\u0000\u00ae\u00a5\u0001\u0000"+
		"\u0000\u0000\u00ae\u00a6\u0001\u0000\u0000\u0000\u00af)\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b1\u0006\u0015\uffff\uffff\u0000\u00b1\u00b8\u0003(\u0014"+
		"\u0000\u00b2\u00b8\u0003&\u0013\u0000\u00b3\u00b4\u0005\u0003\u0000\u0000"+
		"\u00b4\u00b5\u0003*\u0015\u0000\u00b5\u00b6\u0005\u0004\u0000\u0000\u00b6"+
		"\u00b8\u0001\u0000\u0000\u0000\u00b7\u00b0\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b2\u0001\u0000\u0000\u0000\u00b7\u00b3\u0001\u0000\u0000\u0000\u00b8"+
		"\u00c8\u0001\u0000\u0000\u0000\u00b9\u00ba\n\u0007\u0000\u0000\u00ba\u00bb"+
		"\u0005\u001e\u0000\u0000\u00bb\u00c7\u0003*\u0015\u0007\u00bc\u00bd\n"+
		"\u0006\u0000\u0000\u00bd\u00be\u0007\u0004\u0000\u0000\u00be\u00c7\u0003"+
		"*\u0015\u0007\u00bf\u00c0\n\u0005\u0000\u0000\u00c0\u00c1\u0007\u0002"+
		"\u0000\u0000\u00c1\u00c7\u0003*\u0015\u0006\u00c2\u00c3\n\u0004\u0000"+
		"\u0000\u00c3\u00c4\u0003,\u0016\u0000\u00c4\u00c5\u0003*\u0015\u0005\u00c5"+
		"\u00c7\u0001\u0000\u0000\u0000\u00c6\u00b9\u0001\u0000\u0000\u0000\u00c6"+
		"\u00bc\u0001\u0000\u0000\u0000\u00c6\u00bf\u0001\u0000\u0000\u0000\u00c6"+
		"\u00c2\u0001\u0000\u0000\u0000\u00c7\u00ca\u0001\u0000\u0000\u0000\u00c8"+
		"\u00c6\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9"+
		"+\u0001\u0000\u0000\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00cb\u00cc"+
		"\u0007\u0005\u0000\u0000\u00cc-\u0001\u0000\u0000\u0000\u00cd\u00ce\u0007"+
		"\u0006\u0000\u0000\u00ce/\u0001\u0000\u0000\u0000\u001b2:=@CEKUZ^bfmq"+
		"uy}\u0086\u008c\u0092\u009f\u00aa\u00ac\u00ae\u00b7\u00c6\u00c8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}