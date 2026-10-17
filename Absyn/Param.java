package Absyn;
import Symbol.Symbol;

public class Param extends Absyn {
   public Type type;
   public Symbol name;
   public boolean ellipsis;
   public Param(int p, Type type, Symbol name) {pos=p; this.type=type; this.name=name; ellipsis=false;}
   public Param(int p) {pos=p; type=null; name=null; ellipsis=true;}
}