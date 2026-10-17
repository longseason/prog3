package Absyn;

public class ParamList {
   public Param head;
   public ParamList tail;
   public ParamList(Param h, ParamList t) {head=h; tail=t;}
}