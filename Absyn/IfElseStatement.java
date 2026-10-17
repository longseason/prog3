package Absyn;

public class IfElseStatement extends Statement {
   public Exp test;
   public Statement thenClause;
   public Statement elseClause;
   public IfElseStatement(int p, Exp test, Statement thenClause, Statement elseClause) {pos=p; this.test=test; this.thenClause=thenClause; this.elseClause=elseClause;}
}