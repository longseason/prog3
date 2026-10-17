package Absyn;

public class ExpStatement extends Statement {
   public Exp exp;
   public ExpStatement(int p, Exp exp) {pos=p; this.exp=exp;}
}