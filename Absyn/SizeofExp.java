package Absyn;

public class SizeofExp extends Exp {
   public Exp exp;
   public SizeofExp(int p, Exp exp) {pos=p; this.exp=exp;}
}