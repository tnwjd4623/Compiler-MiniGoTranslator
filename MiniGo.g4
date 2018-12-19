grammar MiniGo;
program		: decl+	 ;
decl		: var_decl 
			| fun_decl ;
var_decl	: dec_spec IDENT type_spec  
			| dec_spec IDENT type_spec '=' LITERAL  
			| dec_spec IDENT '[' LITERAL ']' type_spec ;
dec_spec	: VAR ;
type_spec	: INT 
			| ;
fun_decl	: FUNC IDENT '(' params ')' type_spec compound_stmt 
			| FUNC IDENT '(' params ')' '(' type_spec ',' type_spec ')' compound_stmt ;
params		: param(',' param)* 
			| ;
param		: IDENT 
			| IDENT type_spec	;
stmt		: expr_stmt 
			| compound_stmt 
			| if_stmt 
			| for_stmt 
			| return_stmt ;
expr_stmt	: expr	;
for_stmt	: FOR loop_expr stmt	
			| FOR expr stmt	;
compound_stmt: '{' local_decl* stmt* '}' ;
local_decl	: dec_spec IDENT type_spec  
			| dec_spec IDENT type_spec '=' LITERAL  
			| dec_spec IDENT '[' LITERAL ']' type_spec ;
if_stmt		: IF expr stmt 
			| IF expr stmt ELSE stmt ;
return_stmt	: RETURN expr
			| RETURN expr ',' expr
			| RETURN;
loop_expr   : expr ';' expr ';' expr ('++'|'--') ;
expr		: '(' expr ')' 
			| IDENT '[' expr ']' 
			| IDENT '(' args ')' 
			| FMT '.' IDENT '(' args ')' 
			| op=('-'|'+'|'--'|'++'|'!') expr 
			| left=expr op=('*'|'/') right=expr 
			| left=expr op=('%'|'+'|'-') right=expr 
			| left=expr op=(EQ|NE|LE|'<'|GE|'>'|AND|OR) right=expr 
			| IDENT '=' expr 
			| IDENT '[' expr ']' '=' expr
			| (LITERAL|IDENT) ;
args		: expr (',' expr) * 
			| ;
VAR			: 'var'   ;
FUNC		: 'func'  ;
FMT			: 'fmt'	  ;
INT			: 'int'   ;
FOR			: 'for'   ;
IF			: 'if'    ;
ELSE		: 'else'  ;
RETURN		: 'return';
OR			: 'or'    ;
AND			: 'and'   ;
LE			: '<='    ;
GE			: '>='    ;
EQ			: '=='    ;
NE			: '!='    ;

IDENT		: [a-zA-Z_] 
			( [a-zA-Z_]
			| [0-9]
			)*;
			
LITERAL		: DecimalConstant | OctalConstant | HexadecimalConstant ;

DecimalConstant	: '0' | [1-9] [0-9]* ;
OctalConstant	: '0' [0-7]* ;
HexadecimalConstant	: '0' [xX] [0-9a-fA-F]+ ;
WS			: (' '
			| '\t'
			| '\r'
			| '\n'        
			)+
	-> channel(HIDDEN)	 
    ;
