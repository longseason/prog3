package Absyn;
import Symbol.Symbol;

public class ArrayType extends Type {
    public Type base;
    public Exp size;
    public ArrayType(int p, Type base, Exp size) {pos=p; this.base=base; this.size=size;}
    
}
