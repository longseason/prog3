package Absyn;

public class StatementList {
   public Statement head;
   public StatementList tail;
   public StatementList(Statement h, StatementList t) {head=h; tail=t;}
}