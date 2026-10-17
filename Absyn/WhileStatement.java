package Absyn;

public class WhileStatement extends Statement {
   public Exp test;
   public Statement body;
   public WhileStatement(int p, Exp test, Statement body) {pos=p; this.test=test; this.body=body;}
}