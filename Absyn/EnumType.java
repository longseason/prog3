package Absyn;
import Symbol.Symbol;

public class EnumType extends Type {
    public Symbol name;
    public EnumType(int p, Symbol name) {pos=p; this.name=name;}
    
}
