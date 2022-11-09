parser grammar TimeCalculateParser;
options { tokenVocab = TimeCalculateLexer; }

timeCalc
    : dateTime interval? EOF
    ;

dateTime
    : abbrDate
    | abbrDate timeInit time?
    | fullDate? timeInit? time?
    ;

abbrDate
    : yearValue
    | yearValue monthValue
    ;

fullDate
    : yearValue monthValue dayValue
    ;

timeInit
    : C
    | S
    | E
    | DOT
    ;

time
    : hourValue
    | hourValue?':'minuteValue
    | hourValue?':'minuteValue?':'secondValue
    ;
yearValue
    : fourDigit
    | Y
    ;
fourDigit
    : TWO_INTEGER twoDigit
    ;

monthValue
    : UPPER_M
    | twoDigit
    ;
dayValue
    : D
    | twoDigit
    ;
hourValue
    : H
    | twoDigit
    ;
minuteValue
    : LOWER_M
    | twoDigit
    ;
secondValue
    : S
    | twoDigit
    ;
twoDigit
    : LPAPED_TWO_DIGIT
    | TWO_INTEGER
    ;

interval
    : intervalSign firstIntervalField intervalField*
    | intervalSign integerValue
    ;
firstIntervalField
    : value=intervalValue unit=intervalUnit
    ;
intervalField
    : sign=intervalSign? value=intervalValue unit=intervalUnit
    ;

intervalSign
    : PLUS
    | MINUS
    ;
intervalValue
    : integerValue
    | refVar
    | '('expr')'
    ;
refVar
    : Y
    | UPPER_M
    | D
    | H
    | LOWER_M
    | S
    | WOY
    | DOW
    | DOY
    ;
integerValue
    : TWO_INTEGER
    | ZERO
    | ONE_TO_NINE
    | TWO_INTEGER (twoDigit|ZERO|ONE_TO_NINE)+
    ;
expr:	<assoc=right> left=expr op=EXPONAL right=expr                 # Exponal
    |   left=expr op=(ASTERISK | FDIV | SLASH | MOD) right=expr       # MulDivMod
    |   left=expr op=(PLUS | MINUS) right=expr                        # PlusOrMinus
    |   left=expr op=comparisonOperator right=expr                    # Comparison
    |	integerValue                                                  # Number
    |   refVar                                                        # RefDTVar
    |	'(' inner=expr ')'                                            # Parentheses
    ;
comparisonOperator
    : EQ | NEQ | NEQJ | LT | LTE | GT | GTE
    ;
intervalUnit
    : D
    | H
    | LOWER_M
    | UPPER_M
    | S
    | W
    | Y
    ;