package Absyn;
import Symbol.Symbol;
public class AssignExp extends Exp {
   public final static int ASSIGN=0, MUL=1, DIV=2, MOD=3, PLUS=4, MINUS=5, SHL=6, SHR=7, AND=8, XOR=9, OR=10;
   public Exp left;
   public Exp right;
   public int operator;
   public AssignExp(int p, Exp left, int operator, Exp right) {pos=p; this.left=left; this.operator=operator; this.right=right;}
}
