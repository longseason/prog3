package Absyn;
import Symbol.Symbol;

public class GotoStatement extends Statement {
   public Symbol label;
   public GotoStatement(int p, Symbol label) {pos=p; this.label=label;}
}