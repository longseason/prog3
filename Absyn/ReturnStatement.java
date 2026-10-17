package Absyn;

public class ReturnStatement extends Statement {
   public Exp exp;
   public ReturnStatement(int p, Exp exp) {pos=p; this.exp=exp;}
}