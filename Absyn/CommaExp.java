package Absyn;

public class CommaExp extends Exp {
   public Exp left;
   public Exp right;
   public CommaExp(int p, Exp left, Exp right) {pos=p; this.left=left; this.right=right;}
}