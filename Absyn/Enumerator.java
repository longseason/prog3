package Absyn;
import Symbol.Symbol;

public class Enumerator extends Absyn {
   public Symbol name;
   public Exp value;
   public Enumerator(int p, Symbol name, Exp value) {pos=p; this.name=name; this.value=value;}
}