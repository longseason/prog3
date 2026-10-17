package Absyn;
import Symbol.Symbol;

public class DescriptorList {
   public Symbol head;
   public DescriptorList tail;
   public DescriptorList(Symbol h, DescriptorList t) {head=h; tail=t;}
}