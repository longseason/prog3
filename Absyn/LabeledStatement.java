package Absyn;
import Symbol.Symbol;

public class LabeledStatement extends Statement {
   public Symbol label;
   public Statement statement;
   public LabeledStatement(int p, Symbol label, Statement statement) {pos=p; this.label=label; this.statement=statement;}
}