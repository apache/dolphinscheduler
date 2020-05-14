grammar CalendarExpression;

expression
    : (booleanExpression | error) EOF
    ;

booleanExpression
    : NOT booleanExpression                                         # logicalNot
    | left=booleanExpression AND right=booleanExpression            # logicalAnd
    | left=booleanExpression OR right=booleanExpression             # logicalOr
    | NAME                                                          # atomCalendar
    | '(' booleanExpression ')'                                     # parenthesizedExpression
    ;

error
    : UNEXPECTED_CHAR
      {
          throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text);
      }
    ;

AND     : A N D ;
NOT     : N O T ;
OR      : O R ;

NAME                    : (ZM | HZ) (SZ | ZM | HZ | '_' | '-' | '.')* ;
SPACES                  : [ \u000B\t\r\n] -> channel(HIDDEN) ;
UNEXPECTED_CHAR         : . ;

fragment A : [aA] ;
fragment B : [bB] ;
fragment C : [cC] ;
fragment D : [dD] ;
fragment E : [eE] ;
fragment F : [fF] ;
fragment G : [gG] ;
fragment H : [hH] ;
fragment I : [iI] ;
fragment J : [jJ] ;
fragment K : [kK] ;
fragment L : [lL] ;
fragment M : [mM] ;
fragment N : [nN] ;
fragment O : [oO] ;
fragment P : [pP] ;
fragment Q : [qQ] ;
fragment R : [rR] ;
fragment S : [sS] ;
fragment T : [tT] ;
fragment U : [uU] ;
fragment V : [vV] ;
fragment W : [wW] ;
fragment X : [xX] ;
fragment Y : [yY] ;
fragment Z : [zZ] ;

fragment SZ : [0-9] ;
fragment ZM : [a-zA-Z] ;
fragment HZ : [\u4E00-\u9FA5] ;