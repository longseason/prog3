package Absyn;
import Symbol.Symbol;

public class ArrayType extends Type {
    public Type base;
    public int size;
    public ArrayType(int p, Type base, int size) {pos=p; this.base=base; this.size=size;}
    
}
