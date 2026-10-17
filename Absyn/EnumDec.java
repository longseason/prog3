package Absyn;
import Symbol.Symbol;

public class EnumDec extends Dec {
   public Symbol name;
   public EnumeratorList list;
   public EnumDec(int p, Symbol name, EnumeratorList list) {pos=p; this.name=name; this.list=list;}
}