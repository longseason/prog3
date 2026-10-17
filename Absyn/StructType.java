package Absyn;
import Symbol.Symbol;

public class StructType extends Type {
    public boolean isUnion;
    public Symbol name;
    public StructType(int p, boolean isUnion, Symbol name) {pos=p; this.isUnion=isUnion; this.name=name;}
    
}
