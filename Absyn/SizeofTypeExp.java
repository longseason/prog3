package Absyn;

public class SizeofTypeExp extends Exp {
   public Type type;
   public SizeofTypeExp(int p, Type type) {pos=p; this.type=type;}
}