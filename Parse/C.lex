package Parse;
import ErrorMsg.ErrorMsg;

%% 

%implements Lexer
%function nextToken
%type java_cup.runtime.Symbol
%char

%state COMMENT, STRING

ID=[a-zA-Z_][a-zA-Z0-9_]*

hexint=0[xX][0-9a-fA-F]+
octint=0[0-7]*
decint=[1-9][0-9]*
suffix=([uU][lL]?|[lL][uU]?)

%{
private void newline() {
  errorMsg.newline(yychar);
}

private void err(int pos, String s) {
  errorMsg.error(pos,s);
}

private void err(String s) {
  err(yychar,s);
}

private java_cup.runtime.Symbol tok(int kind) {
    return tok(kind, null);
}

private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

private ErrorMsg errorMsg;

Yylex(java.io.InputStream s, ErrorMsg e) {
  this(s);
  errorMsg=e;
}

private StringBuffer sb = new StringBuffer();
private int strStart;
private int quote;

%}

%eofval{
	{
	 return tok(sym.EOF, null);
        }
%eofval}       


%%
<YYINITIAL> " "	{}
<YYINITIAL> [\t\r\f\013]	{}
<YYINITIAL> \n	{newline();}

<YYINITIAL> "/*" { yybegin(COMMENT); }
<COMMENT> "*/" { yybegin(YYINITIAL); }
<COMMENT> \n { newline(); }
<COMMENT> . {}

<YYINITIAL> "int"	{return tok(sym.INT);}
<YYINITIAL> ">"	{return tok(sym.GT);}
<YYINITIAL> "/" {return tok(sym.DIVIDE);}
<YYINITIAL> ":" {return tok(sym.COLON);}
<YYINITIAL> "else" {return tok(sym.ELSE);}
<YYINITIAL> "|" {return tok(sym.OR);}
<YYINITIAL> "do" {return tok(sym.DO);}
<YYINITIAL> ">=" {return tok(sym.GE);}
<YYINITIAL> "<"	{return tok(sym.LT);}
<YYINITIAL> "-"	{return tok(sym.MINUS);}
<YYINITIAL> "for"	{return tok(sym.FOR);}
<YYINITIAL> "*"	{return tok(sym.TIMES);}
<YYINITIAL> ","	{return tok(sym.COMMA);}
<YYINITIAL> "<=" {return tok(sym.LE);}
<YYINITIAL> "="	{return tok(sym.ASSIGN);}
<YYINITIAL> "."	{return tok(sym.DOT);}
<YYINITIAL> "("	{return tok(sym.LPAREN);}
<YYINITIAL> ")"	{return tok(sym.RPAREN);}
<YYINITIAL> "if" {return tok(sym.IF);}
<YYINITIAL> ";"	{return tok(sym.SEMICOLON);}
<YYINITIAL> "while"	{return tok(sym.WHILE);}
<YYINITIAL> "["	{return tok(sym.LBRACK);}
<YYINITIAL> "]"	{return tok(sym.RBRACK);}
<YYINITIAL> "!=" {return tok(sym.NEQ);}
<YYINITIAL> "var"	{return tok(sym.VAR);}
<YYINITIAL> "break"	{return tok(sym.BREAK);}
<YYINITIAL> "&"	{return tok(sym.AND);}
<YYINITIAL> "+"	{return tok(sym.PLUS);}
<YYINITIAL> "{"	{return tok(sym.LBRACE);}
<YYINITIAL> "}"	{return tok(sym.RBRACE);}
<YYINITIAL> "==" {return tok(sym.EQ);}

<YYINITIAL> "switch" {return tok(sym.SWITCH);}
<YYINITIAL> "case" {return tok(sym.CASE);}
<YYINITIAL> "default"	{return tok(sym.DEFAULT);}
<YYINITIAL> "continue" {return tok(sym.CONTINUE);}
<YYINITIAL> "goto" {return tok(sym.GOTO);}
<YYINITIAL> "return" {return tok(sym.RETURN);}
<YYINITIAL> "char" {return tok(sym.CHAR);}
<YYINITIAL> "float" {return tok(sym.FLOAT);}
<YYINITIAL> "double" {return tok(sym.DOUBLE);}
<YYINITIAL> "long" {return tok(sym.LONG);}
<YYINITIAL> "short" {return tok(sym.SHORT);}
<YYINITIAL> "unsigned" {return tok(sym.UNSIGNED);}
<YYINITIAL> "signed" {return tok(sym.SIGNED);}
<YYINITIAL> "void" {return tok(sym.VOID);}
<YYINITIAL> "auto" {return tok(sym.AUTO);}
<YYINITIAL> "register" {return tok(sym.REGISTER);}
<YYINITIAL> "static" {return tok(sym.STATIC);}
<YYINITIAL> "extern" {return tok(sym.EXTERN);}
<YYINITIAL> "const" {return tok(sym.CONST);}
<YYINITIAL> "volatile" {return tok(sym.VOLATILE);}
<YYINITIAL> "struct" {return tok(sym.STRUCT);}
<YYINITIAL> "union" {return tok(sym.UNION);}
<YYINITIAL> "enum" {return tok(sym.ENUM);}
<YYINITIAL> "typedef" {return tok(sym.TYPEDEF);}
<YYINITIAL> "sizeof" {return tok(sym.SIZEOF);}
<YYINITIAL> "fun"	{return tok(sym.FUN);}

<YYINITIAL> "%"	{return tok(sym.MOD);}
<YYINITIAL> "!"	{return tok(sym.NOT);}
<YYINITIAL> "~"	{return tok(sym.TILDE);}
<YYINITIAL> "^"	{return tok(sym.XOR);}
<YYINITIAL> "?" {return tok(sym.QUESTION);}
<YYINITIAL> "++" {return tok(sym.INC);}
<YYINITIAL> "--" {return tok(sym.DEC);}
<YYINITIAL> "->" {return tok(sym.ARROW);}
<YYINITIAL> "<<" {return tok(sym.SHL);}
<YYINITIAL> ">>" {return tok(sym.SHR);}
<YYINITIAL> "&&" {return tok(sym.ANDAND);}
<YYINITIAL> "||" {return tok(sym.OROR);}
<YYINITIAL> "..." {return tok(sym.ELLIPSIS);}
<YYINITIAL> "+=" {return tok(sym.PLUSASSIGN);}
<YYINITIAL> "-=" {return tok(sym.MINUSASSIGN);}
<YYINITIAL> "*=" {return tok(sym.TIMESASSIGN);}
<YYINITIAL> "/=" {return tok(sym.DIVASSIGN);}
<YYINITIAL> "%=" {return tok(sym.MODASSIGN);}
<YYINITIAL> "<<=" {return tok(sym.SHLASSIGN);}
<YYINITIAL> ">>=" {return tok(sym.SHRASSIGN);}
<YYINITIAL> "&=" {return tok(sym.ANDASSIGN);}
<YYINITIAL> "^=" {return tok(sym.XORASSIGN);}
<YYINITIAL> "|=" {return tok(sym.ORASSIGN);}

<YYINITIAL> "#" {return tok(sym.HASH);}
<YYINITIAL> "##" {return tok(sym.HASHHASH);}

<YYINITIAL> {hexint}{suffix}? { return tok(sym.INTLIT, yytext()); }
<YYINITIAL> {octint}{suffix}? { return tok(sym.INTLIT, yytext()); }
<YYINITIAL> {decint}{suffix}? { return tok(sym.INTLIT, yytext()); }

<YYINITIAL> "\"" { sb.setLength(0); strStart = yychar; quote = 34; yybegin(STRING); }
<YYINITIAL> "'" { sb.setLength(0); strStart = yychar; quote = 39; yybegin(STRING); }
<STRING> [^\"'\\\n]+ { sb.append(yytext()); }
<STRING> [\"'] {
  if (yytext().charAt(0) != quote) { sb.append(yytext()); }
  else { yybegin(YYINITIAL);
         return new java_cup.runtime.Symbol(quote == 34 ? sym.STRING : sym.CHARLIT, strStart, yychar+1, sb.toString()); }
}
<STRING> \\n { sb.append((char)10); }
<STRING> \\t { sb.append((char)9); }
<STRING> \\r { sb.append((char)13); }
<STRING> \\a { sb.append((char)7); }
<STRING> \\b { sb.append((char)8); }
<STRING> \\f { sb.append((char)12); }
<STRING> \\v { sb.append((char)11); }
<STRING> \\\? { sb.append((char)63); }
<STRING> \\\\ { sb.append('\\'); }
<STRING> \\\" { sb.append('"'); }
<STRING> \\' { sb.append('\''); }
<STRING> "\\?" { sb.append('?'); }
<STRING> \\[0-7]([0-7][0-7]?)? { sb.append((char)(Integer.parseInt(yytext().substring(1), 8) & 0xFF)); }
<STRING> \\x[0-9a-fA-F]+ { sb.append((char)(new java.math.BigInteger(yytext().substring(2), 16).intValue() & 0xFF)); }
<STRING> \\. { err("Illegal escape sequence: " + yytext()); }
<STRING> \n { err("Unterminated string"); newline(); yybegin(YYINITIAL); }

<YYINITIAL> {ID} {return tok(sym.ID, yytext());}

<YYINITIAL> . { err("Illegal character: " + yytext()); }