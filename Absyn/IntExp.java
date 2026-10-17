package Absyn;
import Symbol.Symbol;
public class IntExp extends Exp {
   public long value;
   public IntExp(int p, long v) {pos=p; value=v;}
}
