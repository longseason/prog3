package Absyn;

public class CastExp extends Exp {
   public Type type;
   public Exp exp;
   public CastExp(int p, Type type, Exp exp) {pos=p; this.type=type; this.exp=exp;}
}