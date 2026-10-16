package Parse;
import java.io.PrintWriter;

public class Main {

  public static void main(String argv[]) throws java.io.IOException {
    for (int i = 0; i < argv.length; ++i) {
      String filename = argv[i];
      if (argv.length > 1)
	System.out.println("***Processing: " + filename);
      ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);
      java.io.InputStream inp=new java.io.FileInputStream(filename);
      Lexer lexer = new Yylex(inp,errorMsg);
      java_cup.runtime.Symbol tok;

      do {
	 String extra = "";
         tok=lexer.nextToken();
	 switch (tok.sym) {
	 case sym.ID:     extra = "\t$" + tok.value; break;
	// case sym.INT:    extra = "\t#" + tok.value; break;
	 case sym.STRING: extra = " \"" + tok.value + "\""; break;
     
      case sym.CHARLIT: extra = " '" + tok.value + "'"; break;
      case sym.INTLIT: extra = "\t#" + tok.value; break;
	 }
	 System.out.println(symnames[tok.sym] + " " + tok.left + extra);
      } while (tok.sym != sym.EOF);

      inp.close();
    }
  }

  static String symnames[] = new String[256]; // increased to 256 to take in symbols
  static {
     
     symnames[sym.FUNCTION] = "FUNCTION";
     symnames[sym.EOF] = "EOF";
     symnames[sym.INT] = "INT";
     symnames[sym.GT] = "GT";
     symnames[sym.DIVIDE] = "DIVIDE";
     symnames[sym.COLON] = "COLON";
     symnames[sym.ELSE] = "ELSE";
     symnames[sym.OR] = "OR";
     symnames[sym.NIL] = "NIL";
     symnames[sym.DO] = "DO";
     symnames[sym.GE] = "GE";
     symnames[sym.error] = "error";
     symnames[sym.LT] = "LT";
     symnames[sym.OF] = "OF";
     symnames[sym.MINUS] = "MINUS";
//     symnames[sym.ARRAY] = "ARRAY";
     symnames[sym.TYPE] = "TYPE";
     symnames[sym.FOR] = "FOR";
     symnames[sym.TO] = "TO";
     symnames[sym.TIMES] = "TIMES";
     symnames[sym.COMMA] = "COMMA";
     symnames[sym.LE] = "LE";
//     symnames[sym.IN] = "IN";
//     symnames[sym.END] = "END";
     symnames[sym.ASSIGN] = "ASSIGN";
     symnames[sym.STRING] = "STRING";
     symnames[sym.DOT] = "DOT";
     symnames[sym.LPAREN] = "LPAREN";
     symnames[sym.RPAREN] = "RPAREN";
     symnames[sym.IF] = "IF";
     symnames[sym.SEMICOLON] = "SEMICOLON";
     symnames[sym.ID] = "ID";
     symnames[sym.WHILE] = "WHILE";
     symnames[sym.LBRACK] = "LBRACK";
     symnames[sym.RBRACK] = "RBRACK";
     symnames[sym.NEQ] = "NEQ";
     symnames[sym.VAR] = "VAR";
     symnames[sym.BREAK] = "BREAK";
     symnames[sym.AND] = "AND";
     symnames[sym.PLUS] = "PLUS";
     symnames[sym.LBRACE] = "LBRACE";
     symnames[sym.RBRACE] = "RBRACE";
//     symnames[sym.LET] = "LET";
     symnames[sym.THEN] = "THEN";
     symnames[sym.EQ] = "EQ";

     // c89 stuff below
     // I specifically used AI here to avoid having to do this manually,
     // because the lines were all similar to the tokens from the sym class
    symnames[sym.SWITCH] = "SWITCH";
    symnames[sym.CASE] = "CASE";
    symnames[sym.DEFAULT] = "DEFAULT";
    symnames[sym.CONTINUE] = "CONTINUE";
    symnames[sym.GOTO] = "GOTO";
    symnames[sym.RETURN] = "RETURN";
    symnames[sym.CHAR] = "CHAR";
    symnames[sym.FLOAT] = "FLOAT";
    symnames[sym.DOUBLE] = "DOUBLE";
    symnames[sym.LONG] = "LONG";
    symnames[sym.SHORT] = "SHORT";
    symnames[sym.UNSIGNED] = "UNSIGNED";
    symnames[sym.SIGNED] = "SIGNED";
    symnames[sym.VOID] = "VOID";
    symnames[sym.AUTO] = "AUTO";
    symnames[sym.REGISTER] = "REGISTER";
    symnames[sym.STATIC] = "STATIC";
    symnames[sym.EXTERN] = "EXTERN";
    symnames[sym.CONST] = "CONST";
    symnames[sym.VOLATILE] = "VOLATILE";
    symnames[sym.STRUCT] = "STRUCT";
    symnames[sym.UNION] = "UNION";
    symnames[sym.ENUM] = "ENUM";
    symnames[sym.TYPEDEF] = "TYPEDEF";
    symnames[sym.SIZEOF] = "SIZEOF";
    symnames[sym.FUN] = "FUN";
    symnames[sym.CHARLIT] = "CHARLIT";
    symnames[sym.INTLIT] = "INTLIT";
    symnames[sym.MOD] = "MOD";
    symnames[sym.NOT] = "NOT";
    symnames[sym.TILDE] = "TILDE";
    symnames[sym.XOR] = "XOR";
    symnames[sym.QUESTION] = "QUESTION";
    symnames[sym.INC] = "INC";
    symnames[sym.DEC] = "DEC";
    symnames[sym.ARROW] = "ARROW";
    symnames[sym.SHL] = "SHL";
    symnames[sym.SHR] = "SHR";
    symnames[sym.ANDAND] = "ANDAND";
    symnames[sym.OROR] = "OROR";
    symnames[sym.ELLIPSIS] = "ELLIPSIS";
    symnames[sym.PLUSASSIGN] = "PLUSASSIGN";
    symnames[sym.MINUSASSIGN] = "MINUSASSIGN";
    symnames[sym.TIMESASSIGN] = "TIMESASSIGN";
    symnames[sym.DIVASSIGN] = "DIVASSIGN";
    symnames[sym.MODASSIGN] = "MODASSIGN";
    symnames[sym.SHLASSIGN] = "SHLASSIGN";
    symnames[sym.SHRASSIGN] = "SHRASSIGN";
    symnames[sym.ANDASSIGN] = "ANDASSIGN";
    symnames[sym.XORASSIGN] = "XORASSIGN";
    symnames[sym.ORASSIGN] = "ORASSIGN";
     symnames[sym.HASH] = "HASH";
      symnames[sym.HASHHASH] = "HASHHASH";
   }

}
