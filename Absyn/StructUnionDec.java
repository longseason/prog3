package Absyn;
import Symbol.Symbol;

public class StructUnionDec extends Dec {
   public DescriptorList desc;
   public boolean isUnion;
   public Symbol name;
   public DecList members;
   public StructUnionDec(int p, DescriptorList desc, boolean isUnion, Symbol name, DecList members) {pos=p; this.desc=desc; this.isUnion=isUnion; this.name=name; this.members=members;}
}