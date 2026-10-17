package Absyn;
import Symbol.Symbol;

public class TypedefDec extends Dec {
   public Type type;
   public Symbol name;
   public TypedefDec(int p, Type type, Symbol name) {pos=p; this.type=type; this.name=name;}
}