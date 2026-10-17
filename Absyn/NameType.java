package Absyn;
import Symbol.Symbol;

public class NameType extends Type {
    public Symbol name;
    public NameType(int p, Symbol name) {pos=p; this.name=name;}
}
