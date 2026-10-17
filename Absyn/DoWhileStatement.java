package Absyn;

public class DoWhileStatement extends Statement {
   public Statement body;
   public Exp test;
   public DoWhileStatement(int p, Statement body, Exp test) {pos=p; this.body=body; this.test=test;}
}