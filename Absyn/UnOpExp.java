package Absyn;

public class UnOpExp extends Exp {
    public final static int NEG=0, POS=1, NOT=2, BITNOT=3, DEREF=4, ADDR=5, PREINC=6, PREDEC=7, POSTINC=8, POSTDEC=9;

    public int operator;
    public Exp exp;
    public UnOpExp(int p, int operator, Exp exp) {pos=p; this.operator=operator; this.exp=exp;}
}
