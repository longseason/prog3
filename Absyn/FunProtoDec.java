package Absyn;
import Symbol.Symbol;

public class FunProtoDec extends Dec {
   public DescriptorList desc;
   public Type type;
   public Symbol name;
   public ParamList params;
   public FunProtoDec(int p, DescriptorList desc, Type type, Symbol name, ParamList params) {pos=p; this.desc=desc; this.type=type; this.name=name; this.params=params;}
}