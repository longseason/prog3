package Absyn;

public class CompoundStatement extends Statement {
   public DecList decs;
   public StatementList stms;
   public CompoundStatement(int p, DecList decs, StatementList stms) {pos=p; this.decs=decs; this.stms=stms;}
}