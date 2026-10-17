package Absyn;

public class ForStatement extends Statement {
   public Exp init;
   public Exp test;
   public Exp incr;
   public Statement body;
   public ForStatement(int p, Exp init, Exp test, Exp incr, Statement body) {pos=p; this.init=init; this.test=test; this.incr=incr; this.body=body;}
}