package Absyn;
import Symbol.Symbol;

public class IdExp extends Exp {
    public Symbol name;
    public IdExp(int p, Symbol name) {pos=p; this.name=name;}
}
