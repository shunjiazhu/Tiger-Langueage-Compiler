package Parse;
import ErrorMsg.ErrorMsg;
import java_cup.runtime.*;

%% 

%public
%implements java_cup.runtime.Scanner
%function next_token
%type java_cup.runtime.Symbol
%char

%{
StringBuffer string = new StringBuffer();
int count;
private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
  errorMsg.error(pos,s);
}

private void err(String s) {
  err(yychar,s);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

private ErrorMsg errorMsg;

public Yylex(java.io.InputStream s, ErrorMsg e) {
  this(s);
  errorMsg=e;
}

%}

%eofval{
	{
	 return tok(sym.EOF, null);
        }
%eofval}       

// regular expression
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}
CommentContent = ( [^*] | \*+ [^/*] )*
Identifier = [:jletter:] [:jletterdigit:]*
DecIntegerLiteral = 0 | [0-9]*

%state STRING
%state COMMENT

%%
<YYINITIAL>
{
" "	{}
{WhiteSpace} {}
"/*"    {count = 1;yybegin(COMMENT);}
"*/"   {err("Comment symbol don't match");}
{EndOfLineComment} {}
"let" {return tok(sym.LET, null);}
"in" {return tok(sym.IN, null);}
"end" {return tok(sym.END, null);}
"function" {return tok(sym.FUNCTION, null);}
"var" {return tok(sym.VAR, null);}
"type" {return tok(sym.TYPE, null);}
"int" {return tok(sym.INT, null);}
"string" {return tok(sym.STRING, null);}
"array" {return tok(sym.ARRAY, null);}
"of" {return tok(sym.OF, null);}
"for" {return tok(sym.FOR, null);}
"while" {return tok(sym.WHILE, null);}
"to" {return tok(sym.TO, null);}
"do" {return tok(sym.DO, null);}
"break" {return tok(sym.BREAK, null);}
"if" {return tok(sym.IF, null);}
"then" {return tok(sym.THEN, null);}
"else" {return tok(sym.ELSE, null);}
":=" {return tok(sym.ASSIGN, null);}
"&" {return tok(sym.AND, null);}
"|" {return tok(sym.OR, null);}
"+" {return tok(sym.PLUS, null);}
"-" {return tok(sym.MINUS, null);}
"*" {return tok(sym.TIMES, null);}
"/" {return tok(sym.DIVIDE, null);}
">=" {return tok(sym.GE, null);}
">" {return tok(sym.GT, null);}
"<=" {return tok(sym.LE, null);}
"<" {return tok(sym.LT, null);}
"<>" {return tok(sym.NEQ, null);}
"=" {return tok(sym.EQ, null);}
\n	{newline();}
","	{return tok(sym.COMMA, null);}
"." {return tok(sym.DOT, null);}
";" {return tok(sym.SEMICOLON, null);}
":" {return tok(sym.COLON, null);}
"{" {return tok(sym.LBRACE, null);}
"}" {return tok(sym.RBRACE, null);}
"(" {return tok(sym.LBRACK, null);}
")" {return tok(sym.RBRACK, null);}
"[" {return tok(sym.LPAREN, null);}
"]" {return tok(sym.RPAREN, null);}
"nil" {return tok(sym.NIL,null);}
\" {string.setLength(0); yybegin(STRING);}
{Identifier} {return tok(sym.ID, yytext());}
{DecIntegerLiteral} {return tok(sym.NUM, new Integer(yytext()));}
[^] {err("Illegal character < "+yytext()+" >!");}
}

<STRING> 
{
\" {yybegin(YYINITIAL);	return tok(sym.STR,string.toString());}
[^\n\r\"\\]+ {string.append(yytext());}
\\t {string.append('\t');}
\\n {string.append('\n');}
\\r {string.append('\r');}
\\\" {string.append('\"');}
\\ {string.append('\\');}
}

<COMMENT> {
	"/*" {count++;}
	"*/" {count--;if (count==0) {yybegin(YYINITIAL);}}
	[^] { /* nothing*/ }
}