package Absyn;
import Symbol.Symbol;

public class FunDec extends Dec {
   public DescriptorList desc;
   public Type type;
   public Symbol name;
   public ParamList params;
   public CompoundStatement body;
   public FunDec(int p, DescriptorList desc, Type type, Symbol name, ParamList params, CompoundStatement body) {pos=p; this.desc=desc; this.type=type; this.name=name; this.params=params; this.body=body;}
}