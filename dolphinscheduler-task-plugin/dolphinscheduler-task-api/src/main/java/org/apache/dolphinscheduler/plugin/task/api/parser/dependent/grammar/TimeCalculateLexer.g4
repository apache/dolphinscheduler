lexer grammar TimeCalculateLexer;
options { caseInsensitive=true;}

/////////////////////////////////////////////
//////////////// lexer start ////////////////
/////////////////////////////////////////////

// ************ punctuation ************ //
DOT : '.';
COLON : ':';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';

// ************ time init ************ //
C: 'C';
E: 'E';

// ************ time reference ∩ unit token ************ //
Y: 'Y';
UPPER_M options { caseInsensitive=false; }: 'M';
W: 'W';
D: 'D';
H: 'H';
LOWER_M options { caseInsensitive=false; }: 'm';
S:'S';

// ************ time reference ************ //
WOY: 'WOY';
DOW: 'DOW';
DOY: 'DOY';

// ************ comparison operators ************ //
EQ  : '=' | '==';
NEQ : '<>';
NEQJ: '!=';
LT  : '<';
LTE : '<=' | '!>';
GT  : '>';
GTE : '>=' | '!<';

// ************ numerical operators ************ //
PLUS: '+';
MINUS: '-';
ASTERISK: '*';
SLASH: '/';
MOD: '%';
FDIV: '//';
EXPONAL: '**';

// ************ numerical value ************ //
LPAPED_TWO_DIGIT
    :'0' DIGIT
    ;
TWO_INTEGER
    : '1' DIGIT
    | [2-9]DIGIT
    ;
ZERO
    : '0'
    ;
ONE_TO_NINE
    : [1-9]
    ;

fragment DIGIT
    : [0-9]
    ;

fragment DECIMAL_DIGITS
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    ;

// ************ space value ************ //
WS
    : [ \r\n\t]+ -> skip
    ;
