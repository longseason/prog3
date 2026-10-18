package Absyn;
import Symbol.Symbol;

/* ( exp ) - the handout asks that parentheses be reflected in the tree */
public class ParenExp extends Exp {
   public Exp exp;
   public ParenExp(int p, Exp exp) {pos=p; this.exp=exp;}
}
