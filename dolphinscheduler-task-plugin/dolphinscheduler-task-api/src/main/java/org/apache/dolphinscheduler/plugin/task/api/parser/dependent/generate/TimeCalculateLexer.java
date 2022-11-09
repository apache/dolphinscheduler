package org.apache.dolphinscheduler.plugin.task.api.parser.dependent.generate;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TimeCalculateLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"DOT", "COLON", "LEFT_PAREN", "RIGHT_PAREN", "C", "E", "Y", "UPPER_M", 
			"W", "D", "H", "LOWER_M", "S", "WOY", "DOW", "DOY", "EQ", "NEQ", "NEQJ", 
			"LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", "SLASH", "MOD", 
			"FDIV", "EXPONAL", "LPAPED_TWO_DIGIT", "TWO_INTEGER", "ZERO", "ONE_TO_NINE", 
			"DIGIT", "DECIMAL_DIGITS", "WS"
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


	public TimeCalculateLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "TimeCalculateLexer.g4"; }

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
		"\u0004\u0000#\u00c6\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
		"\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002"+
		"\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002"+
		"\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007"+
		"!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010"+
		"u\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0003\u0014\u0083\b\u0014\u0001\u0015\u0001\u0015\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u008b\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u001a"+
		"\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u00a4\b\u001f"+
		"\u0001 \u0001 \u0001!\u0001!\u0001\"\u0001\"\u0001#\u0004#\u00ad\b#\u000b"+
		"#\f#\u00ae\u0001#\u0001#\u0005#\u00b3\b#\n#\f#\u00b6\t#\u0001#\u0001#"+
		"\u0004#\u00ba\b#\u000b#\f#\u00bb\u0003#\u00be\b#\u0001$\u0004$\u00c1\b"+
		"$\u000b$\f$\u00c2\u0001$\u0001$\u0000\u0000%\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n"+
		"\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011"+
		"#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a5\u001b"+
		"7\u001c9\u001d;\u001e=\u001f? A!C\"E\u0000G\u0000I#\u0001\u0000\f\u0002"+
		"\u0000CCcc\u0002\u0000EEee\u0002\u0000YYyy\u0002\u0000WWww\u0002\u0000"+
		"DDdd\u0002\u0000HHhh\u0002\u0000SSss\u0002\u0000OOoo\u0001\u000029\u0001"+
		"\u000019\u0001\u000009\u0003\u0000\t\n\r\r  \u00cc\u0000\u0001\u0001\u0000"+
		"\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000"+
		"\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000"+
		"\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000"+
		"\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000"+
		"\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000"+
		"\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000"+
		"\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000"+
		"\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000"+
		"#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001"+
		"\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000"+
		"\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u0000"+
		"1\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005\u0001"+
		"\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u00009\u0001\u0000\u0000"+
		"\u0000\u0000;\u0001\u0000\u0000\u0000\u0000=\u0001\u0000\u0000\u0000\u0000"+
		"?\u0001\u0000\u0000\u0000\u0000A\u0001\u0000\u0000\u0000\u0000C\u0001"+
		"\u0000\u0000\u0000\u0000I\u0001\u0000\u0000\u0000\u0001K\u0001\u0000\u0000"+
		"\u0000\u0003M\u0001\u0000\u0000\u0000\u0005O\u0001\u0000\u0000\u0000\u0007"+
		"Q\u0001\u0000\u0000\u0000\tS\u0001\u0000\u0000\u0000\u000bU\u0001\u0000"+
		"\u0000\u0000\rW\u0001\u0000\u0000\u0000\u000fY\u0001\u0000\u0000\u0000"+
		"\u0011[\u0001\u0000\u0000\u0000\u0013]\u0001\u0000\u0000\u0000\u0015_"+
		"\u0001\u0000\u0000\u0000\u0017a\u0001\u0000\u0000\u0000\u0019c\u0001\u0000"+
		"\u0000\u0000\u001be\u0001\u0000\u0000\u0000\u001di\u0001\u0000\u0000\u0000"+
		"\u001fm\u0001\u0000\u0000\u0000!t\u0001\u0000\u0000\u0000#v\u0001\u0000"+
		"\u0000\u0000%y\u0001\u0000\u0000\u0000\'|\u0001\u0000\u0000\u0000)\u0082"+
		"\u0001\u0000\u0000\u0000+\u0084\u0001\u0000\u0000\u0000-\u008a\u0001\u0000"+
		"\u0000\u0000/\u008c\u0001\u0000\u0000\u00001\u008e\u0001\u0000\u0000\u0000"+
		"3\u0090\u0001\u0000\u0000\u00005\u0092\u0001\u0000\u0000\u00007\u0094"+
		"\u0001\u0000\u0000\u00009\u0096\u0001\u0000\u0000\u0000;\u0099\u0001\u0000"+
		"\u0000\u0000=\u009c\u0001\u0000\u0000\u0000?\u00a3\u0001\u0000\u0000\u0000"+
		"A\u00a5\u0001\u0000\u0000\u0000C\u00a7\u0001\u0000\u0000\u0000E\u00a9"+
		"\u0001\u0000\u0000\u0000G\u00bd\u0001\u0000\u0000\u0000I\u00c0\u0001\u0000"+
		"\u0000\u0000KL\u0005.\u0000\u0000L\u0002\u0001\u0000\u0000\u0000MN\u0005"+
		":\u0000\u0000N\u0004\u0001\u0000\u0000\u0000OP\u0005(\u0000\u0000P\u0006"+
		"\u0001\u0000\u0000\u0000QR\u0005)\u0000\u0000R\b\u0001\u0000\u0000\u0000"+
		"ST\u0007\u0000\u0000\u0000T\n\u0001\u0000\u0000\u0000UV\u0007\u0001\u0000"+
		"\u0000V\f\u0001\u0000\u0000\u0000WX\u0007\u0002\u0000\u0000X\u000e\u0001"+
		"\u0000\u0000\u0000YZ\u0005M\u0000\u0000Z\u0010\u0001\u0000\u0000\u0000"+
		"[\\\u0007\u0003\u0000\u0000\\\u0012\u0001\u0000\u0000\u0000]^\u0007\u0004"+
		"\u0000\u0000^\u0014\u0001\u0000\u0000\u0000_`\u0007\u0005\u0000\u0000"+
		"`\u0016\u0001\u0000\u0000\u0000ab\u0005m\u0000\u0000b\u0018\u0001\u0000"+
		"\u0000\u0000cd\u0007\u0006\u0000\u0000d\u001a\u0001\u0000\u0000\u0000"+
		"ef\u0007\u0003\u0000\u0000fg\u0007\u0007\u0000\u0000gh\u0007\u0002\u0000"+
		"\u0000h\u001c\u0001\u0000\u0000\u0000ij\u0007\u0004\u0000\u0000jk\u0007"+
		"\u0007\u0000\u0000kl\u0007\u0003\u0000\u0000l\u001e\u0001\u0000\u0000"+
		"\u0000mn\u0007\u0004\u0000\u0000no\u0007\u0007\u0000\u0000op\u0007\u0002"+
		"\u0000\u0000p \u0001\u0000\u0000\u0000qu\u0005=\u0000\u0000rs\u0005=\u0000"+
		"\u0000su\u0005=\u0000\u0000tq\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000"+
		"\u0000u\"\u0001\u0000\u0000\u0000vw\u0005<\u0000\u0000wx\u0005>\u0000"+
		"\u0000x$\u0001\u0000\u0000\u0000yz\u0005!\u0000\u0000z{\u0005=\u0000\u0000"+
		"{&\u0001\u0000\u0000\u0000|}\u0005<\u0000\u0000}(\u0001\u0000\u0000\u0000"+
		"~\u007f\u0005<\u0000\u0000\u007f\u0083\u0005=\u0000\u0000\u0080\u0081"+
		"\u0005!\u0000\u0000\u0081\u0083\u0005>\u0000\u0000\u0082~\u0001\u0000"+
		"\u0000\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0083*\u0001\u0000\u0000"+
		"\u0000\u0084\u0085\u0005>\u0000\u0000\u0085,\u0001\u0000\u0000\u0000\u0086"+
		"\u0087\u0005>\u0000\u0000\u0087\u008b\u0005=\u0000\u0000\u0088\u0089\u0005"+
		"!\u0000\u0000\u0089\u008b\u0005<\u0000\u0000\u008a\u0086\u0001\u0000\u0000"+
		"\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008b.\u0001\u0000\u0000\u0000"+
		"\u008c\u008d\u0005+\u0000\u0000\u008d0\u0001\u0000\u0000\u0000\u008e\u008f"+
		"\u0005-\u0000\u0000\u008f2\u0001\u0000\u0000\u0000\u0090\u0091\u0005*"+
		"\u0000\u0000\u00914\u0001\u0000\u0000\u0000\u0092\u0093\u0005/\u0000\u0000"+
		"\u00936\u0001\u0000\u0000\u0000\u0094\u0095\u0005%\u0000\u0000\u00958"+
		"\u0001\u0000\u0000\u0000\u0096\u0097\u0005/\u0000\u0000\u0097\u0098\u0005"+
		"/\u0000\u0000\u0098:\u0001\u0000\u0000\u0000\u0099\u009a\u0005*\u0000"+
		"\u0000\u009a\u009b\u0005*\u0000\u0000\u009b<\u0001\u0000\u0000\u0000\u009c"+
		"\u009d\u00050\u0000\u0000\u009d\u009e\u0003E\"\u0000\u009e>\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u00051\u0000\u0000\u00a0\u00a4\u0003E\"\u0000"+
		"\u00a1\u00a2\u0007\b\u0000\u0000\u00a2\u00a4\u0003E\"\u0000\u00a3\u009f"+
		"\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a4@\u0001"+
		"\u0000\u0000\u0000\u00a5\u00a6\u00050\u0000\u0000\u00a6B\u0001\u0000\u0000"+
		"\u0000\u00a7\u00a8\u0007\t\u0000\u0000\u00a8D\u0001\u0000\u0000\u0000"+
		"\u00a9\u00aa\u0007\n\u0000\u0000\u00aaF\u0001\u0000\u0000\u0000\u00ab"+
		"\u00ad\u0003E\"\u0000\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ad\u00ae"+
		"\u0001\u0000\u0000\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00ae\u00af"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\u00b4"+
		"\u0005.\u0000\u0000\u00b1\u00b3\u0003E\"\u0000\u00b2\u00b1\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b6\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000"+
		"\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5\u00be\u0001\u0000"+
		"\u0000\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00b9\u0005.\u0000"+
		"\u0000\u00b8\u00ba\u0003E\"\u0000\u00b9\u00b8\u0001\u0000\u0000\u0000"+
		"\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00b9\u0001\u0000\u0000\u0000"+
		"\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00be\u0001\u0000\u0000\u0000"+
		"\u00bd\u00ac\u0001\u0000\u0000\u0000\u00bd\u00b7\u0001\u0000\u0000\u0000"+
		"\u00beH\u0001\u0000\u0000\u0000\u00bf\u00c1\u0007\u000b\u0000\u0000\u00c0"+
		"\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2"+
		"\u00c0\u0001\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000\u00c3"+
		"\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c5\u0006$\u0000\u0000\u00c5J\u0001"+
		"\u0000\u0000\u0000\n\u0000t\u0082\u008a\u00a3\u00ae\u00b4\u00bb\u00bd"+
		"\u00c2\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}