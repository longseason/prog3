package Absyn;
import Symbol.Symbol;

public class PointerType extends Type {
    public Type base;
    public PointerType(int p, Type base) {pos=p; this.base=base;}
}
