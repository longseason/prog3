package Parse;

public class Parse {

  public ErrorMsg.ErrorMsg errorMsg;
  /* a C program is a list of top-level declarations */
  public Absyn.DecList absyn;

  public Parse(String filename) {
      errorMsg = new ErrorMsg.ErrorMsg(filename);
      java.io.InputStream inp;
      try {
        inp = new java.io.FileInputStream(filename);
      } catch (java.io.FileNotFoundException e) {
        throw new Error("File not found: " + filename);
      }
      Grm parser = new Grm(new Yylex(inp, errorMsg), errorMsg);

      try {
        absyn = (Absyn.DecList)(parser./*debug_*/parse().value);
      } catch (Throwable e) {
        /* CUP throws when it cannot recover from a syntax error */
        System.err.println(filename + ": parse aborted: " + e.getMessage());
        absyn = null;
      }
      finally {
        try { inp.close(); } catch (java.io.IOException e) {}
      }
  }
}
