package Absyn;

public class SubscriptExp extends Exp {
   public Exp array;
   public Exp index;
   public SubscriptExp(int p, Exp array, Exp index) {pos=p; this.array=array; this.index=index;}
}