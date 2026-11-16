package Absyn;
import Symbol.Symbol;
public class VarDec extends Dec {
   public DescriptorList desc;
   public Type type;
   public Symbol name;
   public Initializer init;
   public VarDec(int p, DescriptorList desc, Type type, Symbol name, Initializer init) {pos=p; this.desc=desc; this.type=type; this.name=name; this.init=init;}
   public boolean escape = false;
   public Semant.VarEntry entry;   // filled in by Semant, null for struct members
}