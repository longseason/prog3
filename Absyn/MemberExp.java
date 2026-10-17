package Absyn;
import Symbol.Symbol;

public class MemberExp extends Exp {
   public Exp record;
   public Symbol field;
   public boolean arrow;
   public MemberExp(int p, Exp record, Symbol field, boolean arrow) {pos=p; this.record=record; this.field=field; this.arrow=arrow;}
}