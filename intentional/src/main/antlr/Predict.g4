grammar Predict;
@header {
package it.unibo.antlr.gen;
}

predict : 'with' cube=id 'predict' mc=measure
             (('for' sc=clause)? 'by' gc+=id (',' gc+=id)* | 'by' gc+=id (',' gc+=id)* ('for' sc=clause)?)
             ('from' against+=measure (',' against+=measure)*)?
             ('using' using+=id (',' using+=id)*)?
             ('nullify' nullify=INT)?
             ('accuracysize' accuracysize=INT)?
             ('executionid' executionid=ID)?
             EOF;

measure
  : ID
  | ('avg' | 'sum' | 'min' | 'max') '(' ID ') as ' ID
  | ('avg' | 'sum' | 'min' | 'max') '(' ID ')';

id locals[String name] : ID { $name = $ID.text; };

clause : condition (binary condition)*;

condition
  : attr=ID op=comparator val+=value
  | attr=ID in=IN '(' val+=value (',' val+=value)* ')'
  | attr=ID in=BETWEEN '[' val+=value + ',' + val+=value ']';

value
  : ID
  | DECIMAL 
  | INT 
  | bool;

comparator
  : GE 
  | LE
  | EQ
  | GT
  | LT
  | LIKE;

binary: AND;
bool: TRUE | FALSE;

LIKE       : 'LIKE' | 'like';
BETWEEN    : 'BETWEEN' | 'between';
IN         : 'IN' | 'in';
AND        : 'AND' | 'and'; 
NOT        : 'NOT' | 'not';
TRUE       : 'TRUE' | 'true';
FALSE      : 'FALSE' | 'false';
GT         : '>';
GE         : '>=';
LT         : '<';
LE         : '<=';
EQ         : '=';
DECIMAL    : '-'? [0-9]+ '.' [0-9]+;
INT        : '-'? [0-9]+;
ID
  : '\'' [a-zA-Z0-9'_'\-'/''.'% :]+ '\''
  |      [a-zA-Z0-9'_'\-'/''.']+ ;
WS         : [ \t\r\n]+ -> skip;