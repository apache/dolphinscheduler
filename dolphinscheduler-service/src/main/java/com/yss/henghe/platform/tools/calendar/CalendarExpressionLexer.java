// Generated from X:/HengHe-2.0.0/henghe-platform-tools/CalendarExpressionParser/src/main/java/com/yss/henghe/platform/tools/calendar\CalendarExpression.g4 by ANTLR 4.8
package com.yss.henghe.platform.tools.calendar;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CalendarExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, AND=3, NOT=4, OR=5, NAME=6, SPACES=7, UNEXPECTED_CHAR=8;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "AND", "NOT", "OR", "NAME", "SPACES", "UNEXPECTED_CHAR", 
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", 
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "SZ", "ZM", 
			"HZ"
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


	public CalendarExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CalendarExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\n\u00a9\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\5\7_\n\7\3\7\3\7\3\7\3\7\7\7e\n\7\f"+
		"\7\16\7h\13\7\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3"+
		"\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3"+
		"\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3"+
		"\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#"+
		"\3$\3$\3%\3%\3&\3&\2\2\'\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2"+
		"\27\2\31\2\33\2\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67\2"+
		"9\2;\2=\2?\2A\2C\2E\2G\2I\2K\2\3\2!\4\2/\60aa\5\2\13\r\17\17\"\"\4\2C"+
		"Ccc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4"+
		"\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTt"+
		"t\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\3"+
		"\2\62;\4\2C\\c|\3\2\u4e02\u9fa7\2\u0090\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\3M\3\2\2\2\5O\3\2\2\2\7Q\3\2\2\2\tU\3\2\2\2\13Y\3\2\2\2\r^\3\2\2\2\17"+
		"i\3\2\2\2\21m\3\2\2\2\23o\3\2\2\2\25q\3\2\2\2\27s\3\2\2\2\31u\3\2\2\2"+
		"\33w\3\2\2\2\35y\3\2\2\2\37{\3\2\2\2!}\3\2\2\2#\177\3\2\2\2%\u0081\3\2"+
		"\2\2\'\u0083\3\2\2\2)\u0085\3\2\2\2+\u0087\3\2\2\2-\u0089\3\2\2\2/\u008b"+
		"\3\2\2\2\61\u008d\3\2\2\2\63\u008f\3\2\2\2\65\u0091\3\2\2\2\67\u0093\3"+
		"\2\2\29\u0095\3\2\2\2;\u0097\3\2\2\2=\u0099\3\2\2\2?\u009b\3\2\2\2A\u009d"+
		"\3\2\2\2C\u009f\3\2\2\2E\u00a1\3\2\2\2G\u00a3\3\2\2\2I\u00a5\3\2\2\2K"+
		"\u00a7\3\2\2\2MN\7*\2\2N\4\3\2\2\2OP\7+\2\2P\6\3\2\2\2QR\5\23\n\2RS\5"+
		"-\27\2ST\5\31\r\2T\b\3\2\2\2UV\5-\27\2VW\5/\30\2WX\59\35\2X\n\3\2\2\2"+
		"YZ\5/\30\2Z[\5\65\33\2[\f\3\2\2\2\\_\5I%\2]_\5K&\2^\\\3\2\2\2^]\3\2\2"+
		"\2_f\3\2\2\2`e\5G$\2ae\5I%\2be\5K&\2ce\t\2\2\2d`\3\2\2\2da\3\2\2\2db\3"+
		"\2\2\2dc\3\2\2\2eh\3\2\2\2fd\3\2\2\2fg\3\2\2\2g\16\3\2\2\2hf\3\2\2\2i"+
		"j\t\3\2\2jk\3\2\2\2kl\b\b\2\2l\20\3\2\2\2mn\13\2\2\2n\22\3\2\2\2op\t\4"+
		"\2\2p\24\3\2\2\2qr\t\5\2\2r\26\3\2\2\2st\t\6\2\2t\30\3\2\2\2uv\t\7\2\2"+
		"v\32\3\2\2\2wx\t\b\2\2x\34\3\2\2\2yz\t\t\2\2z\36\3\2\2\2{|\t\n\2\2| \3"+
		"\2\2\2}~\t\13\2\2~\"\3\2\2\2\177\u0080\t\f\2\2\u0080$\3\2\2\2\u0081\u0082"+
		"\t\r\2\2\u0082&\3\2\2\2\u0083\u0084\t\16\2\2\u0084(\3\2\2\2\u0085\u0086"+
		"\t\17\2\2\u0086*\3\2\2\2\u0087\u0088\t\20\2\2\u0088,\3\2\2\2\u0089\u008a"+
		"\t\21\2\2\u008a.\3\2\2\2\u008b\u008c\t\22\2\2\u008c\60\3\2\2\2\u008d\u008e"+
		"\t\23\2\2\u008e\62\3\2\2\2\u008f\u0090\t\24\2\2\u0090\64\3\2\2\2\u0091"+
		"\u0092\t\25\2\2\u0092\66\3\2\2\2\u0093\u0094\t\26\2\2\u00948\3\2\2\2\u0095"+
		"\u0096\t\27\2\2\u0096:\3\2\2\2\u0097\u0098\t\30\2\2\u0098<\3\2\2\2\u0099"+
		"\u009a\t\31\2\2\u009a>\3\2\2\2\u009b\u009c\t\32\2\2\u009c@\3\2\2\2\u009d"+
		"\u009e\t\33\2\2\u009eB\3\2\2\2\u009f\u00a0\t\34\2\2\u00a0D\3\2\2\2\u00a1"+
		"\u00a2\t\35\2\2\u00a2F\3\2\2\2\u00a3\u00a4\t\36\2\2\u00a4H\3\2\2\2\u00a5"+
		"\u00a6\t\37\2\2\u00a6J\3\2\2\2\u00a7\u00a8\t \2\2\u00a8L\3\2\2\2\6\2^"+
		"df\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}